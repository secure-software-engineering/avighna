package de.fraunhofer.iem;

import de.fraunhofer.iem.util.DynamicAgentConfiguration;
import de.fraunhofer.iem.util.LogFormatter;
import de.fraunhofer.iem.util.LoggerUtil;
import de.fraunhofer.iem.util.YamlUtil;

import java.io.*;
import java.lang.instrument.Instrumentation;
import java.net.JarURLConnection;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;

/**
 * Agent for generating Dynamic CG
 *
 * @author Ranjith Krishnamurthy
 */
public class DynamicCGAgent {
    public static DynamicAgentConfiguration dynamicAgentConfiguration;

    /**
     * Pre-main
     *
     * @param argument        Command argument
     * @param instrumentation Instrumentation
     */
    public static void premain(String argument, Instrumentation instrumentation) {
        InputStream stream = LoggerUtil.class.getClassLoader().
                getResourceAsStream("logging.properties");
        try {
            java.util.logging.LogManager.getLogManager().readConfiguration(stream);
            LoggerUtil.LOGGER = java.util.logging.Logger.getLogger(DynamicCallStackManager.class.getName());

            LoggerUtil.LOGGER.setUseParentHandlers(false);

            ConsoleHandler handler = new ConsoleHandler();

            Formatter formatter = new LogFormatter();
            handler.setFormatter(formatter);

            LoggerUtil.LOGGER.addHandler(handler);
        } catch (IOException e) {
            e.printStackTrace();
        }

        LoggerUtil.LOGGER.info("Dynamic Agent started");
        LoggerUtil.LOGGER.info("Loading the given YAML settings file: " + argument);

        // Load the YAML settings file
        dynamicAgentConfiguration = YamlUtil.parse(argument);

        //add jar class loader
        try {
            JarURLConnection connection = (JarURLConnection) DynamicCGAgent.class.getResource("DynamicCGAgent.class").openConnection();
            instrumentation.appendToBootstrapClassLoaderSearch(connection.getJarFile());
        } catch (IOException e) {
            LoggerUtil.LOGGER.log(Level.SEVERE, "Something went wrong while setting the class loader = " + e.getMessage());
            System.exit(-1);
        }


        LoggerUtil.LOGGER.info("Checking the given root output directory = " + DynamicCGAgent.dynamicAgentConfiguration.getOutputRootDirectory());
        File rootOutputDirectory = new File(dynamicAgentConfiguration.getOutputRootDirectory());

        if (rootOutputDirectory.exists()) {
            if (!rootOutputDirectory.isDirectory()) {
                LoggerUtil.LOGGER.log(Level.SEVERE, "Given output root directory (" +
                        dynamicAgentConfiguration.getOutputRootDirectory() + ") is not valid");
                System.exit(-1);
            }

            LoggerUtil.LOGGER.info("The given root output directory exists");
        } else {
            rootOutputDirectory.mkdirs();
            LoggerUtil.LOGGER.info("The given root output directory doesn't exists. Creating the directory (" +
                    dynamicAgentConfiguration.getOutputRootDirectory() + ")");
        }

        // Statistics file
        String statFileName = rootOutputDirectory.getAbsolutePath().toString() +
                System.getProperty("file.separator") + "stats.txt";
        LoggerUtil.LOGGER.info("Creating the statistics file = " + statFileName);

        try {
            File file = new File(statFileName);
            FileWriter fileWriter = new FileWriter(file);

            BufferedWriter out = new BufferedWriter(fileWriter);
            out.write("Dynamic Call Stack:\n");
            out.close();
        } catch (IOException e) {
            LoggerUtil.LOGGER.log(Level.SEVERE, "Could not create statistics file = " + e.getMessage());
            System.exit(-1);
        }

        // Error file
        String errorFileName = rootOutputDirectory.getAbsolutePath().toString() +
                System.getProperty("file.separator") + "error.txt";
        LoggerUtil.LOGGER.info("Creating the error file = " + errorFileName);
        try {
            File file = new File(errorFileName);
            FileWriter fileWriter = new FileWriter(file);

            BufferedWriter out = new BufferedWriter(fileWriter);
            out.write("Dynamic Call Stack (ERROR):\n");
            out.close();
        } catch (IOException e) {
            LoggerUtil.LOGGER.log(Level.SEVERE, "Could not create error file = " + e.getMessage());
            System.exit(-1);
        }

        LoggerUtil.LOGGER.info("Root application package name  = " + dynamicAgentConfiguration.getRootPackageNameOfApplication());

        String applicationRootPackage = dynamicAgentConfiguration
                .getRootPackageNameOfApplication().replaceAll("\\.", "/") + "/";

        LoggerUtil.LOGGER.info("Instrumentation begins");
        instrumentation.addTransformer(new AgentTransformer(applicationRootPackage));
    }

    /**
     * Agent-main
     *
     * @param argument        Command argument
     * @param instrumentation Instrumentation
     */
    public static void agentmain(String argument, Instrumentation instrumentation) {
        LoggerUtil.LOGGER.info("Started agent-main");
    }
}
