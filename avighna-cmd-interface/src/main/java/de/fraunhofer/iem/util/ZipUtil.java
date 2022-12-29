package de.fraunhofer.iem.util;

import net.lingala.zip4j.ZipFile;
import org.apache.commons.cli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class ZipUtil {
    public static void generateDst(CommandLine commandLine) {
        ArrayList<String> dotFiles = new ArrayList<>();
        try {
            Files.walk(Paths.get(CommandLineUtility.getCommandLine().getOptionValue(CommandLineUtility.OUT_ROOT_DIR_SHORT) + File.separator + "allDotFiles" + File.separator))
                    .filter(Files::isRegularFile)
                    .filter(it -> it.toAbsolutePath().toString().endsWith(".ser"))
                    .forEach(it -> {dotFiles.add(it.toAbsolutePath().toString());});

            ZipFile zipFile = new ZipFile(CommandLineUtility.getCommandLine().getOptionValue(CommandLineUtility.OUT_ROOT_DIR_SHORT) + File.separator + "dynamic_cg.dst");

            for (String path : dotFiles) {
                zipFile.addFile(new File(path));
            }

            zipFile.addFolder(new File(CommandLineUtility.getCommandLine().getOptionValue(CommandLineUtility.OUT_ROOT_DIR_SHORT) +
                    File.separator +
                    "dynamicCP" + File.separator));

            zipFile.close();
        } catch (IOException e) {
            LoggerUtil.getLOGGER().warning("Something went wrong while generating DST file = " + e.getMessage());
        }
    }
}
