package de.fraunhofer.iem;


import de.fraunhofer.iem.exception.DtsSerializeUtilException;
import de.fraunhofer.iem.util.*;
import soot.util.dot.DotGraphEdge;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;


/**
 * Data structure that contains the dynamic call stack and provide some operations
 *
 * @author Ranjith Krishnamurthy
 */
public class DynamicCallStack {
    private static final int MAX_STORAGE = 1;

    private final long pid;
    private final List<String> callStack;

    private final String outputFile;
    private final StringBuilder currentIndentation = new StringBuilder("");

    private final List<String> continuousCallStack;
    private final List<String> associatedLibraryCallStack;
    private boolean isAssociatedLibraryCallPresent;
    private final SerializableDotGraph dotGraph;
    private final EdgesInAGraph edgesInAGraph;
    public static final Set<String> fakeEdges = new HashSet<>();
    public static String outputRootDirectory;
    public static boolean saveCallGraphAsDotFile;

    /**
     * Initializes the stack
     *
     * @param pid Process ID, for which this stack belongs to.
     */
    public DynamicCallStack(long pid) {
        this.pid = pid;
        this.callStack = new ArrayList<>();
        this.outputFile = outputRootDirectory + System.getProperty("file.separator") + "stack_" + this.pid + ".txt";
        this.continuousCallStack = new ArrayList<>();
        this.associatedLibraryCallStack = new ArrayList<>();
        this.isAssociatedLibraryCallPresent = false;
        this.dotGraph = new SerializableDotGraph();
        this.edgesInAGraph = new EdgesInAGraph("callgraph", null);
    }

    /**
     * Getter for process ID
     *
     * @return Process ID
     */
    public long getPid() {
        return pid;
    }

    public void libraryReturnCall(String methodSignature) {
        if (associatedLibraryCallStack.size() > 0) {
            if (methodSignature.startsWith(associatedLibraryCallStack.get(associatedLibraryCallStack.size() - 1))) {
                associatedLibraryCallStack.remove(associatedLibraryCallStack.size() - 1);
                isAssociatedLibraryCallPresent = false;
            }
        }
    }

    public void libraryCall(String methodSignature) {
        if (!isAssociatedLibraryCallPresent && this.continuousCallStack.size() > 0) {
            this.associatedLibraryCallStack.add(methodSignature);
            isAssociatedLibraryCallPresent = true;

            //TODO: Test the below uncommented code (and check is it required)
//            StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
//            String sourceNode = this.continuousCallStack.get(this.continuousCallStack.size() - 1);
//
//            if (stackTraceElements.length > 5) {
//                String calledLibraryMethod = stackTraceElements[3].getClassName() + "." + stackTraceElements[3].getMethodName();
//                String methodThatCalledLibraryMethod = stackTraceElements[4].getClassName() + "." + stackTraceElements[4].getMethodName();
//
//                String temp = methodSignature.split(": ")[0].replace("<", "") +
//                        "." + methodSignature.split(": ")[1].split(" ")[1].replace(">", "");
//                if (temp.startsWith(calledLibraryMethod + "(")) {
//                    if (sourceNode.split(":")[0].trim().startsWith(methodThatCalledLibraryMethod + "(")) {
//                        this.associatedLibraryCallStack.add(methodSignature);
//                        isAssociatedLibraryCallPresent = true;
//                    }
//                }
//            }
        }
    }

    /**
     * Performs the operations related to method call i.e. adding the method to stack, creating
     * an edge in the dot graph.
     *
     * @param methodSignature Method signature
     */
    public void methodCall(String methodSignature) {
        if (this.continuousCallStack.size() == 0) {
            this.continuousCallStack.add(methodSignature);

            // add a fake edge to represent the first call is from the spring boot framework
            String sourceNode = "framework.class: godMethod";
            String associatedLibraryCall = "framework.class.godAssociatedCallSite";


            DirectedEdge directedEdge = new DirectedEdge(
                    sourceNode,
                    methodSignature,
                    associatedLibraryCall,
                    -1,
                    true,
                    false,
                    true);

            if (!edgesInAGraph.getDirectedEdges().contains(directedEdge)) {
                edgesInAGraph.addDirectedEdge(directedEdge);

                DotGraphEdge dotGraphEdge = dotGraph.drawEdge(sourceNode, methodSignature);

                dotGraphEdge.setLabel(-1 + "[" + associatedLibraryCall + "]");

                dotGraphEdge.setAttribute("color", "red");
                dotGraphEdge.setStyle("dashed");
            }
        } else {
            String sourceNode = this.continuousCallStack.get(this.continuousCallStack.size() - 1);
            String associatedLibraryCall = null;
            DotGraphEdge dotGraphEdge = null;
            boolean isFakeEdge = false;
            boolean isCallSiteSameAsCaller = false;
            String nextMethodSignature = "";
            int lineNumber = -1;

            StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();

            for (int index = 0; index < stackTraceElements.length; ++index) {
                StackTraceElement stackTraceElement = stackTraceElements[index];
                String currentMethodSignature = stackTraceElement.getClassName() + "." + stackTraceElement.getMethodName();

                String temp = sourceNode.split(": ")[0] +
                        "." + sourceNode.split(": ")[1].split(" ")[1];

                if (temp.startsWith(currentMethodSignature + "(")) {
                    nextMethodSignature = stackTraceElements[index - 1].getClassName() + "." + stackTraceElements[index - 1].getMethodName();
                    lineNumber = stackTraceElement.getLineNumber();

                    String temp1 = methodSignature.split(": ")[0] +
                            "." + methodSignature.split(": ")[1].split(" ")[1];


                    if (!nextMethodSignature.startsWith("de.fraunhofer.iem.DynamicCallStackManager")) {
                        if (temp1.startsWith(nextMethodSignature + "(")) {
                            isCallSiteSameAsCaller = true;
                        }

                        break;
                    } else {
                        nextMethodSignature = "";
                        lineNumber = -1;
                    }
                }
            }

            if (isFakeEdge(methodSignature)) isFakeEdge = true;
            else if (isFakeEdge(sourceNode)) {
                isFakeEdge = true;
            }

            if (isAssociatedLibraryCallPresent) {
                if (this.associatedLibraryCallStack.size() > 0) {
                    String associated = this.associatedLibraryCallStack.get(this.associatedLibraryCallStack.size() - 1);
                    String temp = associated.split(": ")[0] +
                            "." + associated.split(": ")[1].split(" ")[1];

                    if (nextMethodSignature != null) {
                        if (!temp.startsWith(nextMethodSignature + "(")) {
                            associatedLibraryCall = nextMethodSignature;
                        } else {
                            associatedLibraryCall = temp;
                        }
                    } else {
                        associatedLibraryCall = temp;
                    }

                } else {
                    associatedLibraryCall = nextMethodSignature;
                }
                this.isAssociatedLibraryCallPresent = false;
            } else if (isCallSiteSameAsCaller) {
                associatedLibraryCall = methodSignature;
            } else {
                associatedLibraryCall = nextMethodSignature;
            }

            DirectedEdge directedEdge = new DirectedEdge(
                    sourceNode,
                    methodSignature,
                    associatedLibraryCall,
                    lineNumber,
                    isFakeEdge,
                    isCallSiteSameAsCaller,
                    false);

            if (!edgesInAGraph.getDirectedEdges().contains(directedEdge)) {
                edgesInAGraph.addDirectedEdge(directedEdge);

                dotGraphEdge = dotGraph.drawEdge(sourceNode, methodSignature);

                dotGraphEdge.setLabel(lineNumber + "[" + associatedLibraryCall + "]");

                if (!isCallSiteSameAsCaller) dotGraphEdge.setAttribute("color", "red");

                if (isFakeEdge) {
                    dotGraphEdge.setStyle("dashed");
                    dotGraphEdge.setAttribute("color", "red");
                }
            }

            this.continuousCallStack.add(methodSignature);
        }
    }

    private boolean isFakeEdge(String methodSignature) {
        for (String fakeEdge : fakeEdges) {
            if (methodSignature.contains(fakeEdge)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Performs the operations related to method return call i.e. removing the method to stack,
     * plotting the dot graph if the stack contains only one element.
     *
     * @param methodSignature Method signature
     */
    public void methodReturn(String methodSignature) {
        if (this.continuousCallStack.size() == 1) {
            File dotFile = new File("dynamic_callgraph_" + this.pid + ".dot");
            if (dotFile.exists()) {
                dotFile.delete();
            }

            this.continuousCallStack.remove(this.continuousCallStack.size() - 1);

//            writeRequest();
        } else if (this.continuousCallStack.size() > 0) {
            this.continuousCallStack.remove(this.continuousCallStack.size() - 1);

            if (associatedLibraryCallStack.size() > 0)
                this.isAssociatedLibraryCallPresent = true;
            else
                this.isAssociatedLibraryCallPresent = false;
        }
    }

    public void writeToFile() {
        try {
            File file = new File(this.outputFile);

            BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));

            writer.write("\n" + String.valueOf(callStack).replaceAll(", ", "\n").replaceAll("\\[", "").replaceAll("]", ""));
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeRequest() {
        LoggerUtil.getLOGGER().log(Level.SEVERE, "FINALLY REACHED HERE");
        try {
            SerializableUtility.serialize(edgesInAGraph, outputRootDirectory + System.getProperty("file.separator") + "dynamic_callgraph_" + this.pid);
        } catch (DtsSerializeUtilException e) {
            LoggerUtil.getLOGGER().log(Level.WARNING, "Could not serialize the dynamic traces. \n" + e.getMessage());
        }

        LoggerUtil.getLOGGER().info("Serialized Dynamic CG dumped to the file = " + new File(outputRootDirectory + System.getProperty("file.separator") + "dynamic_callgraph_" + this.pid + ".ser").getAbsolutePath().toString());

        if (saveCallGraphAsDotFile) {
            dotGraph.plot(outputRootDirectory + System.getProperty("file.separator") + "dynamic_callgraph_" + this.pid + ".dot");
            LoggerUtil.getLOGGER().info("DOT file of Dynamic CG dumped to the file = " + new File(outputRootDirectory + System.getProperty("file.separator") + "dynamic_callgraph_" + this.pid + ".dot").getAbsolutePath().toString());
        }
    }
}
