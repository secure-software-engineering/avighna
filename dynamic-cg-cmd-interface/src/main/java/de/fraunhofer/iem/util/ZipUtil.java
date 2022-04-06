package de.fraunhofer.iem.util;

import org.apache.commons.cli.CommandLine;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Objects;
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
                zipFile(path, zos, "");
            }

            zipFolder(CommandLineUtility.getCommandLine().getOptionValue(CommandLineUtility.OUT_ROOT_DIR_SHORT) +
                    File.separator +
                    "dynamicCP" + File.separator, "dynamicCP", zos);

            zos.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void zipFile(String path, ZipOutputStream zos, String rootDir) throws IOException {
        File temp = new File(path);
        FileInputStream fileInputStream = new FileInputStream(temp);
        String zipEntryName;

        if (Objects.equals(rootDir, "")) {
            zipEntryName = temp.getName();
        } else {
            zipEntryName = rootDir + "/" + temp.getName();
        }

        ZipEntry zipEntry = new ZipEntry(zipEntryName);
        zos.putNextEntry(zipEntry);
        byte[] bytes = new byte[1024];
        int length;
        while((length = fileInputStream.read(bytes)) >= 0) {
            zos.write(bytes, 0, length);
        }
        fileInputStream.close();
    }

    private static void zipFolder(String folder, String parentFolder, ZipOutputStream zos) throws IOException {
        File dirFile = new File(folder);

        if (!dirFile.exists()) {
            return;
        }

        File[] files = dirFile.listFiles();

        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
                zipFolder(file.getAbsolutePath(), parentFolder + "/" + file.getName(), zos);
                continue;
            }

            zipFile(file.getAbsolutePath(), zos, parentFolder);
        }
    }
}
