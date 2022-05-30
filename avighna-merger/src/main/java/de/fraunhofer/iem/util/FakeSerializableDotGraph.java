package de.fraunhofer.iem.util;

import soot.util.dot.DotGraph;

/**
 * Wrapper class to produce serializable DotGraph
 *
 * @author Ranjith Krishnamurthy
 */
public class FakeSerializableDotGraph extends DotGraph {
    public FakeSerializableDotGraph() {
        super("callgraph");
    }
}
