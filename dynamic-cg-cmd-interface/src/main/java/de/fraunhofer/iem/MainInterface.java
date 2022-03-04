package de.fraunhofer.iem;

import de.fraunhofer.iem.util.*;
import org.apache.commons.cli.*;

import java.io.*;

/**
 * Main method of this command line tool
 *
 * @author Ranjith Krishnamurthy
 */
public class MainInterface {
    public static final RequestFile requestFile = new RequestFile();

    private static String agentSettingFile;
    private static String allDotFilesLocation;

    private static boolean isInterrupted = false;

    private static void runApplicationWithJavaAgent(CommandLine commandLine) {
        try {
            String cmd = "java" + " -javaagent:" + commandLine.getOptionValue(CommandLineUtility.DYNAMIC_CG_GEN_LONG) + "=" + agentSettingFile + " -jar " + commandLine.getOptionValue(CommandLineUtility.APP_JAR_SHORT);

            Process proc = Runtime.getRuntime().exec(cmd);

            System.out.println(cmd);

            InputStream stdErr = proc.getErrorStream();
            InputStreamReader isrErr = new InputStreamReader(stdErr);
            BufferedReader brErr = new BufferedReader(isrErr);

            System.out.println("ERROR = ");

            while (!isInterrupted) {
                if (brErr.ready()) {
                    System.out.println(brErr.readLine());
                }
            }

            InputStream stdIn = proc.getInputStream();
            InputStreamReader isr = new InputStreamReader(stdIn);
            BufferedReader br = new BufferedReader(isr);

            String line = null;
            System.out.println("OUTPUT = ");

            while (!isInterrupted) {
                if (br.ready()) {
                    System.out.println(br.readLine());
                }
            }

            proc.destroy();

            if (proc.isAlive()) {
                proc.destroyForcibly();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Main Method
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        CommandLineUtility.initializeCommandLineOptions();

        CommandLineUtility.parseCommandLineArguments(args);

        CommandLineUtility.validateCommandLineOptions();

        agentSettingFile = YamlUtility.generateAgentSettingsFile(CommandLineUtility.getCommandLine());
        allDotFilesLocation = CommandLineUtility.getCommandLine().getOptionValue(CommandLineUtility.OUT_ROOT_DIR_SHORT) + File.separator + "allDotFiles";

        File tempFile = new File(allDotFilesLocation + File.separator + "dynamic_callgraph_1.ser");

        if (tempFile.exists()) tempFile.delete();

        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                runApplicationWithJavaAgent(CommandLineUtility.getCommandLine());
            }
        });
        t1.start();


        while (true) {
            if (tempFile.exists()) break;
        }

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        for (String request : requestFile.getRequests()) {
            try {
                System.out.println("Executing = " + request);
                Runtime.getRuntime().exec(request).waitFor();
                Thread.sleep(3000);
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }

        isInterrupted = true;

        if (t1.isAlive()) {
            System.out.println("Saddddlyyy still alllive");
        }

        ZipUtil.generateDTS();

        if (CommandLineUtility.getCommandLine().hasOption(CommandLineUtility.SAVE_IMG_FILE_LONG)) {
            DotToImgUtil.generateImageFromDot();
        }
    }

    public static String getAgentSettingFile() {
        return agentSettingFile;
    }

    public static String getAllDotFilesLocation() {
        return allDotFilesLocation;
    }
}
