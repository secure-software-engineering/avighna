package de.fraunhofer.iem.util;

import java.io.Serializable;

/**
 * Wrapper class to produce serializable DotGraph
 *
 * @author Ranjith Krishnamurthy
 */
public class SerializableDotGraph extends FakeSerializableDotGraph implements Serializable {
    private static final long serialVersionUID = 6529685098267757690L;

    public SerializableDotGraph() {
        super();
    }
}
