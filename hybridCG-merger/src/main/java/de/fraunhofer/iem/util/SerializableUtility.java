package de.fraunhofer.iem.util;

import java.io.*;

/**
 * Utility class to serialize and deserialize dynamic traces (EdgesInAGraph object)
 *
 * @author Ranjith Krishnamurthy
 */
public class SerializableUtility {
    public static void serialize(EdgesInAGraph edgesInAGraph, String filename) {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(filename + ".ser");
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);

            objectOutputStream.writeObject(edgesInAGraph);

            objectOutputStream.close();
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static EdgesInAGraph deSerialize(String filename) {
        EdgesInAGraph edgesInAGraph = null;

        try {
            FileInputStream fileInputStream = new FileInputStream(filename);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);

            edgesInAGraph = (EdgesInAGraph) objectInputStream.readObject();

            fileInputStream.close();
            objectInputStream.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return edgesInAGraph;
    }
}
