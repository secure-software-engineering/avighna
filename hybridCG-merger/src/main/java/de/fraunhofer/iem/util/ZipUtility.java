package de.fraunhofer.iem.util;

import de.fraunhofer.iem.exception.DtsZipUtilException;

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
    /**
     * Unzip the DTS file
     *
     * @param dtsFilePath Path of DTS file
     * @return Path of unzipped folder
     * @throws DtsZipUtilException Zip utility failed to unzip DTS fie
     */
    public static String unzipDTSFile(String dtsFilePath) throws DtsZipUtilException {
        File dir = null;
        try {
            dir = new File(Files.createTempDirectory("allDotFiles").toFile().getAbsolutePath());
        } catch (IOException e) {
            throw new DtsZipUtilException("Failed to create temporary directory for unzipping DTS file." +
                    "\nMessage = " + e.getMessage());
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
            throw new DtsZipUtilException("Failed to unzip DTS file." +
                    "\nMessage = " + e.getMessage());
        }

        return dir.getAbsolutePath();
    }
}
