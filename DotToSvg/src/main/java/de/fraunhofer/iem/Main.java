package de.fraunhofer.iem;

import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;

import java.io.File;
import java.io.IOException;

/**
 * Wrapper project to generate the SVG file for the DOT graph to use in the dynamic-cg-generator project.
 *
 * @author Ranjith Krishnamurthy
 */
public class Main {
    public static void main(String[] args) {
        String dotFileName = args[0];
        String outputImageFileName = args[1];

        try {
            Graphviz.fromFile(new File(dotFileName)).render(Format.SVG).toFile(new File(outputImageFileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
