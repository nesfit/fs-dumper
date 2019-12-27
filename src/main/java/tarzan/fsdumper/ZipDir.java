package tarzan.fsdumper;

import java.io.*;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipDir extends SimpleFileVisitor<Path> implements Closeable {
    private ZipOutputStream zipOutputStream;
    private Path sourcePath;
    private Logger logger;

    public ZipDir(Path sourcePath, OutputStream zipFileOutputStream, Logger logger) {
        // add buffer to the output-stream if unbuffered
        final OutputStream zipOutputStream = zipFileOutputStream instanceof BufferedOutputStream
                ? zipFileOutputStream : new BufferedOutputStream(zipFileOutputStream);
        this.zipOutputStream = new ZipOutputStream(zipOutputStream);
        this.sourcePath = sourcePath;
        this.logger = logger;
    }

    public ZipDir(Path sourcePath, String zipFileName, Logger logger) throws FileNotFoundException {
        this(sourcePath, new BufferedOutputStream(new FileOutputStream(zipFileName)), logger);
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
     * Invoked for a file in a directory to add its data and to store its attributes in the ZIP output stream.
     *
     * @param file       the file to add
     * @param attributes the attributes to set for the file
     */
    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) throws IOException {
        if (!attributes.isRegularFile()) {
            throw new UnsupportedEncodingException("Cannot ZIP other than regular files.");
        }
        // new ZIP entry with path relative to the ZIP root path
        final String fileRelativePathString = sourcePath.relativize(file).toString();
        logger.info("processing " + fileRelativePathString);
        final ZipEntry zipEntry = new ZipEntry(fileRelativePathString);
        // file attribute properties will be stored in the ZIP entry's comment
        try {
            final Properties fileAttributesAsProperties = FileAttrUtils.getFileAttributes(file);
            final StringWriter fileAttributesAsString = new StringWriter();
            fileAttributesAsProperties.store(fileAttributesAsString, fileRelativePathString);
            zipEntry.setComment(fileAttributesAsString.toString());
        } catch (IOException e) {
            throw new IOException("Cannot read the file attributes and store them as properties in the file's ZIP entry.", e);
        }
        // TODO: use the ZIP entry's extra field to store some file attributes, e.g., 0x7855 (unix2) + 0x0008 (this block size) + 0x???? (UID) + 0x???? (GID); see https://docs.oracle.com/javase/8/docs/api/java/util/zip/ZipEntry.html#setExtra-byte:A-
        // the ZIP entry will have the same timestamps as the original file
        zipEntry.setCreationTime(attributes.creationTime());
        zipEntry.setLastAccessTime(attributes.lastAccessTime());
        zipEntry.setLastModifiedTime(attributes.lastModifiedTime());
        // add the ZIP entry and the file data into the ZIP output stream
        try {
            zipOutputStream.putNextEntry(zipEntry);
            final byte[] bytes = Files.readAllBytes(file);
            zipOutputStream.write(bytes, 0, bytes.length);
            zipOutputStream.closeEntry();
        } catch (IOException e) {
            throw new IOException("Cannot add the file's metadata and/or data into the ZIP output stream.", e);
        }
        logger.info("finished " + fileRelativePathString);
        return FileVisitResult.CONTINUE;
    }

    /**
     * Closes this stream and releases any system resources associated
     * with it. If the stream is already closed then invoking this
     * method has no effect.
     *
     * <p> As noted in {@link AutoCloseable#close()}, cases where the
     * close may fail require careful attention. It is strongly advised
     * to relinquish the underlying resources and to internally
     * <em>mark</em> the {@code Closeable} as closed, prior to throwing
     * the {@code IOException}.
     *
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void close() throws IOException {
        this.zipOutputStream.close();
    }
}
