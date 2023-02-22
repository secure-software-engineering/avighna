package de.fraunhofer.iem;

import de.fraunhofer.iem.util.*;
import org.apache.commons.cli.CommandLine;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class MainInterface {
    public static final RequestFile requestFile = new RequestFile();

    private static String agentSettingFile;

    private static boolean isInterrupted = false;
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_GREEN = "\u001B[32m";

    private static void runApplicationWithJavaAgent(CommandLine commandLine) {
        LoggerUtil.getLOGGER().info("Running the provided application by attaching the avighna-agent. Below is the command.");

        try {
            ArrayList<String> cmd = new ArrayList<>();

            cmd.add("java");
            cmd.add("-Xbootclasspath/p:" + commandLine.getOptionValue(CommandLineUtility.DYNAMIC_CG_GEN_LONG));
            cmd.add("-javaagent:" + commandLine.getOptionValue(CommandLineUtility.DYNAMIC_CG_GEN_LONG) + "=" + agentSettingFile);
            cmd.add("-noverify");
            cmd.add("-jar");
            cmd.add(commandLine.getOptionValue(CommandLineUtility.APP_JAR_SHORT));

            if (commandLine.hasOption(CommandLineUtility.APP_ARG_SHORT)) {
                cmd.addAll(Arrays.asList(commandLine.getOptionValue(CommandLineUtility.APP_ARG_SHORT).split(":")));
            }

            String[] javaCmdArr = cmd.toArray(new String[]{});

            System.out.println(
                    Arrays.toString(javaCmdArr)
                            .replaceAll(", ", " ")
                            .replace("[", "")
                            .replace("]", "")
            );

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
                // This if block is: If the list of requests are provided, then automatically send the requests to the application

                Process process = Runtime.getRuntime().exec(javaCmdArr);

                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    LoggerUtil.getLOGGER().warning("Sleep Interrupted");
                }

                LoggerUtil.getLOGGER().info("Spring application is up and running with the provided agent. " +
                        "Sending the provided requests");

                // Start sending the requests
                for (CurlCmd curlCmd : requestFile.getRequests()) {
                    try {
                        LoggerUtil.getLOGGER().info("Executing = " + Arrays.toString(curlCmd.getCurlCmd()));
                        Process pr = Runtime.getRuntime().exec(curlCmd.getCurlCmd());

                        // Curl command's output
                        BufferedReader in = new BufferedReader(new InputStreamReader(pr.getInputStream()));
                        String line;
                        System.out.println(ANSI_GREEN + "Output:" + ANSI_RESET);
                        while ((line = in.readLine()) != null) {
                            System.out.println(ANSI_GREEN + line + ANSI_RESET);
                        }

                        // Curl's commands error
                        BufferedReader in2 = new BufferedReader(new InputStreamReader(pr.getErrorStream()));
                        String line2;
                        System.out.println("\nError:");
                        while ((line2 = in2.readLine()) != null) {
                            System.out.println(line2);
                        }

                        // wait for the curl command to finish
                        pr.waitFor();

                        // Sleep for sometime before move on to next curl command
                        Thread.sleep(3000);
                    } catch (IOException | InterruptedException e) {
                        LoggerUtil.getLOGGER().warning("Something went wrong with " + curlCmd + " = " + e.getMessage());
                    }
                }

                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    LoggerUtil.getLOGGER().warning("Sleep Interrupted");
                }

                // Destroy the process so that avighna-agent stores the data through shutDownHook
                process.destroy();
            } else if (commandLine.hasOption(CommandLineUtility.DACAPO_ARG_SHORT)) {
                // This if block is: for Dacapo

                String[] args = commandLine.getOptionValue(CommandLineUtility.DACAPO_ARG_LONG).split(":");

                if (args.length != 2) {
                    System.out.println("Given DACAPO argument is invalid. The valid format is <DACAPO application>:<size>");
                }

                javaCMD.append(" ").append(args[0]).append(" -s ").append(args[1]);
            } else if (commandLine.hasOption(CommandLineUtility.SINGLE_RUN_APP_SHORT)) {
                // This if block is: if the provided application run once and terminate automatically.

                Process process = Runtime.getRuntime().exec(javaCmdArr);

                // Wait for the application max 30 minutes.
                process.waitFor(30, TimeUnit.MINUTES);
            } else {
                // This else block is for application that runs infinitely and user can send the requests through browser

                Process process = Runtime.getRuntime().exec(javaCmdArr);

                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    LoggerUtil.getLOGGER().warning("Sleep Interrupted");
                }


                LoggerUtil.getLOGGER().info("Application is running by attaching the avighna-agent. " +
                        "If the application is web then, please open browser and run different requests");
                LoggerUtil.getLOGGER().info("A separate terminal is opened, where user can interact with the application." +
                        " Once complete with the testing, send CTRL + C to the application so that shutDownHook will be executed.");

                String closeApp = "n";

                do {
                    LoggerUtil.getLOGGER().info("Completed by sending CTRL + C? Should we terminate the application and generate DTS file? (y/Y/yes/Yes)  ");
                    Scanner scanner = new Scanner(System.in);

                    closeApp = scanner.nextLine();

                    if (closeApp.toLowerCase().equals("y") || closeApp.toLowerCase().equals("yes")) {
                        process.destroy();
                    }

                    if (closeApp == null || closeApp.equals("")) {
                        closeApp = "n";
                    }
                } while (!closeApp.toLowerCase().equals("y") && !closeApp.toLowerCase().equals("yes"));
            }


        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        System.out.println("\n\n\n" + ConsoleStyling.YELLOW + "    ___        _       __               \n" +
                "   /   |_   __(_)___ _/ /_  ____  ____ _\n" +
                "  / /| | | / / / __ `/ __ \\/ __ \\/ __ `/\n" +
                " / ___ | |/ / / /_/ / / / / / / / /_/ / \n" +
                "/_/  |_|___/_/\\__, /_/ /_/_/ /_/\\__,_/  \n" +
                "             /____/     " + ConsoleStyling.RED + ConsoleStyling.ITALICS + "Cmd-Interface   " + ConsoleStyling.RESET + "\n\n\n");

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

        LoggerUtil.getLOGGER().info("Generating DST file.");
        ZipUtil.generateDst(CommandLineUtility.getCommandLine());

        if (CommandLineUtility.getCommandLine().hasOption(CommandLineUtility.SAVE_IMG_FILE_LONG)) {
            DotToImgUtil.generateImageFromDot();
        }

        LoggerUtil.getLOGGER().info("Finishing Avighna.");

        System.out.println("\n\n\n" + ConsoleStyling.YELLOW + "    ___        _       __               \n" +
                "   /   |_   __(_)___ _/ /_  ____  ____ _\n" +
                "  / /| | | / / / __ `/ __ \\/ __ \\/ __ `/\n" +
                " / ___ | |/ / / /_/ / / / / / / / /_/ / \n" +
                "/_/  |_|___/_/\\__, /_/ /_/_/ /_/\\__,_/  \n" +
                "             /____/     " + ConsoleStyling.RED + ConsoleStyling.ITALICS + "Cmd-Interface   " + ConsoleStyling.RESET + "\n\n\n");
    }
}
