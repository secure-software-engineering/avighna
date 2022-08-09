package de.fraunhofer.iem.util;

import de.fraunhofer.iem.MainInterface;
import guru.nidi.graphviz.engine.*;
import org.apache.commons.cli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Utility class to convert the DOT file to image file
 *
 * @author Ranjith Krishnamurthy
 */
public class DotToImgUtil {
    /**
     * This method converts the given dot file into image file
     *
     * @param dotFileName         Dot file name
     * @param outputImageFileName Image file name
     */
    private static void saveDotAsSVG(String dotFileName, String outputImageFileName) {
        try {
            GraphvizCmdLineEngine engine = new GraphvizCmdLineEngine();
            engine.timeout(5, TimeUnit.MINUTES);
            Graphviz.useEngine(engine);
            Graphviz.fromFile(new File(dotFileName)).render(Format.SVG).toFile(new File(outputImageFileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Generates images files for all the DOT files present in the given output root directory
     */
    public static void generateImageFromDot() {
        CommandLine commandLine = CommandLineUtility.getCommandLine();

        List<String> dotFiles = new ArrayList<>();

        try {
            Files.walk(Paths.get(CommandLineUtility.getCommandLine().getOptionValue(CommandLineUtility.OUT_ROOT_DIR_SHORT)
                            + File.separator + "allDotFiles" + File.separator))
                    .filter(Files::isRegularFile)
                    .filter(it -> it.toAbsolutePath().toString().endsWith(".dot"))
                    .forEach(it -> {
                        dotFiles.add(it.toAbsolutePath().toString());
                    });

            for (String dotFileName : dotFiles) {
                saveDotAsSVG(dotFileName, dotFileName.substring(0, dotFileName.lastIndexOf('.')) + ".svg");
            }

            if (!commandLine.hasOption(CommandLineUtility.SAVE_DOT_FILE_LONG)) {
                for (String dotFileName : dotFiles) {
                    saveDotAsSVG(dotFileName, dotFileName.substring(0, dotFileName.lastIndexOf('.')) + ".svg");
                    new File(dotFileName).delete();
                }
            } else {
                for (String dotFileName : dotFiles) {
                    saveDotAsSVG(dotFileName, dotFileName.substring(0, dotFileName.lastIndexOf('.')) + ".svg");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
