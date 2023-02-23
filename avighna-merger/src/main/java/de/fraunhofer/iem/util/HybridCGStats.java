package de.fraunhofer.iem.util;

public class HybridCGStats {
    private int numberOfEdgesInStaticCallGraph;
    private int numberOfEdgesInHybridCallGraph;
    private int numberOfDynamicEdgesAdded;
    private int numberOfEdgesWithSameCallSiteMethod;
    private int numberOfEdgesWithDifferentCallSiteMethod;
    private int numberOfFakeEdges;
    private int numberOfFakeEdgesFoundButNotAdded;
    private int numberOfGodEdges;

    private String pathToStaticCGDOTGraph;
    private String pathToHybridCGDOTGraph;
    private String pathToStaticCGIMGGraph;
    private String pathToHybridCGIMGGraph;

    public int getNumberOfEdgesInStaticCallGraph() {
        return numberOfEdgesInStaticCallGraph;
    }

    public void setNumberOfEdgesInStaticCallGraph(int numberOfEdgesInStaticCallGraph) {
        this.numberOfEdgesInStaticCallGraph = numberOfEdgesInStaticCallGraph;
    }

    public int getNumberOfEdgesInHybridCallGraph() {
        return numberOfEdgesInHybridCallGraph;
    }

    public void setNumberOfEdgesInHybridCallGraph(int numberOfEdgesInHybridCallGraph) {
        this.numberOfEdgesInHybridCallGraph = numberOfEdgesInHybridCallGraph;
    }

    public int getNumberOfDynamicEdgesAdded() {
        return numberOfDynamicEdgesAdded;
    }

    public void setNumberOfDynamicEdgesAdded(int numberOfDynamicEdgesAdded) {
        this.numberOfDynamicEdgesAdded = numberOfDynamicEdgesAdded;
    }

    public int getNumberOfEdgesWithSameCallSiteMethod() {
        return numberOfEdgesWithSameCallSiteMethod;
    }

    public void setNumberOfEdgesWithSameCallSiteMethod(int numberOfEdgesWithSameCallSiteMethod) {
        this.numberOfEdgesWithSameCallSiteMethod = numberOfEdgesWithSameCallSiteMethod;
    }

    public int getNumberOfEdgesWithDifferentCallSiteMethod() {
        return numberOfEdgesWithDifferentCallSiteMethod;
    }

    public void setNumberOfEdgesWithDifferentCallSiteMethod(int numberOfEdgesWithDifferentCallSiteMethod) {
        this.numberOfEdgesWithDifferentCallSiteMethod = numberOfEdgesWithDifferentCallSiteMethod;
    }

    public int getNumberOfFakeEdges() {
        return numberOfFakeEdges;
    }

    public void setNumberOfFakeEdges(int numberOfFakeEdges) {
        this.numberOfFakeEdges = numberOfFakeEdges;
    }

    public int getNumberOfFakeEdgesFoundButNotAdded() {
        return numberOfFakeEdgesFoundButNotAdded;
    }

    public void setNumberOfFakeEdgesFoundButNotAdded(int numberOfFakeEdgesFoundButNotAdded) {
        this.numberOfFakeEdgesFoundButNotAdded = numberOfFakeEdgesFoundButNotAdded;
    }

    public int getNumberOfGodEdges() {
        return numberOfGodEdges;
    }

    public void setNumberOfGodEdges(int numberOfGodEdges) {
        this.numberOfGodEdges = numberOfGodEdges;
    }

    public String getPathToStaticCGDOTGraph() {
        return pathToStaticCGDOTGraph;
    }

    public void setPathToStaticCGDOTGraph(String pathToStaticCGDOTGraph) {
        this.pathToStaticCGDOTGraph = pathToStaticCGDOTGraph;
    }

    public String getPathToHybridCGDOTGraph() {
        return pathToHybridCGDOTGraph;
    }

    public void setPathToHybridCGDOTGraph(String pathToHybridCGDOTGraph) {
        this.pathToHybridCGDOTGraph = pathToHybridCGDOTGraph;
    }

    public String getPathToStaticCGIMGGraph() {
        return pathToStaticCGIMGGraph;
    }

    public void setPathToStaticCGIMGGraph(String pathToStaticCGIMGGraph) {
        this.pathToStaticCGIMGGraph = pathToStaticCGIMGGraph;
    }

    public String getPathToHybridCGIMGGraph() {
        return pathToHybridCGIMGGraph;
    }

    public void setPathToHybridCGIMGGraph(String pathToHybridCGIMGGraph) {
        this.pathToHybridCGIMGGraph = pathToHybridCGIMGGraph;
    }
}
