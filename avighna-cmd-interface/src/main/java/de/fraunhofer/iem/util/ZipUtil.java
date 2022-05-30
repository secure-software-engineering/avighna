package de.fraunhofer.iem.util;

import de.fraunhofer.iem.MainInterface;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Utility class for Zip file
 *
 * @author Ranjith Krishnamurthy
 */
public class ZipUtil {
    /**
     * Generates the DTS file (dynamic traces) from the generated .ser files present in the given output root directory
     * <p>
     * Note: DTS file is a compressed (ZIP) file with the file extension .dts, that contains all the .ser file.
     */
    public static void generateDTS() {
        ArrayList<String> dotFiles = new ArrayList<>();
        try {
            Files.walk(Paths.get(MainInterface.getAllDotFilesLocation() + File.separator))
                    .filter(Files::isRegularFile)
                    .filter(it -> it.toAbsolutePath().toString().endsWith(".ser"))
                    .forEach(it -> {
                        dotFiles.add(it.toAbsolutePath().toString());
                    });

            FileOutputStream fos = new FileOutputStream(CommandLineUtility.getCommandLine().getOptionValue(CommandLineUtility.OUT_ROOT_DIR_SHORT) + File.separator + "dynamic_cg.dst");

            ZipOutputStream zos = new ZipOutputStream(fos);

            for (String path : dotFiles) {
                File temp = new File(path);
                FileInputStream fileInputStream = new FileInputStream(temp);
                ZipEntry zipEntry = new ZipEntry(temp.getName());
                zos.putNextEntry(zipEntry);
                byte[] bytes = new byte[1024];
                int length;
                while ((length = fileInputStream.read(bytes)) >= 0) {
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
