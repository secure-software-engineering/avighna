package de.fraunhofer.iem;

import de.fraunhofer.iem.util.LoggerUtil;
import javassist.*;

import java.io.*;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

/**
 * This transformer instrument the code to generate Dynamic CG
 *
 * @author Ranjith Krishnamurthy
 */
public class AgentTransformer implements ClassFileTransformer {
    private final List<String> exclude = new ArrayList<>();
    private final String rootPackageNameOfApplication;
    private final String rootOutputDirectory;
    public final Set<String> fakeEdges = new HashSet<>();
    public final boolean isTrackEdges;

    public AgentTransformer(
            String rootPackageNameOfApplication,
            String rootOutputDirectory,
            List<String> excludeClasses,
            List<String> fakeEdges,
            boolean isTrackEdges) {
        this.rootPackageNameOfApplication = rootPackageNameOfApplication;
        this.rootOutputDirectory = rootOutputDirectory;
        this.isTrackEdges = isTrackEdges;
        this.exclude.addAll(excludeClasses);
        this.exclude.add("de/fraunhofer/iem/DynamicCGStack");
        this.exclude.add("de/fraunhofer/iem/DynamicCallStack");
        this.exclude.add("de/fraunhofer/iem/DynamicCallStackManager");
        this.exclude.add("de/fraunhofer/iem/DynamicCGAgent");
        this.exclude.add("de/fraunhofer/iem/CallType");
        this.exclude.add("de/fraunhofer/iem/MyFirstJavaAgent");
        this.exclude.add("de/fraunhofer/iem/hybridCG/HybridCallGraph");
        this.exclude.add("de/fraunhofer/iem/util/DirectedEdge");
        this.exclude.add("de/fraunhofer/iem/util/EdgesInAGraph");
        this.exclude.add("de/fraunhofer/iem/util/FakeSerializableDotGraph");
        this.exclude.add("de/fraunhofer/iem/util/SerializableDotGraph");
        this.exclude.add("de/fraunhofer/iem/util/SerializableUtility");
        this.exclude.add("de/fraunhofer/iem/util/LoggerUtil");
        //TODO: the below will cause termination of the tool, test it and fix it
//        exclude.add("de/fraunhofer/iem/springbench/bean/configurations/MyConfiguration$$EnhancerBySpringCGLIB");

        this.fakeEdges.addAll(fakeEdges);
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        if (loader == null || className == null || className.equals("null")) {
            return classfileBuffer;
        }

        if (isFakeEdge(className)) {
            try {
                writeClassFile(className, classfileBuffer);
            } catch (Exception | Error e) {
                LoggerUtil.getLOGGER().log(Level.SEVERE, "ERROR creating class file for dynamic = " + e.getMessage());
                e.printStackTrace();
            }

            if (!isTrackEdges) {
                return classfileBuffer;
            }
        }

        for (String excludeString : exclude) {
            if (className.startsWith(excludeString)) {
                return classfileBuffer;
            }
        }

        if (className.startsWith(this.rootPackageNameOfApplication)) {
            return enhanceClass(className, classfileBuffer, false);
        } else {
            return enhanceClass(className, classfileBuffer, true);
        }
    }

    private void writeClassFile(String className, byte[] classFileBuffer) {
        if (className.contains("/")) {
            new File(rootOutputDirectory.replace("allDotFiles", "") + File.separator +
                    "dynamicCP" + File.separator +
                    className.substring(0, className.lastIndexOf("/"))).mkdirs();
        }

        File file = new File(rootOutputDirectory.replace("allDotFiles", "") + File.separator + "dynamicCP" + File.separator + className + ".class");

        if (file.exists()) file.delete();

        try {
            file.createNewFile();

            FileOutputStream fileOutputStream = new FileOutputStream(file);

            fileOutputStream.write(classFileBuffer);
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
            LoggerUtil.getLOGGER().log(Level.SEVERE, "ERROR generating class file for dynamic classes = " + e.getMessage());
        }

    }

    private byte[] enhanceClass(String className, byte[] classfileBuffer, boolean isLibraryCall) {
        String currentlyProcessingMethod = "";
        ClassPool pool = ClassPool.getDefault();
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        pool.insertClassPath(new LoaderClassPath(cl));
        CtClass clazz = null;
        byte[] byteCode = classfileBuffer;
        try {
            clazz = pool.makeClass(new ByteArrayInputStream(classfileBuffer));

            CtMethod[] methodss = clazz.getDeclaredMethods();
            for (CtMethod method : methodss) {
                currentlyProcessingMethod = generateSootMethodSignature(method, false, className);
                if (!method.isEmpty() && !Modifier.isNative(method.getModifiers())) {
                    String beforeCode = "de.fraunhofer.iem.DynamicCallStackManager.methodCall(\"" + currentlyProcessingMethod + "\"," + isLibraryCall + ");";
                    method.insertBefore(beforeCode);

                    String afterCode = "de.fraunhofer.iem.DynamicCallStackManager.methodReturn(\"" + currentlyProcessingMethod + "\"," + isLibraryCall + ");";
                    method.insertAfter(afterCode);
                }
            }

            CtConstructor[] constructorMethods = clazz.getConstructors();
            for (CtConstructor constructorMethod : constructorMethods) {
                currentlyProcessingMethod = generateSootMethodSignature(constructorMethod, true, className);
                String beforeCode = "de.fraunhofer.iem.DynamicCallStackManager.methodCall(\"" + currentlyProcessingMethod + "\"," + isLibraryCall + ");";
                constructorMethod.insertBefore(beforeCode);

                String afterCode = "de.fraunhofer.iem.DynamicCallStackManager.methodReturn(\"" + currentlyProcessingMethod + "\"," + isLibraryCall + ");";
                constructorMethod.insertAfter(afterCode);
            }

            byteCode = clazz.toBytecode();
        } catch (CannotCompileException | IOException e) {
            try {
                BufferedWriter out = new BufferedWriter(new FileWriter(
                        rootOutputDirectory + System.getProperty("file.separator") + "error.txt", true));
                out.write(currentlyProcessingMethod + " ----- " + e.getClass().getName() + "---" + e.getMessage() + "\n");
                out.close();
            } catch (IOException ex) {
                System.out.println("exception occurred" + e);
            }

            return classfileBuffer;
        } finally {
            if (clazz != null) {
                clazz.detach();
            }
        }

        return byteCode;
    }

    private boolean isFakeEdge(String methodSignature) {
        for (String fakeEdge : fakeEdges) {
            if (methodSignature.contains(fakeEdge)) {
                return true;
            }
        }

        return false;
    }

    private String generateSootMethodSignature(CtBehavior method, boolean isConstructor, String javassistClassName) {
        String className = javassistClassName.replaceAll("/", ".");
        String returnType = "NA";
        String methodName = "";
        String parametersType = "";

        if (isConstructor) {
            returnType = "void";

            methodName = "<init>";
        } else {
            try {
                returnType = ((CtMethod) method).getReturnType().getName();
            } catch (NotFoundException e) {
//                e.printStackTrace();
                //TODO: Record these so that user will know these methods return types are not found
            }

            methodName = method.getName();
        }

        parametersType = method.getLongName().split("\\(")[1].replace(")", "");

        return className +
                ": " +
                returnType +
                " " +
                methodName +
                "(" +
                parametersType +
                ")";
    }
}
