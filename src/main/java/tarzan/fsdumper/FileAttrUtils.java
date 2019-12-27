package tarzan.fsdumper;

import java.io.IOException;
import java.nio.file.FileSystemException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public final class FileAttrUtils {
    public static Properties getFileAttributes(Path path) throws IOException {
        final Properties properties = new Properties();
        // get all supported file attribute views (depends on OS)
        // https://docs.oracle.com/javase/8/docs/api/java/nio/file/FileSystem.html#supportedFileAttributeViews--
        // TODO: check if it is able to read ACLs, etc.; see https://www.bityard.org/wiki/tech/os/linux/xattrs
        final Set<String> supportedFileAttributeViews = FileSystems.getDefault().supportedFileAttributeViews();
        for (String fileAttributeView : supportedFileAttributeViews) {
            try {
                // read file attributes by the file attribute view
                // https://docs.oracle.com/javase/8/docs/api/java/nio/file/Files.html#readAttributes-java.nio.file.Path-java.lang.String-java.nio.file.LinkOption...-
                for (Map.Entry<String, Object> entry : Files.readAttributes(path, fileAttributeView + ":*").entrySet()) {
                    properties.setProperty(entry.getKey(), entry.getValue().toString());
                }
            } catch (FileSystemException e) {
                // NOP (some file attribute view are not supported on all systems, e.g., "dos" at unix/linux systems
            }
        }
        return properties;
    }
}
