package de.fraunhofer.iem;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Utility for File operations
 *
 * @author Ranjith Krishnamurthy
 */
public class FilesUtils {
    /**
     * Checks whether the given string can be a valid path or not
     *
     * @param path Path
     * @return Valid path or not
     */
    protected static boolean isValidPath(String path) {
        try {
            Paths.get(path);
        } catch (InvalidPathException | NullPointerException ex) {
            return false;
        }

        return true;
    }

    /**
     * Process the given app path that contains the classes and returns the list of classes.
     *
     * @param appClassesPath App class path that contains the classes to be converted into Jimple
     * @return List of classes name
     */
    protected static List<String> getClassesAsList(String appClassesPath) {
        Path path = Paths.get(appClassesPath);

        List<String> appClasses = new ArrayList<>();

        try {
            Files.find(path,
                    Integer.MAX_VALUE,
                    (filePath, fileAttr) -> filePath.toString().endsWith(".class") && fileAttr.isRegularFile()
            ).forEach(p -> appClasses.add(p.toString()
                    .replace(path.toString(), "")
                    .replaceAll("\\\\", ".")
                    .replaceAll("^\\.", "")
                    .replaceAll("\\.class$", "")));
        } catch (IOException e) {
            System.err.println("Something went wrong!\nStacktrace: \n");
            e.printStackTrace();
            System.exit(-1);
        }

        return appClasses;
    }

    /**
     * This method recursively creates a directory for the given class name in the given baseDir
     *
     * @param baseDir   base directory
     * @param className Class name
     * @return True if successful otherwise false
     */
    protected static boolean recursivelyCreateDirectory(String baseDir, String className) {
        ArrayList<String> stringArray = new ArrayList<>(Arrays.asList(className.split("\\.")));
        stringArray.remove(stringArray.size() - 1);

        StringBuilder completePath = new StringBuilder(baseDir);

        for (String str : stringArray) {
            completePath.append(File.separator).append(str);
        }

        File completePathFile = new File(completePath.toString());

        if (!completePathFile.exists())
            return completePathFile.mkdirs();

        return true;
    }
}
