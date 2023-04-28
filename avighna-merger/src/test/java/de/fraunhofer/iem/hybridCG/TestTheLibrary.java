package de.fraunhofer.iem.hybridCG;

import de.fraunhofer.iem.exception.DotToImgException;
import de.fraunhofer.iem.exception.DtsSerializeUtilException;
import de.fraunhofer.iem.exception.UnexpectedError;
import de.fraunhofer.iem.exception.DtsZipUtilException;
import soot.*;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.options.Options;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import soot.util.dot.DotGraph;
import soot.util.dot.DotGraphEdge;

/**
 * Test: check it generates valid hybrid call graph
 *
 * @author Ranjith Krishnamurthy
 */
public class TestTheLibrary {
    @Test
    public void test() throws UnexpectedError, DtsSerializeUtilException, DtsZipUtilException, DotToImgException, FileNotFoundException {
        //TODO: Alter this based on your testing

        String appClassPath = "/Volumes/Ranjith/Work/Avighna-Project/Other-Projects/JavaReflectionTest/target/classes";
        String dtsFileName = "/Volumes/Ranjith/Work/Avighna-Project/Other-Projects/JavaReflectionTest/avighna-output/dynamic_cg.dst";
        String hybridOutputPath = "/Volumes/Ranjith/Work/Avighna-Project/Other-Projects/JavaReflectionTest/avighna-output/hybridMergerOutput/";

        System.out.println("Select the Project: ");
        System.out.println("1. Bean");
        System.out.println("2. Spring-Petclinic");
        System.out.println("3. Fredbet");
        System.out.println("4. Spring Initializer Website");
        System.out.println("5. Zipkin");
        System.out.println("6. Reflection Test Suite");

        int option = -1;

        option = 3;

        if (option == 1) { //Bean
            appClassPath = "D:\\Work\\HybridCG\\temp\\Spring_Projects\\zipkin\\zipkin-server\\target\\classes";
            dtsFileName = "D:\\Work\\HybridCG\\temp\\Spring_Projects\\zipkin\\hybridCGOutput\\dynamic_cg.dst";
            hybridOutputPath = "D:\\Work\\HybridCG\\temp\\Spring_Projects\\zipkin\\hybridCGOutput\\hybridMergerOutput\\";
        } else if (option == 2) { //Spring-Petclinic
            appClassPath = "/Volumes/Ranjith/Work/Avighna-Project/Spring-Projects/spring-petclinic/target/classes";
            dtsFileName = "/Volumes/Ranjith/Work/Avighna-Project/Spring-Projects/spring-petclinic/avighnaOutput/dynamic_cg.dst";
            hybridOutputPath = "/Volumes/Ranjith/Work/Avighna-Project/Spring-Projects/spring-petclinic/avighnaOutput/hybridMergerOutput/";
        } else if (option == 3) { //Fredbet
            appClassPath = "/Volumes/Ranjith/Work/Avighna-Project/Spring-Projects/fredbet/target/classes";
            dtsFileName = "/Volumes/Ranjith/Work/Avighna-Project/Spring-Projects/fredbet/avighna-output/dynamic_cg.dst";
            hybridOutputPath = "/Volumes/Ranjith/Work/Avighna-Project/Spring-Projects/fredbet/avighna-output/hybridMergerOutput/";
        } else if (option == 4) { //Spring Initializer Website
            appClassPath = "D:\\Work\\HybridCG\\temp\\Spring_Projects\\start.spring.io\\start-site\\target\\classes";
            dtsFileName = "D:\\Work\\HybridCG\\temp\\Spring_Projects\\start.spring.io\\hybridCGOutput\\dynamic_cg.dst";
            hybridOutputPath = "D:\\Work\\HybridCG\\temp\\Spring_Projects\\start.spring.io\\hybridCGOutput\\hybridMergerOutput\\";
        } else if (option == 5) { //Zipkin
            appClassPath = "D:\\Work\\HybridCG\\temp\\Spring_Projects\\zipkin\\zipkin-server\\target\\classes";
            dtsFileName = "D:\\Work\\HybridCG\\temp\\Spring_Projects\\zipkin\\hybridCGOutput\\dynamic_cg.dst";
            hybridOutputPath = "D:\\Work\\HybridCG\\temp\\Spring_Projects\\zipkin\\hybridCGOutput\\hybridMergerOutput\\";
        } else if (option == 6) { //Reflection Test Suite
            appClassPath = "/Volumes/Ranjith/Work/Avighna-Project/Other-Projects/Decapo/scratch/jar/";
            dtsFileName = "/Volumes/Ranjith/Work/Avighna-Project/Other-Projects/Decapo/avighna-output/dynamic_cg.dst";
            hybridOutputPath = "/Volumes/Ranjith/Work/Avighna-Project/Other-Projects/Decapo/avighna-output/hybridMergerOutput/";
        } else {
            System.exit(0);
        }

        initializeSoot(appClassPath, null);

        int initialEdges = generateInitialDotGraph();

        initializeSoot(appClassPath, dtsFileName);

        new HybridCallGraph(true, initialEdges).merge(
                dtsFileName,
                Scene.v().getCallGraph(),
                hybridOutputPath,
                "callgraph",
                "callgraph",
                ImageType.SVG
        );
    }

    private int generateInitialDotGraph() {
        CallGraph callGraph = Scene.v().getCallGraph();

        int numberOfEdgesInStaticCallGraph = 0;
        DotGraph dotGraph = new DotGraph("final:callgraph");

        for (Edge edge : callGraph) {
            String node_src = edge.getSrc().toString();
            String node_tgt = edge.getTgt().toString();

            if (node_src.startsWith("<java.") || node_tgt.startsWith("<java.")) continue;

            if (node_src.startsWith("<sun.") || node_tgt.startsWith("<sun.")) continue;

            if (node_src.startsWith("<javax.") || node_tgt.startsWith("<javax.")) continue;

            if (node_src.startsWith("<jdk.") || node_tgt.startsWith("<jdk.")) continue;

            if (node_src.startsWith("<com.sun.crypto.provider.") || node_tgt.startsWith("<com.sun.crypto.provider.")) continue;

            ++numberOfEdgesInStaticCallGraph;
        }

        return numberOfEdgesInStaticCallGraph;
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

        String dynamicCP = null;

        if (dtsFileName == null) {
            Options.v().set_soot_classpath(appClassPath + File.pathSeparator);
        } else {
            dynamicCP = new HybridCallGraph().getDynamicClassesPath(dtsFileName);
            System.out.println();
            Options.v().set_soot_classpath(appClassPath + File.pathSeparator + dynamicCP);
        }

        Options.v().set_prepend_classpath(true);
        Options.v().set_whole_program(true);
        Options.v().set_allow_phantom_refs(true);

        Options.v().setPhaseOption("jb", "use-original-names:true");
        //Options.v().setPhaseOption("jb.lns", "enabled:false");

        Options.v().set_output_format(Options.output_format_none);

        List<String> appClasses = new ArrayList<>(FilesUtils.getClassesAsList(appClassPath));

        if (dtsFileName != null) {
            if (new File(dynamicCP).exists()) {
                appClasses.addAll(new ArrayList<>(FilesUtils.getClassesAsList(dynamicCP)));
            }
        }

        List<SootMethod> entries = new ArrayList<SootMethod>();
        for (String appClass : appClasses) {
            System.out.println(appClass);
            SootClass sootClass = Scene.v().forceResolve(appClass, SootClass.BODIES);
            sootClass.setApplicationClass();
            entries.addAll(sootClass.getMethods());
        }

        Scene.v().setEntryPoints(entries);
        Scene.v().loadNecessaryClasses();
        PackManager.v().getPack("cg").apply();
    }
}
