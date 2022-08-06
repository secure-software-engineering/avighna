package de.fraunhofer.iem;

import de.fraunhofer.iem.util.*;
import org.apache.commons.cli.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class MainInterface {
    public static final RequestFile requestFile = new RequestFile();

    private static String agentSettingFile;

    private static boolean isInterrupted = false;

    private static void runApplicationWithJavaAgent(CommandLine commandLine) {
        try {
            String[] cmd;

            String javaCMD = "java" +
                    " -Xbootclasspath/p:" + commandLine.getOptionValue(CommandLineUtility.DYNAMIC_CG_GEN_LONG) +
                    " -javaagent:" + commandLine.getOptionValue(CommandLineUtility.DYNAMIC_CG_GEN_LONG) + "=" + agentSettingFile +
                    " -noverify -jar " +
                    commandLine.getOptionValue(CommandLineUtility.APP_JAR_SHORT);

            if (OperatingSystemUtil.isMac()) {
                String appleCMD = "tell app \"Terminal\"\n" +
                        "do script \"" + javaCMD + "\"\n" +
                        "end tell";
                cmd = new String[]{"osascript", "-e", appleCMD};
            } else {
                cmd = new String[]{"cmd.exe", "/c", "cd . & start cmd.exe /c \"" + javaCMD + " & set /p dummy=Spring application terminated. Press enter.\""};
            }

            System.out.println(Arrays.toString(cmd));
            Process proc = Runtime.getRuntime().exec(cmd);

            System.out.println("Application is running with the provided agent. " +
                    "\nIf the application is web then, please open browser and run different requests");

            String closeApp = "n";

            while (!closeApp.toLowerCase().equals("y") && !closeApp.toLowerCase().equals("yes")) {
                System.out.print("Completed? Should we terminate the application and generate DTS file?   ");
                Scanner scanner = new Scanner(System.in);

                closeApp = scanner.nextLine();

                if (closeApp == null || closeApp.equals("")) {
                    closeApp = "n";
                }
            }
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

        if (CommandLineUtility.getCommandLine().hasOption(CommandLineUtility.LIST_OF_REQUEST_LONG)) {
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
