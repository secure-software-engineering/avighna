package de.fraunhofer.iem;

import de.fraunhofer.iem.util.DynamicAgentConfiguration;
import de.fraunhofer.iem.util.LoggerUtil;
import de.fraunhofer.iem.util.YamlUtil;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.net.JarURLConnection;
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
        LoggerUtil.getLOGGER().info("Dynamic Agent started");
        LoggerUtil.getLOGGER().info("Loading the given YAML settings file: " + argument);

        // Load the YAML settings file
        dynamicAgentConfiguration = YamlUtil.parse(argument);

        //add jar class loader
        try {
            JarURLConnection connection = (JarURLConnection) DynamicCGAgent.class.getResource("DynamicCGAgent.class").openConnection();
            instrumentation.appendToBootstrapClassLoaderSearch(connection.getJarFile());
        } catch (IOException e) {
            LoggerUtil.getLOGGER().log(Level.SEVERE, "Something went wrong while setting the class loader = " + e.getMessage());
            System.exit(-1);
        }


        LoggerUtil.getLOGGER().info("Checking the given root output directory = " + DynamicCGAgent.dynamicAgentConfiguration.getOutputRootDirectory());
        File rootOutputDirectory = new File(dynamicAgentConfiguration.getOutputRootDirectory());

        if (rootOutputDirectory.exists()) {
            if (!rootOutputDirectory.isDirectory()) {
                LoggerUtil.getLOGGER().log(Level.SEVERE, "Given output root directory (" + dynamicAgentConfiguration.getOutputRootDirectory() + ") is not valid");
                System.exit(-1);
            }

            LoggerUtil.getLOGGER().info("The given root output directory exists");
        } else {
            rootOutputDirectory.mkdirs();
            LoggerUtil.getLOGGER().info("The given root output directory doesn't exists. Creating the directory (" + dynamicAgentConfiguration.getOutputRootDirectory() + ")");
        }

        // Statistics file
        String statFileName = rootOutputDirectory.getAbsolutePath().toString() + System.getProperty("file.separator") + "stats.txt";
        LoggerUtil.getLOGGER().info("Creating the statistics file = " + statFileName);

        try {
            File file = new File(statFileName);
            FileWriter fileWriter = new FileWriter(file);

            BufferedWriter out = new BufferedWriter(fileWriter);
            out.write("Dynamic Call Stack:\n");
            out.close();
        } catch (IOException e) {
            LoggerUtil.getLOGGER().log(Level.SEVERE, "Could not create statistics file = " + e.getMessage());
            System.exit(-1);
        }

        // Error file
        String errorFileName = rootOutputDirectory.getAbsolutePath().toString() + System.getProperty("file.separator") + "error.txt";
        LoggerUtil.getLOGGER().info("Creating the error file = " + errorFileName);
        try {
            File file = new File(errorFileName);
            FileWriter fileWriter = new FileWriter(file);

            BufferedWriter out = new BufferedWriter(fileWriter);
            out.write("Dynamic Call Stack (ERROR):\n");
            out.close();
        } catch (IOException e) {
            LoggerUtil.getLOGGER().log(Level.SEVERE, "Could not create error file = " + e.getMessage());
            System.exit(-1);
        }

        String applicationRootPackage = dynamicAgentConfiguration.getRootPackageNameOfApplication().replaceAll("\\.", "/") + "/";

        LoggerUtil.getLOGGER().info("Root application package name = " + applicationRootPackage);
        LoggerUtil.getLOGGER().info("Save Dynamic CG as DOT file? = " + dynamicAgentConfiguration.isSaveCallGraphAsDotFile());
        LoggerUtil.getLOGGER().info("Excluded classes = " + dynamicAgentConfiguration.getExcludeClasses().toString()
                .replace("[", "\n")
                .replaceAll(", ", "\n")
                .replaceAll("]", ""));
        LoggerUtil.getLOGGER().info("Fake edges = " + dynamicAgentConfiguration.getFakeEdgesString().toString()
                .replace("[", "\n")
                .replaceAll(", ", "\n")
                .replaceAll("]", ""));

        DynamicCallStack.outputRootDirectory = dynamicAgentConfiguration.getOutputRootDirectory();
        DynamicCallStack.fakeEdges.addAll(dynamicAgentConfiguration.getFakeEdgesString());
        DynamicCallStack.saveCallGraphAsDotFile = dynamicAgentConfiguration.isSaveCallGraphAsDotFile();

        LoggerUtil.getLOGGER().info("Instrumentation begins");
        instrumentation.addTransformer(new AgentTransformer(applicationRootPackage, dynamicAgentConfiguration.getOutputRootDirectory(), dynamicAgentConfiguration.getExcludeClasses(), dynamicAgentConfiguration.getFakeEdgesString()));
    }

    /**
     * Agent-main
     *
     * @param argument        Command argument
     * @param instrumentation Instrumentation
     */
    public static void agentmain(String argument, Instrumentation instrumentation) {
        LoggerUtil.getLOGGER().info("Started agent-main");
    }
}
