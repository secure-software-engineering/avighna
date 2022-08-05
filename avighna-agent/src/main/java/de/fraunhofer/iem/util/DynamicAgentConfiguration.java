package de.fraunhofer.iem.util;

import java.util.List;

/**
 * Configuration class to configure the Dynamic Agent
 *
 * @author Ranjith Krishnamurthy
 */
public class DynamicAgentConfiguration {
    private List<String> rootPackageNameOfApplication;
    private String outputRootDirectory;
    private boolean saveCallGraphAsDotFile;
    private List<String> excludeClasses;
    private List<String> fakeEdgesString;
    private boolean isTrackEdges;

    public List<String> getRootPackageNameOfApplication() {
        return rootPackageNameOfApplication;
    }

    public void setRootPackageNameOfApplication(List<String> rootPackageNameOfApplication) {
        this.rootPackageNameOfApplication = rootPackageNameOfApplication;
    }

    public String getOutputRootDirectory() {
        return outputRootDirectory;
    }

    public void setOutputRootDirectory(String outputRootDirectory) {
        this.outputRootDirectory = outputRootDirectory;
    }

    public boolean isSaveCallGraphAsDotFile() {
        return saveCallGraphAsDotFile;
    }

    public void setSaveCallGraphAsDotFile(boolean saveCallGraphAsDotFile) {
        this.saveCallGraphAsDotFile = saveCallGraphAsDotFile;
    }

    public List<String> getExcludeClasses() {
        return excludeClasses;
    }

    public void setExcludeClasses(List<String> excludeClasses) {
        this.excludeClasses = excludeClasses;
    }

    public List<String> getFakeEdgesString() {
        return fakeEdgesString;
    }

    public void setFakeEdgesString(List<String> fakeEdgesString) {
        this.fakeEdgesString = fakeEdgesString;
    }

    public boolean isTrackEdges() {
        return isTrackEdges;
    }

    public void setTrackEdges(boolean trackEdges) {
        isTrackEdges = trackEdges;
    }
}
