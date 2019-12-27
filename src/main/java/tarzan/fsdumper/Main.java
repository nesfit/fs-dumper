package tarzan.fsdumper;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.*;
import java.util.logging.Logger;

public class Main {

    private static Logger logger = Logger.getLogger(Main.class.getCanonicalName());

    private static void printHelp() {
        System.err.println("Usage: " + Main.class.getCanonicalName() + " c <zip-file> <source-dir-to-pack> "
                + "# create new archive recursively including all files in the source dir and their attributes in comments");
        System.err.println("Usage: " + Main.class.getCanonicalName() + " a <dir-to-save-attrs> <source-dir-to-analyze> "
                + "# create the same directory structure as the source dir their where the files will contain"
                + " lists of properties storing attributes of coresponding files in the source dir");
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 3) {
            printHelp();
            System.exit(1);
        }
        switch (args[0]) {
            case "c": {
                final String zipFileName = args[1];
                final Path sourcePath = Paths.get(args[2]);
                try (ZipDir zipDir = new ZipDir(sourcePath, zipFileName, logger)) {
                    Files.walkFileTree(sourcePath, zipDir);
                }
            } break;
            case "a": {
                final Path attrsPath = Paths.get(args[1]);
                final Path sourcePath = Paths.get(args[2]);
                final AttrsDir attrsDir = new AttrsDir(sourcePath, attrsPath, logger);
                Files.walkFileTree(sourcePath, attrsDir);
            } break;
            default: {
                printHelp();
                System.exit(2);
            }
        }
    }
}
