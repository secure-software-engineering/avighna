package de.fraunhofer.iem.util;

import soot.util.dot.DotGraph;

public class FakeSerializableDotGraph extends DotGraph {
    public FakeSerializableDotGraph() {
        super("callgraph");
    }
}
