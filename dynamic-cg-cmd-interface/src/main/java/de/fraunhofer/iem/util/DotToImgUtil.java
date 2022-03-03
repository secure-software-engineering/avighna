package de.fraunhofer.iem.util;

import de.fraunhofer.iem.MainInterface;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import org.apache.commons.cli.CommandLine;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class DotToImgUtil {
    /**
     * This method converts the given dot file into image file
     *
     * @param dotFileName         Dot file name
     * @param outputImageFileName Image file name
     */
    private static void saveDotAsSVG(String dotFileName, String outputImageFileName) {
        try {
            Graphviz.fromFile(new File(dotFileName)).render(Format.SVG).toFile(new File(outputImageFileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void generateImageFromDot() {
        CommandLine commandLine = CommandLineUtility.getCommandLine();
        String outRootDir = commandLine.getOptionValue(CommandLineUtility.OUT_ROOT_DIR_LONG);
        ;

        List<String> dotFiles = new ArrayList<>();

        try {
            Files.walk(Paths.get(CommandLineUtility.getCommandLine().getOptionValue(CommandLineUtility.OUT_ROOT_DIR_SHORT) + File.separator + "allDotFiles" + File.separator))
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
