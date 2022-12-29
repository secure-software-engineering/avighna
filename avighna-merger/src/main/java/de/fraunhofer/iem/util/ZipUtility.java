package de.fraunhofer.iem.util;

import de.fraunhofer.iem.exception.DtsZipUtilException;
import net.lingala.zip4j.ZipFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * @author Ranjith Krishnamurthy
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

        try (ZipFile zipFile = new ZipFile(new File(dtsFilePath))) {
            zipFile.extractAll(dir.getAbsolutePath());
        } catch (IOException e) {
            throw new DtsZipUtilException("Failed to extract provided DST file." +
                    "\nMessage = " + e.getMessage());
        }

        return dir.getAbsolutePath();
    }
}
