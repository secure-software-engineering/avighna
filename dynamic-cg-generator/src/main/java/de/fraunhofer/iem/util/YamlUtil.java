package de.fraunhofer.iem.util;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.logging.Level;

/**
 * Utility class for Yaml
 *
 * @author Ranjith Krishnamurthy
 */
public class YamlUtil {
    /**
     * Parses the given yaml file and then returns the DynamicAgentConfiguration object
     *
     * @param yamlFileName Yaml file name
     * @return DynamicAgentConfiguration
     */
    public static DynamicAgentConfiguration parse(String yamlFileName) {
        try {
            InputStream inputStream = new FileInputStream(yamlFileName);
            Yaml yaml = new Yaml(new Constructor(DynamicAgentConfiguration.class));
            DynamicAgentConfiguration data = yaml.load(inputStream);

//            System.out.println(data.getRootPackageNameOfApplication());
//            System.out.println(data.getOutputRootDirectory());
//            System.out.println(data.isSaveCallGraphAsDotFile());
//            System.out.println(data.isSaveCallGraphAsImage());
//            System.out.println(data.getExcludeClasses());
//            System.out.println(data.getFakeEdgesString());

            return data;
        } catch (FileNotFoundException e) {
            LoggerUtil.LOGGER.log(Level.SEVERE, "FileNotFoundException = " + e.getMessage());
            System.exit(-1);
        } catch (Exception e) {
            LoggerUtil.LOGGER.log(Level.SEVERE, "Invalid key value in the given YAML file = " + e.getMessage());
            System.exit(-1);
        }

        return null;
    }
}
