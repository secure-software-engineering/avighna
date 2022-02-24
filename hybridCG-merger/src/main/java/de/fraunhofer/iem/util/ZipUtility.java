package de.fraunhofer.iem.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Taken from https://www.journaldev.com/960/java-unzip-file-example#:~:text=To%20unzip%20a%20zip%20file,present%20in%20the%20zip%20file.
 */
public class ZipUtility {
    public static List<String> unzipDTSFile(String dtsFilePath) {
        File dir = null;
        try {
            dir = new File(Files.createTempDirectory("allDotFiles").toFile().getAbsolutePath());
        } catch (IOException e) {
            //TODO: Handle custom exception
            e.printStackTrace();
            return new ArrayList<>();
        }

        if (!dir.exists())
            dir.mkdirs();

        try {
            FileInputStream fileInputStream = new FileInputStream(dtsFilePath);
            ZipInputStream zipInputStream = new ZipInputStream(fileInputStream);
            ZipEntry zipEntry = zipInputStream.getNextEntry();

            byte[] buffer = new byte[1024];

            while (zipEntry != null) {
                String fileName = zipEntry.getName();
                File newFile = new File(dir.getAbsolutePath() + File.separator + fileName);

                new File(newFile.getParent()).mkdirs();

                FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                while ((len = zipInputStream.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();

                zipInputStream.closeEntry();
                zipEntry = zipInputStream.getNextEntry();
            }

            zipInputStream.closeEntry();
            zipInputStream.close();
            fileInputStream.close();
        } catch (IOException e) {
            //TODO: Throw custom exception to inform the user
            e.printStackTrace();
        }

        ArrayList<String> dotFiles = new ArrayList<>();

        try {
            Files.walk(Paths.get(dir.getAbsolutePath()))
                    .filter(Files::isRegularFile)
                    .filter(it -> it.toAbsolutePath().toString().endsWith(".ser"))
                    .forEach(it -> {
                        dotFiles.add(it.toAbsolutePath().toString());
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }

        return dotFiles;
    }
}
