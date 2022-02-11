package de.fraunhofer.iem;


import de.fraunhofer.iem.util.DirectedEdge;
import de.fraunhofer.iem.util.EdgesInAGraph;
import de.fraunhofer.iem.util.SerializableDotGraph;
import de.fraunhofer.iem.util.SerializableUtility;
import soot.util.dot.DotGraphEdge;

import java.io.*;
import java.util.*;


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

    private final List<String> continousCallStack;
    private final List<String> associatedLibraryCallStack;
    private boolean isAssociatedLibraryCallPresent;
    private final SerializableDotGraph dotGraph;
    private final EdgesInAGraph edgesInAGraph;
    private final Set<String> fakeEdges = new HashSet<>();

    /**
     * Initializes the stack
     *
     * @param pid Process ID, for which this stack belongs to.
     */
    public DynamicCallStack(long pid) {
        this.pid = pid;
        this.callStack = new ArrayList<>();
        this.outputFile = "stack_" + this.pid + ".txt";
        this.continousCallStack = new ArrayList<>();
        this.associatedLibraryCallStack = new ArrayList<>();
        this.isAssociatedLibraryCallPresent = false;
        this.dotGraph = new SerializableDotGraph();
        this.edgesInAGraph = new EdgesInAGraph("callgraph", dotGraph);

        fakeEdges.add("$$EnhancerBySpringCGLIB$$");
        fakeEdges.add("$$FastClassBySpringCGLIB$$");
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
                System.out.println("*** Library Return Call ***");
                System.out.println("CS = " + continousCallStack);
                System.out.println("LCS = " + associatedLibraryCallStack);
                System.out.println("*** Library Return Call ***");
            }
        }
    }

    public void libraryCall(String methodSignature) {
        if (methodSignature.contains("org.springframework.context.support.refresh")) {
            System.out.println("OK ENTERED");
        }

        if (!isAssociatedLibraryCallPresent && this.continousCallStack.size() != 0) {
            if (methodSignature.contains("org.springframework.context.support.refresh")) {
                System.out.println("OK ENTERED");
            }
            StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
            String sourceNode = this.continousCallStack.get(this.continousCallStack.size() - 1);

            if (stackTraceElements.length > 5) {
                if (methodSignature.contains("org.springframework.context.support.refresh")) {
                    System.out.println("OK ENTERED");
                }
                String calledLibraryMethod = stackTraceElements[3].getClassName() + "." + stackTraceElements[3].getMethodName();
                String methodThatCalledLibraryMethod = stackTraceElements[4].getClassName() + "." + stackTraceElements[4].getMethodName();

                if (methodSignature.startsWith(calledLibraryMethod + "(")) {
                    if (methodSignature.contains("org.springframework.context.support.refresh")) {
                        System.out.println("OK ENTERED");
                    }
                    if (sourceNode.split(":")[0].trim().startsWith(methodThatCalledLibraryMethod + "(")) {
                        this.associatedLibraryCallStack.add(methodSignature);
                        isAssociatedLibraryCallPresent = true;
                        System.out.println("*** Library Call ***");
                        System.out.println("CS = " + continousCallStack);
                        System.out.println("LCS = " + associatedLibraryCallStack);
                        System.out.println("*** Library Call ***");
                    }
                }
            }

//            new Scanner(System.in).next();
//            for (int index = 0; index < stackTraceElements.length; ++index) {
//                StackTraceElement stackTraceElement = stackTraceElements[index];
//                String currentMethodSignature = stackTraceElement.getClassName() + "." + stackTraceElement.getMethodName();
//
//                if (sourceNode.split(":")[0].trim().startsWith(currentMethodSignature + "(")) {
//                    String nextMethodSignature = stackTraceElements[index - 1].getClassName() + "." + stackTraceElements[index - 1].getMethodName();
//
//                    if (methodSignature.startsWith(nextMethodSignature + "(")) {
//                        this.associatedLibraryCallStack.add(methodSignature);
//                        isAssociatedLibraryCallPresent = true;
//                        System.out.println("*** Library Call ***");
//                        System.out.println("CS = " + continousCallStack);
//                        System.out.println("LCS = " + associatedLibraryCallStack);
//                        System.out.println("*** Library Call ***");
//                    }
//                }
//            }

            if (methodSignature.contains("org.springframework.context.support.refresh")) {
                System.out.println("OK ENTERED");
                new Scanner(System.in).nextLine();
            }
        }
    }

    /**
     * Performs the operations related to method call i.e. adding the method to stack, creating
     * an edge in the dot graph.
     *
     * @param methodSignature Method signature
     */
    public void methodCall(String methodSignature) {
        if (this.continousCallStack.size() == 0) {
            this.continousCallStack.add(methodSignature);
            System.out.println("*** Application Call ***");
            System.out.println("CS = " + continousCallStack);
            System.out.println("LCS = " + associatedLibraryCallStack);
            System.out.println("*** Application Call ***");

        } else {
            String sourceNode = this.continousCallStack.get(this.continousCallStack.size() - 1);
            String associatedLibraryCall = null;
            DotGraphEdge dotGraphEdge = null;
            boolean isFakeEdge = false;
            boolean isCallSiteSameAsCaller = false;
            String nextMethodSignature = null;
            int lineNumber = -1;

            StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();

            for (int index = 0; index < stackTraceElements.length; ++index) {
                StackTraceElement stackTraceElement = stackTraceElements[index];
                String currentMethodSignature = stackTraceElement.getClassName() + "." + stackTraceElement.getMethodName();

                if (sourceNode.split(":")[0].trim().startsWith(currentMethodSignature + "(")) {
                    nextMethodSignature = stackTraceElements[index - 1].getClassName() + "." + stackTraceElements[index - 1].getMethodName();
                    lineNumber = stackTraceElement.getLineNumber();

                    if (methodSignature.startsWith(nextMethodSignature + "(")) {
                        isCallSiteSameAsCaller = true;
                    }
                }
            }

//            if (destinationNode == null) {
//                destinationNode = methodSignature + " : -1[]";
//            }

            if (isFakeEdge(methodSignature)) isFakeEdge = true;
            else if (isFakeEdge(sourceNode)) {
                isFakeEdge = true;
            }

            dotGraphEdge = dotGraph.drawEdge(sourceNode, methodSignature);

            if (isAssociatedLibraryCallPresent) {
                System.out.println("Entered Here");
                String temp = this.associatedLibraryCallStack.get(this.associatedLibraryCallStack.size() - 1);

                if (nextMethodSignature != null) {
                    if (!temp.startsWith(nextMethodSignature + "(")) {
                        associatedLibraryCall = nextMethodSignature;
                    } else {
                        associatedLibraryCall = temp;
                    }
                } else {
                    associatedLibraryCall = temp;
                }

                this.isAssociatedLibraryCallPresent = false;
            } else if (isCallSiteSameAsCaller) {
                associatedLibraryCall = methodSignature;
//                this.associatedLibraryCallStack.add(associatedLibraryCall);
            } else {
                associatedLibraryCall = "NA";
//                this.associatedLibraryCallStack.add("NA");
            }

            edgesInAGraph.addDirectedEdge(new DirectedEdge(sourceNode, methodSignature, associatedLibraryCall, lineNumber, isFakeEdge, isCallSiteSameAsCaller));

            dotGraphEdge.setLabel(lineNumber + "[" + associatedLibraryCall + "]");

            if (!isCallSiteSameAsCaller) dotGraphEdge.setAttribute("color", "red");

            if (isFakeEdge) {
                dotGraphEdge.setStyle("dashed");
                dotGraphEdge.setAttribute("color", "red");
            }

            this.continousCallStack.add(methodSignature);
            System.out.println("*** Application Call ***");
            System.out.println("CS = " + continousCallStack);
            System.out.println("LCS = " + associatedLibraryCallStack);
            System.out.println("*** Application Call ***");
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

    public void methodCall_backup(String methodSignature) {
        if (callStack.size() == MAX_STORAGE) {
            writeToFile();

            callStack.clear();
        }

        callStack.add(currentIndentation.toString() + ">" + methodSignature);

        currentIndentation.append("  ");
    }

    /**
     * Performs the operations related to method return call i.e. removing the method to stack,
     * plotting the dot graph if the stack contains only one element.
     *
     * @param methodSignature Method signature
     */
    public void methodReturn(String methodSignature) {
        if (this.continousCallStack.size() == 1) {
            File dotFile = new File("dynamic_callgraph_" + this.pid + ".dot");
            if (dotFile.exists()) {
                dotFile.delete();
            }

            dotGraph.plot("dynamic_callgraph_" + this.pid + ".dot");

            this.continousCallStack.remove(this.continousCallStack.size() - 1);

            SerializableUtility.serialize(edgesInAGraph, "dynamic_callgraph_" + this.pid);
        } else {
            this.continousCallStack.remove(this.continousCallStack.size() - 1);
            this.isAssociatedLibraryCallPresent = true;
        }

        System.out.println("*** Return Call ***");
        System.out.println("CS = " + continousCallStack);
        System.out.println("LCS = " + associatedLibraryCallStack);
        System.out.println("*** Return Call ***");
    }

    private String readFromInputStream(InputStream inputStream) throws IOException {
        StringBuilder resultStringBuilder = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = br.readLine()) != null) {
                resultStringBuilder.append(line).append("\n");
            }
        }
        return resultStringBuilder.toString();
    }

    public void methodReturn_backup(String methodSignature) {
        currentIndentation.setLength(currentIndentation.length() - 2);

        callStack.add(currentIndentation.toString() + "<" + methodSignature);

        writeToFile();

        callStack.clear();
    }

    public void writeToFile() {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(this.outputFile, true));

            writer.write("\n" + String.valueOf(callStack).replaceAll(", ", "\n").replaceAll("\\[", "").replaceAll("]", ""));
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeRequest() {
        writeToFile();
        callStack.clear();
    }
}
