package de.fraunhofer.iem.hybridCG;

import de.fraunhofer.iem.util.DirectedEdge;
import de.fraunhofer.iem.util.EdgesInAGraph;
import de.fraunhofer.iem.util.SerializableUtility;
import soot.*;
import soot.jimple.InvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.options.Options;
import soot.util.dot.DotGraph;
import soot.util.dot.DotGraphEdge;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class HybridCallGraph {
    public static CallGraph merge(EdgesInAGraph edgesInAGraph, CallGraph staticCallGraph) {

        for (DirectedEdge directedEdge : edgesInAGraph.getDirectedEdges()) {
//            System.out.println("*******************************");
//            System.out.println("Source: " + directedEdge.getSource());
//            System.out.println("Destination: " + directedEdge.getDestination());
//            System.out.println("Associated Library call: " + directedEdge.getAssociatedCallSite());
//            System.out.println("Line number: " + directedEdge.getAssociatedCallSiteLineNumber());
//            System.out.println("isFakeEdge: " + directedEdge.isFakeEdge());
//            System.out.println("isCallSiteSameAsCaller: " + directedEdge.isCallSiteSameAsCaller());
//            System.out.println("*******************************");

            if (!directedEdge.isFakeEdge()) {
                SootMethod caller = getMethod(directedEdge.getSource());
                SootMethod destination = getMethod(directedEdge.getDestination());
                Stmt associatedCallSiteUnit = getAssociatedCallSiteUnit(
                        caller,
                        directedEdge.getAssociatedCallSite(),
                        directedEdge.getAssociatedCallSiteLineNumber()
                );

                boolean isEdgeFound = false;

//                System.out.println(staticCallGraph);
                try {
                    for (Iterator<Edge> it = staticCallGraph.edgesOutOf(associatedCallSiteUnit); it.hasNext(); ) {
                        Edge edge = it.next();

                        if (edge.getSrc().method().getSignature().equals(caller.getSignature())) {
                            if (edge.getTgt().method().getSignature().equals(destination.getSignature())) {
                                System.out.println("Found = " + edge.getSrc().method().getSignature() +
                                        " : " + edge.getTgt().method().getSignature());

                                isEdgeFound = true;
                            }
                        }
                    }
                } catch (Exception | Error e) {
                    e.printStackTrace();
                }

                if (!isEdgeFound) {
                    Edge edge = new Edge(caller, associatedCallSiteUnit, destination);

                    staticCallGraph.addEdge(edge);
                }
            }
//            Unit associatedCallSite =
        }

        generateDotGraph();

        return null;
    }

    public static void generateDotGraph() {
        CallGraph callGraph = Scene.v().getCallGraph();

        DotGraph dot = new DotGraph("final:callgraph");
        Iterator<Edge> iteratorEdges = callGraph.iterator();

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


            DotGraphEdge dotGraphEdge = dot.drawEdge(node_src, node_tgt);
            dotGraphEdge.setLabel(edge.srcStmt().toString());

        }
        dot.plot("callgraph.dot");
    }

    private static Stmt getAssociatedCallSiteUnit(
            SootMethod caller,
            String associatedCallSite,
            int associatedCallSiteLineNumber) {
        if (caller.hasActiveBody()) {
            Body body = caller.getActiveBody();

            for (Unit unitBox : body.getUnits()) {
                Stmt unit = (Stmt) unitBox;

                if (unit.containsInvokeExpr()) {
                    InvokeExpr invokeExpr = unit.getInvokeExpr();

                    SootMethod callSiteMethod = invokeExpr.getMethod();
                    String methodSignature = callSiteMethod.getSignature();

                    if (associatedCallSite.contains("(") && associatedCallSite.contains(")")) {
                        StringBuilder temp = new StringBuilder(callSiteMethod.getDeclaringClass().getName() + "." +
                                callSiteMethod.getName() + "(");

                        if (callSiteMethod.getParameterTypes().size() > 0) {
                            for (Type type : callSiteMethod.getParameterTypes()) {
                                temp.append(type.toString()).append(",");
                            }

                            temp.setLength(temp.length() - 1);
                        }

                        temp.append(")");

                        System.out.println(temp);
                        System.out.println(associatedCallSite);
                        System.out.println(associatedCallSiteLineNumber);

                        if (temp.toString().equals(associatedCallSite)) {
                            if (associatedCallSiteLineNumber > 0) {
                                System.out.println("GOOD NEWS");
                                return unit;
                            }
                        }
                    } else {
                        if (methodSignature.contains(associatedCallSite)) {
                            if (associatedCallSiteLineNumber > 0) {
                                return unit;
                            }
                        }
                    }
                }
            }
        }

        return null;
    }

    private static SootMethod getMethod(String methodSignature) {
        String[] methodSignatureArray = methodSignature.split("\\(");
        String parameters = methodSignatureArray[1].replaceAll("\\)", "");
        String[] classNameArray = methodSignatureArray[0].split("\\.");
        String methodName = classNameArray[classNameArray.length - 1];
        String className = String.join(".", Arrays.copyOf(classNameArray, classNameArray.length - 1));
        List<Type> types = new ArrayList<>();

        for (String param : parameters.split(",")) {
            if (!param.equals("")) {
                types.add(Scene.v().getType(param));
            }
        }

//        System.out.println(className);
//        System.out.println(methodName);
//        System.out.println(parameters);
//        System.out.println(types);

        SootClass sootClass = Scene.v().forceResolve(className, SootClass.BODIES);

        SootMethod sootMethod = sootClass.getMethod(methodName, types);

//        System.out.println("----> " + sootMethod);
//        for (SootMethod sootMethod : sootClass.)
        return sootMethod;
    }

    public static void main(String[] args) {
        String appClassPath = "D:\\cgbench\\CGBench\\bean\\target\\classes";

        initializeSoot(appClassPath);

        String filename = "D:\\cgbench\\CGBench\\bean\\target\\dynamic_callgraph_1";

        EdgesInAGraph edgesInAGraph = SerializableUtility.deSerialize(filename);

        if (edgesInAGraph == null) {
            System.out.println("NULL");
            return;
        }

        merge(edgesInAGraph, Scene.v().getCallGraph());
    }

    /**
     * Initializes the soot
     */
    private static void initializeSoot(String appClassPath) {
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
