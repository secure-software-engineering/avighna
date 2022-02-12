package de.fraunhofer.iem;

import de.fraunhofer.iem.util.DynamicAgentConfiguration;
import de.fraunhofer.iem.util.YamlUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.net.JarURLConnection;

/**
 * Agent for generating Dynamic CG
 *
 * @author Ranjith Krishnamurthy
 */
public class DynamicCGAgent {
    private static final Logger LOGGER = LogManager.getLogger(DynamicCGAgent.class);

    public static DynamicAgentConfiguration dynamicAgentConfiguration;

    /**
     * Pre-main
     *
     * @param argument        Command argument
     * @param instrumentation Instrumentation
     */
    public static void premain(String argument, Instrumentation instrumentation) {
        LOGGER.info("Dynamic Agent started");
        LOGGER.info("Loading the given YAML settings file: " + argument);

        // Load the YAML settings file
        dynamicAgentConfiguration = YamlUtil.parse(argument);

        //add jar class loader
        try {
            JarURLConnection connection = (JarURLConnection) DynamicCGAgent.class.getResource("DynamicCGAgent.class").openConnection();
            instrumentation.appendToBootstrapClassLoaderSearch(connection.getJarFile());
        } catch (IOException e) {
            LOGGER.error("Something went wrong while setting the class loader = " + e.getMessage());
            System.exit(-1);
        }


        LOGGER.info("Checking the given root output directory = " + DynamicCGAgent.dynamicAgentConfiguration.getOutputRootDirectory());
        File rootOutputDirectory = new File(dynamicAgentConfiguration.getOutputRootDirectory());

        if (rootOutputDirectory.exists()) {
            if (!rootOutputDirectory.isDirectory()) {
                LOGGER.error("Given output root directory (" +
                        dynamicAgentConfiguration.getOutputRootDirectory() + ") is not valid");
                System.exit(-1);
            }

            LOGGER.info("The given root output directory exists");
        } else {
            rootOutputDirectory.mkdirs();
            LOGGER.info("The given root output directory doesn't exists. Creating the directory (" +
                    dynamicAgentConfiguration.getOutputRootDirectory() + ")");
        }

        // Statistics file
        String statFileName = rootOutputDirectory.getAbsolutePath().toString() +
                System.getProperty("file.separator") + "stats.txt";
        LOGGER.info("Creating the statistics file = " + statFileName);

        try {
            File file = new File(statFileName);
            FileWriter fileWriter = new FileWriter(file);

            BufferedWriter out = new BufferedWriter(fileWriter);
            out.write("Dynamic Call Stack:\n");
            out.close();
        } catch (IOException e) {
            LOGGER.error("Could not create statistics file = " + e.getMessage());
            System.exit(-1);
        }

        // Error file
        String errorFileName = rootOutputDirectory.getAbsolutePath().toString() +
                System.getProperty("file.separator") + "error.txt";
        LOGGER.info("Creating the error file = " + errorFileName);
        try {
            File file = new File(errorFileName);
            FileWriter fileWriter = new FileWriter(file);

            BufferedWriter out = new BufferedWriter(fileWriter);
            out.write("Dynamic Call Stack (ERROR):\n");
            out.close();
        } catch (IOException e) {
            LOGGER.error("Could not create error file = " + e.getMessage());
            System.exit(-1);
        }

        LOGGER.info("Root application package name  = " + dynamicAgentConfiguration.getRootPackageNameOfApplication());

        String applicationRootPackage = dynamicAgentConfiguration
                .getRootPackageNameOfApplication().replaceAll("\\.", "/") + "/";

        LOGGER.info("Instrumentation begins");
        instrumentation.addTransformer(new AgentTransformer(applicationRootPackage));
    }

    /**
     * Agent-main
     *
     * @param argument        Command argument
     * @param instrumentation Instrumentation
     */
    public static void agentmain(String argument, Instrumentation instrumentation) {
        LOGGER.info("Started agent-main");
    }
}
