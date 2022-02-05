package de.fraunhofer.iem.hybridCG;

import de.fraunhofer.iem.util.DirectedEdge;
import de.fraunhofer.iem.util.EdgesInAGraph;
import de.fraunhofer.iem.util.SerializableUtility;
import soot.SootMethod;
import soot.jimple.toolkits.callgraph.CallGraph;

public class HybridCallGraph {
    public static CallGraph merge(EdgesInAGraph edgesInAGraph, CallGraph staticCallGraph) {

        for (DirectedEdge directedEdge : edgesInAGraph.getDirectedEdges()) {
            System.out.println("*******************************");
            System.out.println("Source: " + directedEdge.getSource());
            System.out.println("Destination: " + directedEdge.getDestination());
            System.out.println("Associated Library call: " + directedEdge.getAssociatedCallSite());
            System.out.println("Line number: " + directedEdge.getAssociatedCallSiteLineNumber());
            System.out.println("isFakeEdge: " + directedEdge.isFakeEdge());
            System.out.println("isCallSiteSameAsCaller: " + directedEdge.isCallSiteSameAsCaller());
            System.out.println("*******************************");
        }
        return null;
    }

    private static SootMethod getMethod(String methodSignature) {

        return null;
    }

    public static void main(String[] args) {
        String filename = "D:\\cgbench\\CGBench\\bean\\target\\dynamic_callgraph_1";

        EdgesInAGraph edgesInAGraph = SerializableUtility.deSerialize(filename);

        if (edgesInAGraph == null) {
            System.out.println("NULL");
            return;
        }

        merge(edgesInAGraph, null);
    }
}
