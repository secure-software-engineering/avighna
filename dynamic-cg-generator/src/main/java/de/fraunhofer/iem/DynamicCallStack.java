package de.fraunhofer.iem;


import de.fraunhofer.iem.util.*;
import soot.util.dot.DotGraphEdge;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
    public static boolean saveCallGraphAsImage;

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
        this.edgesInAGraph = new EdgesInAGraph("callgraph", dotGraph);
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
            StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
            String sourceNode = this.continuousCallStack.get(this.continuousCallStack.size() - 1);

            if (stackTraceElements.length > 5) {
                String calledLibraryMethod = stackTraceElements[3].getClassName() + "." + stackTraceElements[3].getMethodName();
                String methodThatCalledLibraryMethod = stackTraceElements[4].getClassName() + "." + stackTraceElements[4].getMethodName();

                if (methodSignature.startsWith(calledLibraryMethod + "(")) {
                    if (sourceNode.split(":")[0].trim().startsWith(methodThatCalledLibraryMethod + "(")) {
                        this.associatedLibraryCallStack.add(methodSignature);
                        isAssociatedLibraryCallPresent = true;
                    }
                }
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
        if (this.continuousCallStack.size() == 0) {
            this.continuousCallStack.add(methodSignature);

        } else {
            String sourceNode = this.continuousCallStack.get(this.continuousCallStack.size() - 1);
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

            if (isFakeEdge(methodSignature)) isFakeEdge = true;
            else if (isFakeEdge(sourceNode)) {
                isFakeEdge = true;
            }

            dotGraphEdge = dotGraph.drawEdge(sourceNode, methodSignature);

            if (isAssociatedLibraryCallPresent) {
                if (this.associatedLibraryCallStack.size() > 0) {
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

                } else {
                    associatedLibraryCall = nextMethodSignature;
                }
                this.isAssociatedLibraryCallPresent = false;
            } else if (isCallSiteSameAsCaller) {
                associatedLibraryCall = methodSignature;
            } else {
                associatedLibraryCall = "NA";
            }

            edgesInAGraph.addDirectedEdge(new DirectedEdge(sourceNode, methodSignature, associatedLibraryCall, lineNumber, isFakeEdge, isCallSiteSameAsCaller));

            dotGraphEdge.setLabel(lineNumber + "[" + associatedLibraryCall + "]");

            if (!isCallSiteSameAsCaller) dotGraphEdge.setAttribute("color", "red");

            if (isFakeEdge) {
                dotGraphEdge.setStyle("dashed");
                dotGraphEdge.setAttribute("color", "red");
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
        if (this.continuousCallStack.size() == 1) {
            File dotFile = new File("dynamic_callgraph_" + this.pid + ".dot");
            if (dotFile.exists()) {
                dotFile.delete();
            }

            this.continuousCallStack.remove(this.continuousCallStack.size() - 1);

            SerializableUtility.serialize(edgesInAGraph, outputRootDirectory + System.getProperty("file.separator") + "dynamic_callgraph_" + this.pid);

            LoggerUtil.getLOGGER().info("Serialized Dynamic CG dumped to the file = " + new File(outputRootDirectory + System.getProperty("file.separator") + "dynamic_callgraph_" + this.pid + ".ser").getAbsolutePath().toString());

            if (saveCallGraphAsDotFile || saveCallGraphAsImage) {
                dotGraph.plot(outputRootDirectory + System.getProperty("file.separator") + "dynamic_callgraph_" + this.pid + ".dot");
                LoggerUtil.getLOGGER().info("DOT file of Dynamic CG dumped to the file = " + new File(outputRootDirectory + System.getProperty("file.separator") + "dynamic_callgraph_" + this.pid + ".dot").getAbsolutePath().toString());
            }

            if (saveCallGraphAsImage) {
                saveDotAsSVG(outputRootDirectory + System.getProperty("file.separator") + "dynamic_callgraph_" + this.pid + ".dot", outputRootDirectory + System.getProperty("file.separator") + "dynamic_callgraph_" + this.pid + ".svg");
                LoggerUtil.getLOGGER().info("SVG file of Dynamic CG dumped to the file = " + new File(outputRootDirectory + System.getProperty("file.separator") + "dynamic_callgraph_" + this.pid + ".svg").getAbsolutePath().toString());
            }

            if (saveCallGraphAsImage && !saveCallGraphAsDotFile) {
                new File(outputRootDirectory + System.getProperty("file.separator") + "dynamic_callgraph_" + this.pid + ".dot")
                        .delete();
            }
        } else if (this.continuousCallStack.size() > 0) {
            this.continuousCallStack.remove(this.continuousCallStack.size() - 1);
            this.isAssociatedLibraryCallPresent = true;
        }
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
            File file = new File(this.outputFile);

            BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));

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

    public static String dotToSvgJarPath = null;

    /**
     * This method converts the given dot file into image file
     *
     * @param dotFileName         Dot file name
     * @param outputImageFileName Image file name
     */
    private void saveDotAsSVG(String dotFileName, String outputImageFileName) {
        if (dotToSvgJarPath == null) {
            LoggerUtil.getLOGGER().log(Level.WARNING, "Can not generates SVG file");
            return;
        }

        try {
            String command = "java -jar " + dotToSvgJarPath + " " + dotFileName + " " + outputImageFileName;

            Process proc = Runtime.getRuntime().exec(command);

            InputStream err = proc.getErrorStream();

            BufferedReader inn = new BufferedReader(new InputStreamReader(err));
            String message = null;
            String line = null;
            while ((line = inn.readLine()) != null) {
                if (message == null) message = line + "\n";

                message += line + "\n";
            }

            if (line != null) {
                LoggerUtil.getLOGGER().log(Level.WARNING, "Some error occurred while generating SVG file = Message from external Jar is = " + message);
            }
        } catch (IOException e) {
            LoggerUtil.getLOGGER().log(Level.WARNING, "Some error occurred while generating SVG file = " + e.getMessage());
        }
    }
}
