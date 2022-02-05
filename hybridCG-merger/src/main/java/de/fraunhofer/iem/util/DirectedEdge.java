package de.fraunhofer.iem.util;

import java.io.Serializable;

public class DirectedEdge implements Serializable {
    private static final long serialVersionUID = 6529685099967757690L;

    private String source;
    private String destination;
    private String associatedCallSite;
    private int associatedCallSiteLineNumber;
    private boolean isFakeEdge;
    private boolean isCallSiteSameAsCaller;

    public DirectedEdge(String source,
                        String destination,
                        String associatedCallSite,
                        int associatedCallSiteLineNumber,
                        boolean isFakeEdge,
                        boolean isCallSiteSameAsCaller) {
        this.source = source;
        this.destination = destination;
        this.associatedCallSite = associatedCallSite;
        this.associatedCallSiteLineNumber = associatedCallSiteLineNumber;
        this.isFakeEdge = isFakeEdge;
        this.isCallSiteSameAsCaller = isCallSiteSameAsCaller;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getAssociatedCallSite() {
        return associatedCallSite;
    }

    public void setAssociatedCallSite(String associatedCallSite) {
        this.associatedCallSite = associatedCallSite;
    }

    public int getAssociatedCallSiteLineNumber() {
        return associatedCallSiteLineNumber;
    }

    public void setAssociatedCallSiteLineNumber(int associatedCallSiteLineNumber) {
        this.associatedCallSiteLineNumber = associatedCallSiteLineNumber;
    }

    public boolean isFakeEdge() {
        return isFakeEdge;
    }

    public void setFakeEdge(boolean fakeEdge) {
        isFakeEdge = fakeEdge;
    }

    public boolean isCallSiteSameAsCaller() {
        return isCallSiteSameAsCaller;
    }

    public void setCallSiteSameAsCaller(boolean callSiteSameAsCaller) {
        isCallSiteSameAsCaller = callSiteSameAsCaller;
    }
}
