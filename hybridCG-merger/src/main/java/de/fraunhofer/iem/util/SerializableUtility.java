package de.fraunhofer.iem.util;

import de.fraunhofer.iem.exception.DtsSerializeUtilException;

import java.io.*;

/**
 * Utility class to serialize and deserialize dynamic traces (EdgesInAGraph object)
 *
 * @author Ranjith Krishnamurthy
 */
public class SerializableUtility {
    /**
     * Serialize the given dynamic traces (EdgesInAGraph object)
     *
     * @param edgesInAGraph Dynamic Traces (EdgesInAGraph object)
     * @param filename      serialize DTS file location
     * @throws DtsSerializeUtilException Serializable utility failed to serialize DTS file
     */
    public static void serialize(EdgesInAGraph edgesInAGraph, String filename) throws DtsSerializeUtilException {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(filename + ".ser");
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);

            objectOutputStream.writeObject(edgesInAGraph);

            objectOutputStream.close();
            fileOutputStream.close();
        } catch (IOException e) {
            throw new DtsSerializeUtilException("Failed to serialize to DTS file. \nMessage = " + e.getMessage());
        }
    }

    /**
     * Deserialize the given dynamic traces (EdgesInAGraph object)
     *
     * @param filename serialized DTS file location
     * @return Dynamic Traces (EdgesInAGraph object)
     * @throws DtsSerializeUtilException Serializable utility failed to deserialize DTS file
     */
    public static EdgesInAGraph deSerialize(String filename) throws DtsSerializeUtilException {
        EdgesInAGraph edgesInAGraph = null;

        try {
            FileInputStream fileInputStream = new FileInputStream(filename);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);

            edgesInAGraph = (EdgesInAGraph) objectInputStream.readObject();

            fileInputStream.close();
            objectInputStream.close();
        } catch (IOException | ClassNotFoundException e) {
            throw new DtsSerializeUtilException("Failed to deserialize to DTS file. \nMessage = " + e.getMessage());
        }

        if (edgesInAGraph == null) {
            throw new DtsSerializeUtilException("Failed to deserialize to DTS file. Got null object after deserialization.");
        }

        return edgesInAGraph;
    }
}
