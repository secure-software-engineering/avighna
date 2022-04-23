package de.fraunhofer.iem;

import de.fraunhofer.iem.util.*;
import org.apache.commons.cli.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class MainInterface {
    public static final RequestFile requestFile = new RequestFile();

    private static String agentSettingFile;

    private static boolean isInterrupted = false;

    private static void runApplicationWithJavaAgent(CommandLine commandLine) {
        try {
            String cmd = "cmd.exe /c cd . & start cmd.exe /c \"java" +
                    " -Xbootclasspath/p:" + commandLine.getOptionValue(CommandLineUtility.DYNAMIC_CG_GEN_LONG) +
                    " -javaagent:" + commandLine.getOptionValue(CommandLineUtility.DYNAMIC_CG_GEN_LONG) + "=" + agentSettingFile +
                    " -noverify -jar " +
                    commandLine.getOptionValue(CommandLineUtility.APP_JAR_SHORT) + " & set /p dummy=Spring application terminated. Press enter.\"";

            System.out.println(cmd);
            Process proc = Runtime.getRuntime().exec(cmd);

//            try {
//                Thread.sleep(60000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//
//            while (true) {
//                if (!proc.isAlive())
//                    break;
//            }

            String closeApp = "n";

            while (!closeApp.toLowerCase().equals("y") && !closeApp.toLowerCase().equals("yes")) {
                System.out.print("Completed? Should we terminate the application and generate DTS file?   ");
                Scanner scanner = new Scanner(System.in);

                closeApp = scanner.nextLine();

                if (closeApp == null || closeApp.equals("")) {
                    closeApp = "n";
                }
            }
//            System.out.println(cmd);
//
//            InputStream stdErr = proc.getErrorStream();
//            InputStreamReader isrErr = new InputStreamReader(stdErr);
//            BufferedReader brErr = new BufferedReader(isrErr);
//
//            String linee = null;
//            System.out.println("ERROR = ");
//
//            while (!isInterrupted) {
//                if (brErr.ready()) {
//                    System.out.println(brErr.readLine());
//                }
//            }
//
//            InputStream stdIn = proc.getInputStream();
//            InputStreamReader isr = new InputStreamReader(stdIn);
//            BufferedReader br = new BufferedReader(isr);
//
//            String line = null;
//            System.out.println("OUTPUT = ");
//
//            while (!isInterrupted) {
//                if (br.ready()) {
//                    System.out.println(br.readLine());
//                }
//            }
//
//            proc.destroy();
//
//            if (proc.isAlive()) {
//                proc.destroyForcibly();
//            }
//
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        CommandLineUtility.initializeCommandLineOptions();

        CommandLineUtility.getCommandLineOptions(args);

        CommandLineUtility.validateCommandLineOptions();

        agentSettingFile = YamlUtility.generateAgentSettingsFile(CommandLineUtility.getCommandLine());

        File tempFile = new File(
                CommandLineUtility.getCommandLine().getOptionValue(CommandLineUtility.OUT_ROOT_DIR_SHORT)
                        + File.separator + "allDotFiles" + File.separator + "dynamic_callgraph_1.ser");

        if (tempFile.exists())
            tempFile.delete();

        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                runApplicationWithJavaAgent(CommandLineUtility.getCommandLine());
            }
        });
        t1.start();


//        while (true) {
//            if (tempFile.exists())
//                break;
//        }

        while (true) {
            if (!t1.isAlive()) {
                break;
            }
        }

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (!CommandLineUtility.getCommandLine().hasOption(CommandLineUtility.LIST_OF_REQUEST_LONG)) {
            System.out.println("Spring application is up and running with the provided agent. " +
                    "\nPlease open browser and run different requests");

            String closeApp = "n";

            while (!closeApp.toLowerCase().equals("y") && !closeApp.toLowerCase().equals("yes")) {
                System.out.print("Completed? Should we terminate the application and generate DTS file?   ");
                Scanner scanner = new Scanner(System.in);

                closeApp = scanner.nextLine();

                if (closeApp == null || closeApp.equals("")) {
                    closeApp = "n";
                }
            }
        } else {
            System.out.println("Spring application is up and running with the provided agent. " +
                    "\nSending the provided requests");
            for (String request : requestFile.getRequests()) {
                try {
                    System.out.println("Executing = " + request);
                    Runtime.getRuntime().exec(request).waitFor();
                    Thread.sleep(3000);
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        isInterrupted = true;

        if (t1.isAlive()) {
            System.out.println("Saddddlyyy still alllive");
        }

        ZipUtil.generateDTS(CommandLineUtility.getCommandLine());

        if (CommandLineUtility.getCommandLine().hasOption(CommandLineUtility.SAVE_IMG_FILE_LONG)) {
            DotToImgUtil.generateImageFromDot();
        }
    }
}
