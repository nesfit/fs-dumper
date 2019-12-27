package tarzan.fsdumper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AttrsDir extends SimpleFileVisitor<Path> {
    private Path attrsPath;
    private Path sourcePath;
    private Logger logger;

    public AttrsDir(Path sourcePath, Path attrsPath, Logger logger) {
        this.sourcePath = sourcePath;
        this.attrsPath = attrsPath;
        this.logger = logger;
    }

    /**
     * Invoked for a file that could not be visited.
     *
     * @param file the file to visit
     * @param exc  the exception on visit
     */
    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        logger.log(Level.WARNING, "Cannot process file " + file, exc);
        return super.visitFileFailed(file, exc);
    }

    /**
     * Invoked for a file in a directory to to store its attributes in a corresponding file in the attrs directory.
     *
     * @param file       the file to add
     * @param attributes the attributes to set for the file
     */
    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) throws IOException {
        final Path fileRelativePath = sourcePath.relativize(file);
        final Path attrsFilePath = attrsPath.resolve(fileRelativePath);
        logger.info("processing " + fileRelativePath);
        // get properties representing the file's attributes
        final Properties fileAttributesAsProperties = FileAttrUtils.getFileAttributes(file);
        // create all components of the target directory
        Files.createDirectories(attrsFilePath.getParent());
        // store the properties in the target file
        final File attrsFile = attrsFilePath.toFile();
        try (FileOutputStream attrsFileOutputStream = new FileOutputStream(attrsFile)) {
            fileAttributesAsProperties.store(attrsFileOutputStream, fileRelativePath.toString());
        }
        // set the target file timestamps
        Files.setLastModifiedTime(attrsFilePath, attributes.lastModifiedTime());
        logger.info("finished " + fileRelativePath);
        return FileVisitResult.CONTINUE;
    }
}
