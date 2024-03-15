package de.fraunhofer.iem.avighna.util;

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
