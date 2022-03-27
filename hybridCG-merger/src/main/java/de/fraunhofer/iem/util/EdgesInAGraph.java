package de.fraunhofer.iem.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;

public class EdgesInAGraph implements Serializable {
    private static final long serialVersionUID = 6529685028967757690L;

    private String name;
    private final HashSet<DirectedEdge> directedEdges = new HashSet<>();
    private final SerializableDotGraph serializableDotGraph;

    public EdgesInAGraph(String name, SerializableDotGraph serializableDotGraph) {
        this.name = name;
        this.serializableDotGraph = serializableDotGraph;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public HashSet<DirectedEdge> getDirectedEdges() {
        return directedEdges;
    }

    public void addDirectedEdge(DirectedEdge directedEdges) {
        this.directedEdges.add(directedEdges);
    }

    public SerializableDotGraph getSerializableDotGraph() {
        return serializableDotGraph;
    }
}
