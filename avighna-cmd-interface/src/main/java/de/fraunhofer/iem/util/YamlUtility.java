package de.fraunhofer.iem.util;

import de.fraunhofer.iem.MainInterface;
import org.apache.commons.cli.CommandLine;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Utility class for Yaml file
 *
 * @author Ranjith Krishnamurthy
 */
public class YamlUtility {
    /**
     * Parses the given yaml file and then returns the RequestFile object
     *
     * @param yamlFileName Yaml file name of a request yaml file
     * @return RequestFile
     */
    public static RequestFile parseRequestYamlFile(String yamlFileName) {
        try {
            InputStream inputStream = new FileInputStream(yamlFileName);
            Yaml yaml = new Yaml(new Constructor(RequestFile.class));

            return yaml.load(inputStream);
        } catch (FileNotFoundException e) {
            LoggerUtil.getLOGGER().severe("FileNotFoundException = " + e.getMessage());
            System.exit(-1);
        } catch (Exception e) {
            LoggerUtil.getLOGGER().severe("Invalid key value in the given YAML file = " + e.getMessage());
            System.exit(-1);
        }

        return null;
    }

    /**
     * Generates the Yaml settings file for dynamic agent
     *
     * @param commandLine Command Line options
     * @return Path of the generates settings file
     */
    public static String generateAgentSettingsFile(CommandLine commandLine) {
        LoggerUtil.getLOGGER().info("Generating avighna-agent configuration file.");

        DumperOptions options = new DumperOptions();
        options.setIndent(2);
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setIndicatorIndent(2);
        options.setIndentWithIndicator(true);
        options.setPrettyFlow(true);

        Representer representer = new Representer();
        representer.addClassTag(DynamicAgentConfiguration.class, Tag.MAP);
        Yaml yaml = new Yaml(representer, options);

        DynamicAgentConfiguration dynamicAgentConfiguration = new DynamicAgentConfiguration();

        String rootPackages = commandLine.getOptionValue(CommandLineUtility.ROOT_APP_PACKAGE_SHORT);
        List<String> rootPackageNames = Arrays.asList(rootPackages.split(":"));
        dynamicAgentConfiguration.setRootPackageNameOfApplication(rootPackageNames);

        File file = new File(commandLine.getOptionValue(CommandLineUtility.OUT_ROOT_DIR_SHORT) + File.separator + "allDotFiles");

        if (file.exists()) {
            file.delete();
        }

        file.mkdirs();

        File file1 = new File(commandLine.getOptionValue(CommandLineUtility.OUT_ROOT_DIR_SHORT) + File.separator + "dynamicCP");

        if (file1.exists()) {
            file1.delete();
        }

        file1.mkdirs();

        dynamicAgentConfiguration.setOutputRootDirectory(file.getAbsolutePath());
        dynamicAgentConfiguration.setSaveCallGraphAsDotFile(commandLine.hasOption(CommandLineUtility.SAVE_DOT_FILE_LONG));

        List<String> excludeClasses = new ArrayList<>();
        excludeClasses.add("java/");
        //TODO Handle this
        excludeClasses.add("de/fraunhofer/iem/springbench/bean/configurations/MyConfiguration$$EnhancerBySpringCGLIB");

        dynamicAgentConfiguration.setExcludeClasses(excludeClasses);

        // TODO: decide whether to take this from the user
        List<String> fakeEdgesString = new ArrayList<>();
        fakeEdgesString.add("$$EnhancerBySpringCGLIB$$");
        fakeEdgesString.add("$$FastClassBySpringCGLIB$$");
        fakeEdgesString.add("$$KeyFactoryByCGLIB$$");

        fakeEdgesString.add("$$FastClassByGuice$$");

        dynamicAgentConfiguration.setFakeEdgesString(fakeEdgesString);
        dynamicAgentConfiguration.setTrackEdges(!commandLine.hasOption(CommandLineUtility.DONT_TRACK_FAKE_EDGE_LONG));

        File settingsFile = new File(commandLine.getOptionValue(CommandLineUtility.OUT_ROOT_DIR_SHORT) + File.separator + "dynamic_agent.yml");

        try {
            yaml.dump(dynamicAgentConfiguration, new FileWriter(settingsFile));
        } catch (IOException e) {
            LoggerUtil.getLOGGER().severe("Could not dump the avighna-agent configuration to a yaml file = " + e.getMessage());
            System.exit(-1);
        }

        return settingsFile.getAbsolutePath();
    }
}
