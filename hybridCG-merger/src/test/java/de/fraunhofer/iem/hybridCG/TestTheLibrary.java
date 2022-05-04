package de.fraunhofer.iem.hybridCG;

import de.fraunhofer.iem.exception.DotToImgException;
import de.fraunhofer.iem.exception.DtsSerializeUtilException;
import de.fraunhofer.iem.exception.UnexpectedError;
import de.fraunhofer.iem.exception.DtsZipUtilException;
import soot.*;
import soot.options.Options;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.PrintWriter;
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
    public void test() throws UnexpectedError, DtsSerializeUtilException, DtsZipUtilException, DotToImgException, FileNotFoundException {
        //TODO: Alter this based on your testing
//        String appClassPath = "D:\\cgbench\\CGBench\\bean\\target\\classes";
//        String appClassPath = "D:\\Work\\HybridCG\\spring-petclinic\\target\\classes";
//        String appClassPath = "D:\\Work\\HybridCG\\temp\\Test\\target\\classes";
//        String appClassPath = "D:\\Work\\HybridCG\\temp\\fredbet\\target\\classes";
//        String appClassPath = "D:\\Work\\HybridCG\\temp\\initializr\\start.spring.io\\start-site\\target\\classes";
//        String appClassPath = "C:\\Users\\Ranjith\\Downloads\\zipkin-server-2.23.16-exec\\BOOT-INF\\classes";
//        String appClassPath = "C:\\Users\\Ranjith\\Downloads\\streamflow-0.13.0\\streamflow-0.13.0\\lib\\streamflow-app-jar-0.13.0";

        String appClassPath = "D:\\Work\\HybridCG\\temp\\Spring_Projects\\zipkin\\zipkin-server\\target\\classes";
        String dtsFileName = "D:\\Work\\HybridCG\\temp\\Spring_Projects\\zipkin\\hybridCGOutput\\dynamic_cg.dst";
        String hybridOutputPath = "D:\\Work\\HybridCG\\temp\\Spring_Projects\\zipkin\\hybridCGOutput\\hybridMergerOutput\\";

        initializeSoot(appClassPath, dtsFileName);

        new HybridCallGraph().merge(
                dtsFileName,
                Scene.v().getCallGraph(),
                hybridOutputPath,
                "callgraph",
                "callgraph",
                ImageType.SVG
        );
    }

    /**
     * Initializes the soot
     */
    private static void initializeSoot(String appClassPath, String dtsFileName) throws DtsZipUtilException, FileNotFoundException {
        //TODO: Remove this after testing
        G.reset();
        Options.v().set_keep_line_number(true);

        Options.v().setPhaseOption("cg.spark", "on");

        Options.v().setPhaseOption("cg", "all-reachable:true");
        Options.v().set_allow_phantom_refs(true);

        String dynamicCP = new HybridCallGraph().getDynamicClassesPath(appClassPath, dtsFileName);
        Options.v().set_soot_classpath(appClassPath + File.pathSeparator + dynamicCP);

        Options.v().set_prepend_classpath(true);
        Options.v().set_whole_program(true);
        Options.v().set_allow_phantom_refs(true);

        Options.v().setPhaseOption("jb", "use-original-names:true");
        //Options.v().setPhaseOption("jb.lns", "enabled:false");

        Options.v().set_output_format(Options.output_format_none);

        List<String> appClasses = new ArrayList<>(FilesUtils.getClassesAsList(appClassPath));

        appClasses.addAll(new ArrayList<>(FilesUtils.getClassesAsList(dynamicCP)));

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
