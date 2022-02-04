package de.fraunhofer.iem;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.net.JarURLConnection;
import java.util.Scanner;

/**
 * Agent for generating Dynamic CG
 *
 * @author Ranjith Krishnamurthy
 */
public class DynamicCGAgent {
    public static void premain(String argument, Instrumentation instrumentation) throws IOException {
        JarURLConnection connection = (JarURLConnection) DynamicCGAgent.class.getResource("DynamicCGAgent.class").openConnection();
        instrumentation.appendToBootstrapClassLoaderSearch(connection.getJarFile());

        String fileName = "stats.txt";

        try {
            File file = new File(fileName);
            FileWriter fileWriter = new FileWriter(file);

            //System.out.println(file.getAbsolutePath());
            BufferedWriter out = new BufferedWriter(fileWriter);
            out.write("Dynamic Call Stack:\n");
            out.close();
        } catch (IOException e) {
            System.out.println("Exception Occurred" + e);
        }

        String errorFileName = "error.txt";
        try {
            File file = new File(fileName);
            FileWriter fileWriter = new FileWriter(file);

            //System.out.println(file.getAbsolutePath());
            BufferedWriter out = new BufferedWriter(fileWriter);
            out.write("Dynamic Call Stack (ERROR):\n");
            out.close();
        } catch (IOException e) {
            System.out.println("Exception Occurred" + e);
        }

        System.out.print("Enter the application's root package: ");
        String applicationRootPackage = new Scanner(System.in).
                next().
                replaceAll("\\.", "/") + "/";
        instrumentation.addTransformer(new MyFirstJavaAgent(applicationRootPackage));
/*
        for (Class cls : instrumentation.getAllLoadedClasses()) {
            try {
                instrumentation.retransformClasses(cls);
            } catch (UnmodifiableClassException e) {
                System.out.println("Cant transform: " + cls.getName());
            }
        }*/
    }

    public static void agentmain(String argument, Instrumentation instrumentation) {
        System.out.println("Started agent-main");
    }
}
