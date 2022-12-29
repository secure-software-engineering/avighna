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
    private boolean isGodEdge;

    public DirectedEdge(String source,
                        String destination,
                        String associatedCallSite,
                        int associatedCallSiteLineNumber,
                        boolean isFakeEdge,
                        boolean isCallSiteSameAsCaller,
                        boolean isGodEdge) {
        this.source = source;
        this.destination = destination;
        this.associatedCallSite = associatedCallSite;
        this.associatedCallSiteLineNumber = associatedCallSiteLineNumber;
        this.isFakeEdge = isFakeEdge;
        this.isCallSiteSameAsCaller = isCallSiteSameAsCaller;
        this.isGodEdge = isGodEdge;
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

    public boolean isGodEdge() {
        return isGodEdge;
    }

    public void setGodEdge(boolean godEdge) {
        isGodEdge = godEdge;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((source == null) ? 0 : source.hashCode());
        result = prime * result + ((destination == null) ? 0 : destination.hashCode());
        result = prime * result + ((associatedCallSite == null) ? 0 : associatedCallSite.hashCode());
        result = prime * result + Integer.hashCode(associatedCallSiteLineNumber);
        result = prime * result + Boolean.hashCode(isFakeEdge);
        result = prime * result + Boolean.hashCode(isCallSiteSameAsCaller);
        result = prime * result + Boolean.hashCode(isGodEdge);
        return result;
    }

    @Override
    public String toString() {
        return super.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;

        DirectedEdge other = (DirectedEdge) obj;

        if (source == null) {
            if (other.getSource() != null)
                return false;
        } else if (!source.equals(other.getSource()))
            return false;

        if (destination == null) {
            if (other.getDestination() != null)
                return false;
        } else if (!destination.equals(other.getDestination()))
            return false;

        if (associatedCallSite == null) {
            if (other.getAssociatedCallSite() != null)
                return false;
        } else if (!associatedCallSite.equals(other.getAssociatedCallSite()))
            return false;

        if (associatedCallSiteLineNumber != other.getAssociatedCallSiteLineNumber())
            return false;

        if (isFakeEdge != other.isFakeEdge())
            return false;

        if (isCallSiteSameAsCaller != other.isCallSiteSameAsCaller())
            return false;

        return isGodEdge == other.isGodEdge();
    }
}
