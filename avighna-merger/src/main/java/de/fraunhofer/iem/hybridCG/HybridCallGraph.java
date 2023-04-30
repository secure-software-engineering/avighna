package de.fraunhofer.iem.hybridCG;

import de.fraunhofer.iem.exception.DotToImgException;
import de.fraunhofer.iem.exception.DtsSerializeUtilException;
import de.fraunhofer.iem.exception.UnexpectedError;
import de.fraunhofer.iem.exception.DtsZipUtilException;
import de.fraunhofer.iem.util.*;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.engine.GraphvizCmdLineEngine;
import soot.*;
import soot.jimple.InvokeExpr;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.jimple.Stmt;
import soot.jimple.internal.JStaticInvokeExpr;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.util.dot.DotGraph;
import soot.util.dot.DotGraphEdge;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * This class merges the dynamically generated EdgesInAGraphs into the statically generated call graph
 *
 * @author Ranjith Krishnaurthy
 */
public class HybridCallGraph {
    private int numberOfEdgesInPureStaticCallgraph = 0;
    private int numberOfInitialFakeEdges = 0;
    private boolean isFakeEdgesAdded = false;
    private DotGraph dotGraph = null;
    private DotGraph onlyAddedDotGraph = null;
    private DotGraph staticDotGraph = null;
    private HybridCGStats hybridCGStats = null;

    private int numberOfEdgesInStaticCallGraph = 0;
    private int numberOfDynamicEdgesAdded = 0;
    private int numberOfEdgesWithSameCallSiteMethod = 0;
    private int numberOfEdgesWithDifferentCallSiteMethod = 0;
    private int numberOfFakeEdges = 0;

    private int numberOfGodEdges = 0;

    private final HashSet<String> listOfAddedGodEdges = new HashSet<>();

    private JimpleBody godJimpleBody = null;
    private SootMethod godMethod = null;

    public HybridCallGraph() {
        this(false, 0);
    }

    public HybridCallGraph(boolean isFakeEdgesAdded, int numberOfEdgesInPureStaticCallgraph) {
        this.isFakeEdgesAdded = isFakeEdgesAdded;
        this.numberOfEdgesInPureStaticCallgraph = numberOfEdgesInPureStaticCallgraph;
    }

    public String getDynamicClassesPath(String dstFileName) throws DtsZipUtilException {
        return ZipUtility.unzipDTSFile(dstFileName) + File.separator + "dynamicCP";
    }

    private JimpleBody getGodJimpleBody() {
        if (godJimpleBody == null) {
            SootClass godClass = new SootClass("GodClass", Modifier.PUBLIC);
            godClass.setSuperclass(Scene.v().getSootClass("java.lang.Object"));
            Scene.v().addClass(godClass);

            godMethod = new SootMethod("godMethod", Collections.emptyList(), VoidType.v(), Modifier.PUBLIC);
            godClass.addMethod(godMethod);

            godJimpleBody = Jimple.v().newBody(godMethod);
            godMethod.setActiveBody(godJimpleBody);

            return godJimpleBody;
        } else {
            return godJimpleBody;
        }
    }

    private int associatedCallSiteCount = 0;
    private Stmt getGodAssociatedStmt() {
        SootClass godAssociatedCallSite = new SootClass("GodAssociatedCallSite", Modifier.PUBLIC);
        godAssociatedCallSite.setSuperclass(Scene.v().getSootClass("java.lang.Object"));
        Scene.v().addClass(godAssociatedCallSite);

        SootMethod associatedGodMethod = new SootMethod("godAssociatedMethod" + associatedCallSiteCount++, Collections.emptyList(), VoidType.v(), Modifier.STATIC);
        godAssociatedCallSite.addMethod(associatedGodMethod);

        JimpleBody associatedGodJimpleBody = Jimple.v().newBody(associatedGodMethod);
        associatedGodMethod.setActiveBody(associatedGodJimpleBody);

        JStaticInvokeExpr jStaticInvokeExpr = new JStaticInvokeExpr(associatedGodMethod.makeRef(), Collections.emptyList());

        return Jimple.v().newInvokeStmt(jStaticInvokeExpr);
    }

    /**
     * This method merges the given list of dynamically generated edgesInAGraphs into the statically generated
     * call graph in the Soot scene.
     *
     * @param dstFileName     Path of the DTS file (dynamic stack trace file)
     * @throws UnexpectedError           Unexpected error occurred
     * @throws DtsSerializeUtilException Serializable utility failed to serialize/deserialize DTS file
     * @throws DtsZipUtilException       Zip utility failed to unzip DTS fie
     */
    private void merge(String dstFileName, String rootOutputDir, boolean isDotGraphGenerate) throws UnexpectedError, DtsSerializeUtilException, DtsZipUtilException {
        CallGraph staticCallGraph = Scene.v().getCallGraph();

        hybridCGStats = new HybridCGStats();
        hybridCGStats.setPathToStaticCGIMGGraph("Not Available");
        hybridCGStats.setPathToHybridCGIMGGraph("Not Available");
        hybridCGStats.setPathToStaticCGDOTGraph("Not Available");
        hybridCGStats.setPathToHybridCGDOTGraph("Not Available");

        dotGraph = new DotGraph("final:callgraph");
        onlyAddedDotGraph = new DotGraph("final:newAddedCallgraph");
        staticDotGraph = new DotGraph("final:callgraph");
        // Generate the initial dot-graph from the static call-graph
        generateInitialDotGraph();

        List<EdgesInAGraph> dynamicEdgesInAGraphs = getEdgesInAGraphFromDTSFile(dstFileName);

        for (EdgesInAGraph edgesInAGraph : dynamicEdgesInAGraphs) {
            for (DirectedEdge directedEdge : edgesInAGraph.getDirectedEdges()) {
                //TODO: For now we left fake edges, consider in the future if want

                if (directedEdge.isGodEdge()) {
                    JimpleBody tempGodJimpleBody = getGodJimpleBody();

                    SootMethod destination;

                    try {
                        destination = getMethod(directedEdge.getDestination());
                    } catch (Exception | Error e) {
                        continue;
                    }

                    if (!listOfAddedGodEdges.contains(destination.getSignature())) {
                        Stmt stmt = getGodAssociatedStmt();
                        tempGodJimpleBody.getUnits().add(stmt);
                        Edge edge = new Edge(godMethod, stmt, destination);
                        staticCallGraph.addEdge(edge);

                        listOfAddedGodEdges.add(destination.getSignature());

                        if (isDotGraphGenerate) {
                            DotGraphEdge dotGraphEdge = dotGraph.drawEdge(godMethod.getSignature(), destination.getSignature());
                            dotGraphEdge.setLabel(stmt.toString());
                            dotGraphEdge.setAttribute("color", "red");

                            DotGraphEdge dotGraphEdge1 = onlyAddedDotGraph.drawEdge(godMethod.getSignature(), destination.getSignature());
                            dotGraphEdge1.setLabel(stmt.toString());
                            dotGraphEdge1.setAttribute("color", "red");
                        }

                        ++numberOfDynamicEdgesAdded;
                        ++numberOfGodEdges;
                    }
                } else if (!directedEdge.isFakeEdge()) {
                    SootMethod caller;
                    SootMethod destination;

                    try {
                        caller = getMethod(directedEdge.getSource());
                        destination = getMethod(directedEdge.getDestination());
                    } catch (Exception | Error e) {
                        continue;
                    }

                    List<Stmt> statements = getAssociatedCallSiteUnit(caller, directedEdge.getAssociatedCallSite(), directedEdge.getAssociatedCallSiteLineNumber());

                    for (Stmt associatedCallSiteUnit : statements) {
                        boolean isEdgeFound = false;

                        try {
                            for (Iterator<Edge> it = staticCallGraph.edgesOutOf(associatedCallSiteUnit); it.hasNext(); ) {
                                Edge edge = it.next();

                                if (edge.getSrc().method().getSignature().equals(caller.getSignature())) {
                                    if (edge.getTgt().method().getSignature().equals(destination.getSignature())) {
                                        //TODO: Should we have to check for line numbers too?
                                        isEdgeFound = true;
                                    }
                                }
                            }
                        } catch (Exception | Error e) {
                            throw new UnexpectedError("Something went wrong while matching an edge = " + e.getMessage());
                        }

                        if (!isEdgeFound) {
                            Edge edge = new Edge(caller, associatedCallSiteUnit, destination);
                            staticCallGraph.addEdge(edge);

                            if (isDotGraphGenerate) {
                                String colorCode = "";

                                if (directedEdge.isCallSiteSameAsCaller()) {
                                    colorCode = "purple";
                                } else {
                                    colorCode = "blue";
                                }

                                DotGraphEdge dotGraphEdge = dotGraph.drawEdge(caller.getSignature(), destination.getSignature());
                                dotGraphEdge.setLabel(associatedCallSiteUnit.toString());
                                dotGraphEdge.setAttribute("color", colorCode);

                                DotGraphEdge dotGraphEdge1 = onlyAddedDotGraph.drawEdge(caller.getSignature(), destination.getSignature());
                                dotGraphEdge1.setLabel(associatedCallSiteUnit.toString());
                                dotGraphEdge1.setAttribute("color", colorCode);
                            }

                            ++numberOfDynamicEdgesAdded;

                            if (associatedCallSiteUnit.getInvokeExpr().getMethod().getSignature().equals(destination.getSignature())) {
                                ++numberOfEdgesWithSameCallSiteMethod;
                            } else {
                                ++numberOfEdgesWithDifferentCallSiteMethod;
                            }
                        }


                    }
                } else {
                    //TODO: If needed then add the fake edge here
                    ++numberOfFakeEdges;
                }
            }
        }

        hybridCGStats.setNumberOfEdgesInStaticCallGraph(numberOfEdgesInPureStaticCallgraph);
        hybridCGStats.setNumberOfDynamicEdgesAdded(numberOfDynamicEdgesAdded + numberOfInitialFakeEdges);
        hybridCGStats.setNumberOfEdgesWithSameCallSiteMethod(numberOfEdgesWithSameCallSiteMethod);
        hybridCGStats.setNumberOfEdgesWithDifferentCallSiteMethod(numberOfEdgesWithDifferentCallSiteMethod);

        int numberOfEdgesInHybridCallGraph = numberOfEdgesInStaticCallGraph + numberOfDynamicEdgesAdded;
        hybridCGStats.setNumberOfEdgesInHybridCallGraph(numberOfEdgesInHybridCallGraph);

        hybridCGStats.setNumberOfFakeEdges(numberOfInitialFakeEdges);
        hybridCGStats.setNumberOfFakeEdgesFoundButNotAdded(numberOfFakeEdges);
        hybridCGStats.setNumberOfGodEdges(numberOfGodEdges);
    }

    /**
     * This method merges the given list of dynamically generated edgesInAGraphs into the statically generated
     * call graph in the Soot scene.
     * <p>
     * Note: This will not generate DOT graph or statistics file
     *
     * @param dtsFileName     Path of the DTS file (dynamic stack trace file)
     * @param staticCallGraph Static call graph generated by the soot
     * @throws UnexpectedError           Unexpected error occurred
     * @throws DtsSerializeUtilException Serializable utility failed to serialize/deserialize DTS file
     * @throws DtsZipUtilException       Zip utility failed to unzip DTS fie
     */
    public void merge(String dtsFileName, CallGraph staticCallGraph, String rootOutputDir) throws UnexpectedError, DtsSerializeUtilException, DtsZipUtilException {
        merge(dtsFileName, rootOutputDir, false);

        YamlUtilClass.generateStatsFile(hybridCGStats, rootOutputDir);
    }

    /**
     * This method merges the given list of dynamically generated edgesInAGraphs into the statically generated
     * call graph in the Soot scene. This method also generates the new call graph in dot file and an image with the
     * given image type
     *
     * @param dtsFileName       Path of the DTS file (dynamic stack trace file)
     * @param staticCallGraph   Static call graph generated by the soot
     * @param outputDotFileName File name of the generated dot file of the new call graph
     * @param outputImageName   Image file name of the dot file of the new call graph
     * @param imageType         Image type
     * @return Returns the generated statistics file
     * @throws UnexpectedError           Unexpected error occurred
     * @throws DtsSerializeUtilException Serializable utility failed to serialize/deserialize DTS file
     * @throws DtsZipUtilException       Zip utility failed to unzip DTS fie
     * @throws DotToImgException         Failed to convert DOT to image file
     */
    public String merge(String dtsFileName, CallGraph staticCallGraph, String rootOutputDir, String outputDotFileName, String outputImageName, ImageType imageType) throws UnexpectedError, DtsSerializeUtilException, DtsZipUtilException, DotToImgException {
        String statFile = merge(dtsFileName, staticCallGraph, rootOutputDir, outputDotFileName);

        new File(statFile).delete();

        System.out.println("Generating Image files");
        switch (imageType) {
            case SVG:
                System.out.println(hybridCGStats.getPathToHybridCGDOTGraph().replace("_hybrid_cg", "_hybrid_cg_only"));
                saveDotAsImageFile(
                        hybridCGStats.getPathToHybridCGDOTGraph().replace("_hybrid_cg", "_hybrid_cg_only"),
                        rootOutputDir + outputImageName + "_hybrid_cg_only.svg", Format.SVG);

                saveDotAsImageFile(
                        hybridCGStats.getPathToStaticCGDOTGraph(),
                        rootOutputDir + outputImageName + "_static_cg.svg", Format.SVG);
                hybridCGStats.setPathToStaticCGIMGGraph(new File(rootOutputDir + outputImageName + "_static_cg.svg").getAbsolutePath());

                saveDotAsImageFile(
                        hybridCGStats.getPathToHybridCGDOTGraph(),
                        rootOutputDir + outputImageName + "_hybrid_cg.svg", Format.SVG);
                hybridCGStats.setPathToHybridCGIMGGraph(new File(rootOutputDir + outputImageName + "_hybrid_cg.svg").getAbsolutePath());

                break;
            case PNG:
                saveDotAsImageFile(
                        hybridCGStats.getPathToStaticCGDOTGraph(),
                        rootOutputDir + outputImageName + "_static_cg.png", Format.PNG);
                hybridCGStats.setPathToStaticCGIMGGraph(new File(rootOutputDir + outputImageName + "_static_cg.png").getAbsolutePath());

                saveDotAsImageFile(
                        hybridCGStats.getPathToHybridCGDOTGraph(),
                        rootOutputDir + outputImageName + "_hybrid_cg.png", Format.PNG);
                hybridCGStats.setPathToHybridCGIMGGraph(new File(rootOutputDir + outputImageName + "_hybrid_cg.png").getAbsolutePath());

                break;
        }

        return YamlUtilClass.generateStatsFile(hybridCGStats, rootOutputDir);
    }

    /**
     * This method merges the given list of dynamically generated edgesInAGraphs into the statically generated
     * call graph in the Soot scene. This method also generates the new call graph in dot file and an image in SVG format
     *
     * @param dtsFileName       Path of the DTS file (dynamic stack trace file)
     * @param staticCallGraph   Static call graph generated by the soot
     * @param outputDotFileName File name of the generated dot file of the new call graph
     * @param outputImageName   Image file name of the dot file of the new call graph
     * @return Returns the generated statistics file
     * @throws UnexpectedError           Unexpected error occurred
     * @throws DtsSerializeUtilException Serializable utility failed to serialize/deserialize DTS file
     * @throws DtsZipUtilException       Zip utility failed to unzip DTS fie
     * @throws DotToImgException         Failed to convert DOT to image file
     */
    public String merge(String dtsFileName, CallGraph staticCallGraph, String rootOutputDir, String outputDotFileName, String outputImageName) throws UnexpectedError, DtsSerializeUtilException, DtsZipUtilException, DotToImgException {
        return merge(dtsFileName, staticCallGraph, rootOutputDir, outputDotFileName, outputImageName, ImageType.SVG);
    }

    /**
     * This method merges the given list of dynamically generated edgesInAGraphs into the statically generated
     * call graph in the Soot scene. This method also generates the new call graph in dot file
     *
     * @param dtsFileName       Path of the DTS file (dynamic stack trace file)
     * @param staticCallGraph   Static call graph generated by the soot
     * @param outputDotFileName File name of the generated dot file of the new call graph
     * @return Returns the generated statistics file
     * @throws UnexpectedError           Unexpected error occurred
     * @throws DtsSerializeUtilException Serializable utility failed to serialize/deserialize DTS file
     * @throws DtsZipUtilException       Zip utility failed to unzip DTS fie
     */
    public String merge(String dtsFileName, CallGraph staticCallGraph, String rootOutputDir, String outputDotFileName) throws UnexpectedError, DtsSerializeUtilException, DtsZipUtilException {
        merge(dtsFileName, rootOutputDir, true);

        staticDotGraph.plot(rootOutputDir + outputDotFileName + "_static_cg.dot");

        hybridCGStats.setPathToStaticCGDOTGraph(new File(rootOutputDir + outputDotFileName + "_static_cg.dot").getAbsolutePath());

        dotGraph.plot(rootOutputDir + outputDotFileName + "_hybrid_cg.dot");
        hybridCGStats.setPathToHybridCGDOTGraph(new File(rootOutputDir + outputDotFileName + "_hybrid_cg.dot").getAbsolutePath());

        onlyAddedDotGraph.plot(rootOutputDir + outputDotFileName + "_hybrid_cg_only.dot");

        return YamlUtilClass.generateStatsFile(hybridCGStats, rootOutputDir);
    }

    /**
     * Obtain the dynamic traces (EdgesInAGraph object) from the provided DTS file
     *
     * @param dtsFilePath Path of the DTS file
     * @return Dynamic traces (EdgesInAGraph object)
     * @throws DtsSerializeUtilException Serializable utility failed to serialize/deserialize DTS file
     * @throws DtsZipUtilException       Zip utility failed to unzip DTS fie
     */
    private List<EdgesInAGraph> getEdgesInAGraphFromDTSFile(String dtsFilePath) throws DtsSerializeUtilException, DtsZipUtilException {
        List<EdgesInAGraph> edgesInAGraphs = new ArrayList<>();

        ArrayList<String> dotFiles = new ArrayList<>();

        try {
            Files.walk(Paths.get(ZipUtility.unzipDTSFile(dtsFilePath)))
                    .filter(Files::isRegularFile)
                    .filter(it -> it.toAbsolutePath().toString().endsWith(".ser"))
                    .forEach(it -> {
                        dotFiles.add(it.toAbsolutePath().toString());
                    });
        } catch (IOException e) {
            throw new DtsZipUtilException("Failed to retrieve unzipped DTS files location." +
                    "\nMessage = " + e.getMessage());
        }

        for (String filename : dotFiles) {
            EdgesInAGraph edgesInAGraph = SerializableUtility.deSerialize(filename);

            edgesInAGraphs.add(edgesInAGraph);
        }

        return edgesInAGraphs;
    }

    /**
     * This method converts the given dot file into image file
     *
     * @param dotFileName         Dot file name
     * @param outputImageFileName Image file name
     * @param format              Image format
     * @throws DotToImgException Failed to convert DOT to image file
     */
    private void saveDotAsImageFile(String dotFileName, String outputImageFileName, Format format) throws DotToImgException {
        try {
            GraphvizCmdLineEngine engine = new GraphvizCmdLineEngine();
            engine.timeout(15, TimeUnit.MINUTES);
            Graphviz.useEngine(engine);
            Graphviz.fromFile(new File(dotFileName)).render(format).toFile(new File(outputImageFileName));
        } catch (IOException e) {
            throw new DotToImgException("Could not convert DOT to image file." + "\nMessage = " + e.getMessage());
        }
    }

    /**
     * This method generates the dot graph for the call graph present in the Soot scene
     */
    private void generateInitialDotGraph() {
        CallGraph callGraph = Scene.v().getCallGraph();

        for (Edge edge : callGraph) {
            String node_src = edge.getSrc().toString();
            String node_tgt = edge.getTgt().toString();

            if (node_src.startsWith("<java.") || node_tgt.startsWith("<java.")) continue;

            if (node_src.startsWith("<sun.") || node_tgt.startsWith("<sun.")) continue;

            if (node_src.startsWith("<javax.") || node_tgt.startsWith("<javax.")) continue;

            if (node_src.startsWith("<jdk.") || node_tgt.startsWith("<jdk.")) continue;

            if (node_src.startsWith("<com.sun.crypto.provider.") || node_tgt.startsWith("<com.sun.crypto.provider.")) continue;


            DotGraphEdge dotGraphEdge = dotGraph.drawEdge(node_src, node_tgt);
            DotGraphEdge staticDotGraphEdge = staticDotGraph.drawEdge(node_src, node_tgt);
            DotGraphEdge onlyAddedDotGraphEdge = onlyAddedDotGraph.drawEdge(node_src, node_tgt);

            if (node_src.contains("$") || node_tgt.contains("$")) {
                dotGraphEdge.setAttribute("color", "grey");
                dotGraphEdge.setStyle("dashed");

                staticDotGraphEdge.setAttribute("color", "grey");
                staticDotGraphEdge.setStyle("dashed");

                onlyAddedDotGraphEdge.setAttribute("color", "grey");
                onlyAddedDotGraphEdge.setStyle("dashed");
            }

            if (edge.srcStmt() == null) {
                dotGraphEdge.setLabel("null");
                staticDotGraphEdge.setLabel("null");
                onlyAddedDotGraphEdge.setLabel("null");
            } else {
                dotGraphEdge.setLabel(edge.srcUnit().toString());
                staticDotGraphEdge.setLabel(edge.srcUnit().toString());
                onlyAddedDotGraphEdge.setLabel(edge.srcUnit().toString());
            }


            ++numberOfEdgesInStaticCallGraph;
        }

        if (!this.isFakeEdgesAdded) {
            this.numberOfEdgesInPureStaticCallgraph = this.numberOfEdgesInStaticCallGraph;
        }

        this.numberOfInitialFakeEdges = this.numberOfEdgesInStaticCallGraph - this.numberOfEdgesInPureStaticCallgraph;
    }

    /**
     * This method returns the list of statements in the given caller method that matches the given
     * method (associatedCallSite) and the associatedCallSiteLineNumber
     *
     * @param caller                       Caller method
     * @param associatedCallSite           Associated call site method name inside the given caller method
     * @param associatedCallSiteLineNumber line number of the associated call site method in the given caller method
     * @return List of Soot Stmt
     */
    private List<Stmt> getAssociatedCallSiteUnit(SootMethod caller, String associatedCallSite, int associatedCallSiteLineNumber) {
        ArrayList<Stmt> statements = new ArrayList<>();

        if (caller.hasActiveBody()) {
            Body body = caller.getActiveBody();

            for (Unit unitBox : body.getUnits()) {
                Stmt unit = (Stmt) unitBox;

                if (unit.containsInvokeExpr()) {
                    InvokeExpr invokeExpr = unit.getInvokeExpr();

                    SootMethod callSiteMethod = invokeExpr.getMethod();

                    String methodNameWithClassName = callSiteMethod.getDeclaringClass().getName() + "." + callSiteMethod.getName();

                    StringBuilder parametersTypes = new StringBuilder("(");

                    if (callSiteMethod.getParameterTypes().size() > 0) {
                        for (Type type : callSiteMethod.getParameterTypes()) {
                            parametersTypes.append(type.toString()).append(",");
                        }

                        parametersTypes.setLength(parametersTypes.length() - 1);
                    }

                    parametersTypes.append(")");

                    if (associatedCallSite.contains("(") && associatedCallSite.contains(")")) {
                        String temp = methodNameWithClassName + parametersTypes.toString();

                        if (temp.equals(associatedCallSite)) {
                            if (associatedCallSiteLineNumber > 0 && unit.getJavaSourceStartLineNumber() > 0) {
                                if (associatedCallSiteLineNumber == unit.getJavaSourceStartLineNumber()) {
                                    statements.add(unit);
                                }
                            } else {
                                statements.add(unit);
                            }
                        }
                    } else if (methodNameWithClassName.equals(associatedCallSite)) {
                        if (associatedCallSiteLineNumber > 0 && unit.getJavaSourceStartLineNumber() > 0) {
                            if (associatedCallSiteLineNumber == unit.getJavaSourceStartLineNumber()) {
                                statements.add(unit);
                            }
                        } else {
                            statements.add(unit);
                        }
                    }
                }
            }
        }

        return statements;
    }

    /**
     * Returns the SootMethod of the given method signature
     *
     * @param methodSignature Method signature
     * @return SootMethod
     */
    private SootMethod getMethod(String methodSignature) {
        String[] temp = methodSignature.split(": ");
        String className = temp[0];

        temp = temp[1].split(" ");

        Type returnType = Scene.v().getType(temp[0]);

        temp = temp[1].split("\\(");

        String methodName = temp[0];
        String params = temp[1].replace(")", "");

        List<Type> types = new ArrayList<>();

        for (String param : params.split(",")) {
            if (!param.equals("")) {
                types.add(Scene.v().getType(param));
            }
        }

        SootClass sootClass = Scene.v().forceResolve(className, SootClass.BODIES);

        return sootClass.getMethod(methodName, types, returnType);
    }
}
