package de.fraunhofer.iem;

import soot.*;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.options.Options;
import soot.util.dot.DotGraph;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Main {
    private void initializeSoot() {
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

        List<SootMethod> entries = new ArrayList<SootMethod>();
        List<String> appClasses = new ArrayList<>(FilesUtils.getClassesAsList(appClassPath));

        for (String appClass : appClasses) {
            SootClass sootClass = Scene.v().forceResolve(appClass, SootClass.BODIES);
            sootClass.setApplicationClass();
            entries.addAll(sootClass.getMethods());
        }

        Scene.v().setEntryPoints(entries);
        Scene.v().loadNecessaryClasses();
    }

    private void mergeCallGraph() {
        DotGraph dotGraph = new DotGraph("");


    }

    public void generateDotGraph(SootMethod sootMethod) {
        initializeSoot();
        PackManager.v().runPacks();

        CallGraph callGraph = Scene.v().getCallGraph();

        sootMethod.getActiveBody().uni
        SootClass sootClass = Scene.v().forceResolve(appClass, SootClass.BODIES);
        MethodOrMethodContext methodOrMethodContext = new Me
        Edge edge1 = new Edge("")
        DotGraph dot = new DotGraph("callgraph");
        Iterator<Edge> iteratorEdges = callGraph.iterator();

        callGraph.addEdge(new Edge())
        System.out.println("Call Graph size : " + callGraph.size());
        while (iteratorEdges.hasNext()) {
            Edge edge = iteratorEdges.next();
            String node_src = edge.getSrc().toString();
            String node_tgt = edge.getTgt().toString();

            if (node_src.startsWith("<java.") || node_tgt.startsWith("<java."))
                continue;

            if (node_src.startsWith("<sun.") || node_tgt.startsWith("<sun."))
                continue;

            if (node_src.startsWith("<javax.") || node_tgt.startsWith("<javax."))
                continue;


            dot.drawEdge(node_src, node_tgt).sets;

        }
        dot.plot("callgraph.dot");
    }
}
