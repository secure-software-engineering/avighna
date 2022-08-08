package de.fraunhofer.iem.hybridCG;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for file processing
 *
 * @author Ranjith Krishnamurthy
 */
public class FilesUtils {
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
                    .replaceAll(File.separator, ".")
                    .replaceAll("^\\.", "")
                    .replaceAll("\\.class$", "")));
        } catch (IOException e) {
            System.err.println("Something went wrong!\nStacktrace: \n");
            e.printStackTrace();
        }

        return appClasses;
    }
}
