package de.fraunhofer.iem.hybridCG;

import de.fraunhofer.iem.exception.DotToImgException;
import de.fraunhofer.iem.exception.DtsSerializeUtilException;
import de.fraunhofer.iem.exception.UnexpectedError;
import de.fraunhofer.iem.exception.DtsZipUtilException;
import soot.*;
import soot.options.Options;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

/**
 * Test: check it generates valid hybrid call graph
 *
 * @author Ranjith Krishnamurthy
 */
public class TestTheLibrary {
    @Test
    public void test() throws UnexpectedError, DtsSerializeUtilException, DtsZipUtilException, DotToImgException {
        //TODO: Remove this after testing
        String appClassPath = "D:\\cgbench\\CGBench\\bean\\target\\classes";

        initializeSoot(appClassPath);

        String dtsFileName = "D:\\cgbench\\CGBench\\bean\\output\\dynamic_cg.dst";

        new HybridCallGraph().merge(dtsFileName, Scene.v().getCallGraph(), "callgraph", "callgraph", ImageType.PNG);
    }

    /**
     * Initializes the soot
     */
    private static void initializeSoot(String appClassPath) {
        //TODO: Remove this after testing
        G.reset();
        Options.v().set_keep_line_number(true);

        Options.v().setPhaseOption("cg.spark", "on");

        Options.v().setPhaseOption("cg", "all-reachable:true");
        Options.v().set_allow_phantom_refs(true);
        Options.v().set_soot_classpath(appClassPath);
        Options.v().set_prepend_classpath(true);
        Options.v().set_whole_program(true);
        Options.v().set_allow_phantom_refs(true);

        Options.v().setPhaseOption("jb", "use-original-names:true");
        //Options.v().setPhaseOption("jb.lns", "enabled:false");

        Options.v().set_output_format(Options.output_format_none);

        List<String> appClasses = new ArrayList<>(FilesUtils.getClassesAsList(appClassPath));

        List<SootMethod> entries = new ArrayList<SootMethod>();
        for (String appClass : appClasses) {
            SootClass sootClass = Scene.v().forceResolve(appClass, SootClass.BODIES);
            sootClass.setApplicationClass();
            entries.addAll(sootClass.getMethods());
        }

        Scene.v().setEntryPoints(entries);
        Scene.v().loadNecessaryClasses();
        PackManager.v().getPack("cg").apply();
    }
}
