package de.fraunhofer.iem.util;

import de.fraunhofer.iem.MainInterface;
import org.apache.commons.cli.*;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

public class CommandLineUtility {
    /**
     * Below are the command line arguments options short and long names
     */
    public static final String APP_JAR_SHORT = "aj";
    public static final String APP_JAR_LONG = "app-jar";

    public static final String APP_ARG_SHORT = "aa";
    public static final String APP_ARG_LONG = "app-arg";

    public static final String SINGLE_RUN_APP_SHORT = "sra";
    public static final String SINGLE_RUN_APP_LONG = "single-run-app";

    public static final String DACAPO_ARG_SHORT = "dar";
    public static final String DACAPO_ARG_LONG = "dacapo-app-arg";
    public static final String DYNAMIC_CG_GEN_SHORT = "aaj";
    public static final String DYNAMIC_CG_GEN_LONG = "avighna-agent-jar";
    public static final String LIST_OF_REQUEST_SHORT = "lrf";
    public static final String LIST_OF_REQUEST_LONG = "list-request-file";
    public static final String OUT_ROOT_DIR_SHORT = "od";
    public static final String OUT_ROOT_DIR_LONG = "out-root-dir";
    public static final String ROOT_APP_PACKAGE_SHORT = "rap";
    public static final String ROOT_APP_PACKAGE_LONG = "root-app-packages";
    public static final String SAVE_DOT_FILE_SHORT = "sdf";
    public static final String SAVE_DOT_FILE_LONG = "save-dot-files";
    public static final String SAVE_IMG_FILE_SHORT = "sif";
    public static final String SAVE_IMG_FILE_LONG = "save-img-files";
    public static final String DONT_TRACK_FAKE_EDGE_SHORT = "dtf";
    public static final String DONT_TRACK_FAKE_EDGE_LONG = "dont-track-fake-edge";

    private static final Options cmdOptions = new Options();
    private static CommandLine commandLine;

    public static CommandLine getCommandLine() {
        return commandLine;
    }

    /**
     * Initializes the command line options.
     * <p>
     * Note: In future, if needed to add new options, then add it here.
     *
     * @return Command line options
     */
    public static void initializeCommandLineOptions() {
        Option appJarFile = new Option(
                APP_JAR_SHORT,
                APP_JAR_LONG,
                true,
                "Path of the application jar file to generate dynamic information");
        appJarFile.setRequired(true);

        Option appArg = new Option(
                APP_ARG_SHORT,
                APP_ARG_LONG,
                true,
                "Program arguments for the given application. If multiple arguments then separate it by :");

        Option singleRunApp = new Option(
                SINGLE_RUN_APP_SHORT,
                SINGLE_RUN_APP_LONG,
                false,
                "This argument indicates that the given app is a single run and not the web application which runs infinitely until explicitly terminated.");

        Option jarFile = new Option(
                DYNAMIC_CG_GEN_SHORT,
                DYNAMIC_CG_GEN_LONG,
                true,
                "Path of the avighna agent jar file");
        jarFile.setRequired(true);

        Option dacapoAppArg = new Option(
                DACAPO_ARG_SHORT,
                DACAPO_ARG_LONG,
                true,
                "Arguments to the DACAPO application. The value format is <dacapo application>:<data size>. These provided arguments will be appended after the given application jar file to the final command");
        appJarFile.setRequired(false);

        Option reqFile = new Option(
                LIST_OF_REQUEST_SHORT,
                LIST_OF_REQUEST_LONG,
                true,
                "Path of the YAML file that contains the list of requests. Request must be full curl command");

        Option outDir = new Option(
                OUT_ROOT_DIR_SHORT,
                OUT_ROOT_DIR_LONG,
                true,
                "Root directory to store the output");
        outDir.setRequired(true);

        Option rootAppPackage = new Option(
                ROOT_APP_PACKAGE_SHORT,
                ROOT_APP_PACKAGE_LONG,
                true,
                "Root application package(s) for generating the dynamic cg. Multiple packages are seperated by :");
        rootAppPackage.setRequired(true);

        Option saveDotFiles = new Option(
                SAVE_DOT_FILE_SHORT,
                SAVE_DOT_FILE_LONG,
                false,
                "Save the generated dynamic trace as dot files"
        );

        Option saveImgFiles = new Option(
                SAVE_IMG_FILE_SHORT,
                SAVE_IMG_FILE_LONG,
                false,
                "Save the generated dynamic trace as image files"
        );

        Option trackFakeEdge = new Option(
                DONT_TRACK_FAKE_EDGE_SHORT,
                DONT_TRACK_FAKE_EDGE_LONG,
                false,
                "Dont track fake edges while generating dynamic traces"
        );

        cmdOptions.addOption(appJarFile);
        cmdOptions.addOption(appArg);
        cmdOptions.addOption(singleRunApp);
        cmdOptions.addOption(jarFile);
        cmdOptions.addOption(dacapoAppArg);
        cmdOptions.addOption(reqFile);
        cmdOptions.addOption(outDir);
        cmdOptions.addOption(rootAppPackage);
        cmdOptions.addOption(saveDotFiles);
        cmdOptions.addOption(saveImgFiles);
        cmdOptions.addOption(trackFakeEdge);
    }

    public static void getCommandLineOptions(String[] args) {
        // Initialize the command line options

        CommandLineParser commandLineParser = new DefaultParser();
        HelpFormatter helpFormatter = new HelpFormatter();

        try {
            // Parse the command line arguments
            commandLine = commandLineParser.parse(cmdOptions, args);
        } catch (ParseException ex) {
            helpFormatter.printHelp("avighna-cmd-interface", cmdOptions);
            System.exit(-1);
        }
    }

    public static void validateCommandLineOptions() {
        LoggerUtil.getLOGGER().info("Validating provided commandline options.");

        String appJarFilePath = commandLine.getOptionValue(CommandLineUtility.APP_JAR_LONG);
        String jarFilePath = commandLine.getOptionValue(CommandLineUtility.DYNAMIC_CG_GEN_LONG);
        String outDirPath = commandLine.getOptionValue(CommandLineUtility.OUT_ROOT_DIR_LONG);

        // Verify given app jar file path
        File appJarFile = new File(appJarFilePath);

        if (appJarFile.exists()) {
            if (appJarFile.isFile()) {
                if (!appJarFile.getName().endsWith(".jar")) {
                    LoggerUtil.getLOGGER().severe("Given application jar file (" + appJarFilePath + ") is invalid");
                    System.exit(-1);
                }
            } else {
                LoggerUtil.getLOGGER().severe("Given application jar file (" + appJarFilePath + ") is not a file");
                System.exit(-1);
            }
        } else {
            LoggerUtil.getLOGGER().severe("Given application jar file (" + appJarFilePath + ") does not exist");
            System.exit(-1);
        }

        // Verify given jar file path
        File jarFile = new File(jarFilePath);

        if (jarFile.exists()) {
            if (jarFile.isFile()) {
                if (!jarFile.getName().endsWith(".jar")) {
                    LoggerUtil.getLOGGER().severe("Given avighna-agent jar file (" + jarFilePath + ") is invalid");
                    System.exit(-1);
                }
            } else {
                LoggerUtil.getLOGGER().severe("Given avighna-agent jar file (" + jarFilePath + ") is not a file");
                System.exit(-1);
            }
        } else {
            LoggerUtil.getLOGGER().severe("Given avighna-agent jar file (" + jarFilePath + ") does not exist");
            System.exit(-1);
        }

        // Verify given output dir
        File outDir = new File(outDirPath);

        if (outDir.exists()) {
            if (outDir.isFile()) {
                LoggerUtil.getLOGGER().severe("Given root out directory (" + outDir + ") is not a directory");
                System.exit(-1);
            } else {
                try {
                    FileUtils.deleteDirectory(outDir);
                } catch (IOException e) {
                    LoggerUtil.getLOGGER().severe("Given root out directory (" + outDir + ") already exists and failed to delete this directory.");
                    System.exit(-1);
                }
            }
        }

        if (!outDir.mkdirs()) {
            LoggerUtil.getLOGGER().severe("Given root out directory (" + outDir + ") does not exist and we could not create one.");
            System.exit(-1);
        }

        // Verify given request yaml file
        if (CommandLineUtility.getCommandLine().hasOption(CommandLineUtility.LIST_OF_REQUEST_LONG)) {
            String requestFilePath = CommandLineUtility.getCommandLine().getOptionValue(CommandLineUtility.LIST_OF_REQUEST_LONG);
            File requestFile = new File(requestFilePath);

            if (requestFile.exists()) {
                if (requestFile.isFile()) {
                    if (!requestFile.getName().endsWith(".yaml") && !requestFile.getName().endsWith(".yml")) {
                        LoggerUtil.getLOGGER().severe("Given request file (" + requestFilePath + ") is not yaml file");
                        System.exit(-1);
                    }
                } else {
                    LoggerUtil.getLOGGER().severe("Given request file (" + requestFilePath + ") is not a file");
                    System.exit(-1);
                }
            } else {
                LoggerUtil.getLOGGER().severe("Given request file (" + requestFilePath + ") does not exist");
                System.exit(-1);
            }

            //Verify given request file path and set the requestFile
            MainInterface.requestFile.setRequests(YamlUtility.parseRequestYamlFile(requestFilePath).getRequests());
        }
    }
}
