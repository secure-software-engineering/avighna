package de.fraunhofer.iem;

import de.fraunhofer.iem.util.*;
import org.apache.commons.cli.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class MainInterface {
    public static final RequestFile requestFile = new RequestFile();

    private static String agentSettingFile;

    private static boolean isInterrupted = false;

    private static Process getCompleteCmd(String javaCmd) throws IOException {
        String[] cmd;

        if (OperatingSystemUtil.isMac()) {
            String appleCMD = "tell app \"Terminal\"\n" +
                    "do script \"" + javaCmd + "\"\n" +
                    "end tell";
            cmd = new String[]{"osascript", "-e", appleCMD};
        } else {
            cmd = new String[]{"cmd.exe", "/c", "cd . & start cmd.exe /c \"" + javaCmd + " & set /p dummy=Spring application terminated. Press enter.\""};
        }

        System.out.println(Arrays.toString(cmd));

        return Runtime.getRuntime().exec(cmd);
    }

    private static void runApplicationWithJavaAgent(CommandLine commandLine) {
        try {
            StringBuilder javaCMD = new StringBuilder("java" +
                    " -Xbootclasspath/p:" + commandLine.getOptionValue(CommandLineUtility.DYNAMIC_CG_GEN_LONG) +
                    " -javaagent:" + commandLine.getOptionValue(CommandLineUtility.DYNAMIC_CG_GEN_LONG) + "=" + agentSettingFile +
                    " -noverify -jar " +
                    commandLine.getOptionValue(CommandLineUtility.APP_JAR_SHORT));

            if (commandLine.hasOption(CommandLineUtility.APP_ARG_SHORT)) {
                for (String arg : commandLine.getOptionValue(CommandLineUtility.APP_ARG_SHORT).split(":")) {
                    javaCMD.append(" ").append(arg);
                }
            }

            if (CommandLineUtility.getCommandLine().hasOption(CommandLineUtility.LIST_OF_REQUEST_LONG)) {
                String[] cmd = {
                        "java",
                        "-Xbootclasspath/p:" + commandLine.getOptionValue(CommandLineUtility.DYNAMIC_CG_GEN_LONG),
                        "-javaagent:" + commandLine.getOptionValue(CommandLineUtility.DYNAMIC_CG_GEN_LONG) + "=" + agentSettingFile,
                        "-noverify",
                        "-jar",
                        commandLine.getOptionValue(CommandLineUtility.APP_JAR_SHORT)
                };

                Process process = Runtime.getRuntime().exec(cmd);

                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                System.out.println("Spring application is up and running with the provided agent. " +
                        "\nSending the provided requests");
                for (CurlCmd curlCmd : requestFile.getRequests()) {
                    try {
                        System.out.println("Executing = " + Arrays.toString(curlCmd.getCurlCmd()));
                        Runtime.getRuntime().exec(curlCmd.getCurlCmd()).waitFor();
                        Thread.sleep(3000);
                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                process.destroy();
            } else if (commandLine.hasOption(CommandLineUtility.DACAPO_ARG_SHORT)) {
                String[] args = commandLine.getOptionValue(CommandLineUtility.DACAPO_ARG_LONG).split(":");

                if (args.length != 2) {
                    System.out.println("Given DACAPO argument is invalid. The valid format is <DACAPO application>:<size>");
                }

                javaCMD.append(" ").append(args[0]).append(" -s ").append(args[1]);
            } else if (commandLine.hasOption(CommandLineUtility.SINGLE_RUN_APP_SHORT)) {
                Process process = getCompleteCmd(javaCMD.toString());
                process.waitFor(30, TimeUnit.MINUTES);
            } else {
                Process process = getCompleteCmd(javaCMD.toString());



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
            }


        } catch (IOException | InterruptedException e) {
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

        while (true) {
            if (!t1.isAlive()) {
                break;
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
