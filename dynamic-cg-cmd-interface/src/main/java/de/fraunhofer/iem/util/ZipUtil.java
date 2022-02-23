package de.fraunhofer.iem.util;

import org.apache.commons.cli.CommandLine;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipUtil {
    public static void generateDTS(CommandLine commandLine) {
        ArrayList<String> dotFiles = new ArrayList<>();
        try {
            Files.walk(Paths.get(CommandLineUtility.getCommandLine().getOptionValue(CommandLineUtility.OUT_ROOT_DIR_SHORT) + File.separator + "allDotFiles" + File.separator))
                    .filter(Files::isRegularFile)
                    .filter(it -> it.toAbsolutePath().toString().endsWith(".ser"))
                    .forEach(it -> {dotFiles.add(it.toAbsolutePath().toString());});

            FileOutputStream fos = new FileOutputStream(CommandLineUtility.getCommandLine().getOptionValue(CommandLineUtility.OUT_ROOT_DIR_SHORT) + File.separator + "dynamic_cg.dst");

            ZipOutputStream zos = new ZipOutputStream(fos);

            for (String path : dotFiles) {
                File temp = new File(path);
                FileInputStream fileInputStream = new FileInputStream(temp);
                ZipEntry zipEntry = new ZipEntry(temp.getName());
                zos.putNextEntry(zipEntry);
                byte[] bytes = new byte[1024];
                int length;
                while((length = fileInputStream.read(bytes)) >= 0) {
                    zos.write(bytes, 0, length);
                }
                fileInputStream.close();
            }

            zos.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
