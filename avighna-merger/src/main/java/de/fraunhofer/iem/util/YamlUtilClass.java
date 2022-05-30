package de.fraunhofer.iem.util;


import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Utility class for Yaml file
 *
 * @author Ranjith Krishnamurthy
 */
public class YamlUtilClass {
    /**
     * Generates the HybridCGStats file
     *
     * @param hybridCGStats HybridCGStats
     * @return Path of the generated HybridCGStats file
     */
    public static String generateStatsFile(HybridCGStats hybridCGStats, String rootOutputDir) {
        DumperOptions options = new DumperOptions();
        options.setIndent(2);
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setIndicatorIndent(2);
        options.setIndentWithIndicator(true);
        options.setPrettyFlow(true);

        Representer representer = new Representer();
        representer.addClassTag(HybridCGStats.class, Tag.MAP);
        Yaml yaml = new Yaml(representer, options);

        File settingsFile = new File(rootOutputDir + "hybrid_stats.yml");

        try {
            yaml.dump(hybridCGStats, new FileWriter(settingsFile));
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        return settingsFile.getAbsolutePath();
    }
}
