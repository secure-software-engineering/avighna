package de.fraunhofer.iem.util;

import de.fraunhofer.iem.MainInterface;
import org.apache.commons.cli.*;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * Utility class for parsing command line arguments
 *
 * @author Ranjith Krishnamurthy
 */
public class CommandLineUtility {
    /**
     * Below are the command line arguments options short and long names
     */
    public static final String APP_JAR_SHORT = "aj";
    public static final String APP_JAR_LONG = "app-jar";
    public static final String DYNAMIC_CG_GEN_SHORT = "dcj";
    public static final String DYNAMIC_CG_GEN_LONG = "dynamic-cg-gen-jar";
    public static final String LIST_OF_REQUEST_SHORT = "lrf";
    public static final String LIST_OF_REQUEST_LONG = "list-request-file";
    public static final String OUT_ROOT_DIR_SHORT = "od";
    public static final String OUT_ROOT_DIR_LONG = "out-root-dir";
    public static final String ROOT_APP_PACKAGE_SHORT = "rap";
    public static final String ROOT_APP_PACKAGE_LONG = "root-app-package";
    public static final String SAVE_DOT_FILE_SHORT = "sdf";
    public static final String SAVE_DOT_FILE_LONG = "save-dot-files";
    public static final String SAVE_IMG_FILE_SHORT = "sif";
    public static final String SAVE_IMG_FILE_LONG = "save-img-files";

    private static final Options cmdOptions = new Options();
    private static CommandLine commandLine;

    /**
     * Getter for commandLine
     *
     * @return CommandLine
     */
    public static CommandLine getCommandLine() {
        return commandLine;
    }

    /**
     * Initializes the command line options.
     * <p>
     * Note: In the future, if needed to add new options, then add it here.
     */
    public static void initializeCommandLineOptions() {
        Option appJarFile = new Option(
                APP_JAR_SHORT,
                APP_JAR_LONG,
                true,
                "Path of the application jar file");
        appJarFile.setRequired(true);

        Option jarFile = new Option(
                DYNAMIC_CG_GEN_SHORT,
                DYNAMIC_CG_GEN_LONG,
                true,
                "Path of the dynamic cg generator jar file");
        jarFile.setRequired(true);

        Option reqFile = new Option(
                LIST_OF_REQUEST_SHORT,
                LIST_OF_REQUEST_LONG,
                true,
                "Path of the YAML file that contains the list of requests. Request must be full curl command");
        reqFile.setRequired(true);

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
                "Root application package for generating the dynamic cg. For multiple use the ; as separator.");
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

        cmdOptions.addOption(appJarFile);
        cmdOptions.addOption(jarFile);
        cmdOptions.addOption(reqFile);
        cmdOptions.addOption(outDir);
        cmdOptions.addOption(rootAppPackage);
        cmdOptions.addOption(saveDotFiles);
        cmdOptions.addOption(saveImgFiles);
    }

    /**
     * Parses the given command line arguments and stores the parsed command line arguments in commandLine static variable
     *
     * @param args Command line arguments
     */
    public static void parseCommandLineArguments(String[] args) {
        // Initialize the command line options

        CommandLineParser commandLineParser = new DefaultParser();
        HelpFormatter helpFormatter = new HelpFormatter();

        try {
            // Parse the command line arguments
            commandLine = commandLineParser.parse(cmdOptions, args);
        } catch (ParseException ex) {
            helpFormatter.printHelp("dynamic-cg-interface", cmdOptions);
            System.exit(-1);
        }
    }

    /**
     * Validates the parsed command line arguments
     */
    public static void validateCommandLineOptions() {
        String appJarFilePath = commandLine.getOptionValue(CommandLineUtility.APP_JAR_LONG);
        String jarFilePath = commandLine.getOptionValue(CommandLineUtility.DYNAMIC_CG_GEN_LONG);
        String reqFilePath = commandLine.getOptionValue(CommandLineUtility.LIST_OF_REQUEST_LONG);
        String outDirPath = commandLine.getOptionValue(CommandLineUtility.OUT_ROOT_DIR_LONG);

        // Verify given app jar file path
        File appJarFile = new File(jarFilePath);

        if (appJarFile.exists()) {
            if (appJarFile.isFile()) {
                if (!appJarFile.getName().endsWith(".jar")) {
                    System.out.println("Given application jar file (" + appJarFilePath + ") is invalid");
                    System.exit(-1);
                }
            } else {
                System.out.println("Given application jar file (" + appJarFilePath + ") is not a file");
                System.exit(-1);
            }
        } else {
            System.out.println("Given application jar file (" + appJarFilePath + ") does not exist");
            System.exit(-1);
        }

        // Verify given dynamic-cg-generator jar file path
        File jarFile = new File(jarFilePath);

        if (jarFile.exists()) {
            if (jarFile.isFile()) {
                if (!jarFile.getName().endsWith(".jar")) {
                    System.out.println("Given dynamic-cg-generator jar file (" + jarFilePath + ") is invalid");
                    System.exit(-1);
                }
            } else {
                System.out.println("Given dynamic-cg-generator jar file (" + jarFilePath + ") is not a file");
                System.exit(-1);
            }
        } else {
            System.out.println("Given dynamic-cg-generator jar file (" + jarFilePath + ") does not exist");
            System.exit(-1);
        }

        //Verify given request file path and set the requestFile
        MainInterface.requestFile.setRequests(YamlUtility.parseRequestYamlFile(reqFilePath).getRequests());

        // Verify given output dir
        File outDir = new File(outDirPath);

        if (outDir.exists()) {
            if (outDir.isFile()) {
                System.out.println("Given root out directory (" + jarFilePath + ") is not a directory");
                System.exit(-1);
            } else {
                try {
                    FileUtils.deleteDirectory(outDir);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            if (!outDir.mkdirs()) {
                System.out.println("Given root out directory (" + jarFilePath + ") does not exist and we could not create one");
                System.exit(-1);
            }
        }
    }
}
