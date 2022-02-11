package de.fraunhofer.iem.hybridCG;

import java.io.File;

/**
 * //TODO: Remove this after testing
 *
 * @author Ranjith Krishnamurthy
 */
public class CommandLineOptionsUtility {
    /**
     * Below are the command line arguments options short and long names
     */
    protected static final String CLASS_PATH_SHORT = "scp";
    protected static final String CLASS_PATH_LONG = "suite-class-path";
    protected static final String OUTPUT_ROOT_DIR_SHORT = "od";
    protected static final String OUTPUT_ROOT_DIR_LONG = "out-dir";
    protected static final String CLASS_LIST_SHORT = "cl";
    protected static final String CLASS_LIST_LONG = "class-list";
    protected static final String BOOMERANG_PRE_TRANSFORMER_SHORT = "bpt";
    protected static final String BOOMERANG_PRE_TRANSFORMER_LONG = "boomerang-pre-transformer";
    protected static final String REPLACE_OLD_JIMPLE_SHORT = "rej";
    protected static final String REPLACE_OLD_JIMPLE_LONG = "replace-existing-jimple";
    protected static final String GENERATE_DOT_GRAPH_SHORT = "gdg";
    protected static final String GENERATE_DOT_GRAPH_LONG = "generate-dot-graph";
    protected static final String CG_ALGO_SHORT = "cga";
    protected static final String CG_ALGO_LONG = "callgraph-algorithm";

    /**
     * Check for the validness of the given classpath.
     *
     * @param classPath Classpath
     */
    private void checkClassPath(String classPath) {
        if (!FilesUtils.isValidPath(classPath)) {
            System.err.println("Given classpath is not valid!!!");
            System.exit(-1);
        }

        File file = new File(classPath);

        if (file.exists() && !file.isDirectory()) {
            System.err.println("Given classpath is not a directory!!!");
            System.exit(-1);
        }
    }

    /**
     * Check for the validness of the given output root directory.
     *
     * @param outDir Output root directory
     */
    private void checkOutDir(String outDir) {
        if (!FilesUtils.isValidPath(outDir)) {
            System.err.println("Given output root directory is not valid!!!");
            System.exit(-1);
        }

        File file = new File(outDir);

        if (file.exists() && !file.isDirectory()) {
            System.err.println("Given output root directory is not a directory!!!");
            System.exit(-1);
        }
    }

    /**
     * Prints the stacktrace and exit the program
     *
     * @param e Exception
     */
    protected static void printStackTraceAndExit(Exception e) {
        System.err.println("Something went wrong!\nStacktrace: \n");
        e.printStackTrace();
        System.exit(-1);
    }
}
