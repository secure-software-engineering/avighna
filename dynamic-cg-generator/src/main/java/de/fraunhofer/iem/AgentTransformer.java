package de.fraunhofer.iem;

import javassist.*;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.List;

/**
 * This transformer instrument the code to generate Dynamic CG
 *
 * @author Ranjith Krishnamurthy
 */
public class AgentTransformer implements ClassFileTransformer {
    private final List<String> exclude = new ArrayList<>();
    private final String rootPackageNameOfApplication;

    public AgentTransformer(String rootPackageNameOfApplication, List<String> excludeClasses) {
        this.rootPackageNameOfApplication = rootPackageNameOfApplication;
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
        //TODO: the below will cause termination of the tool, test it and fix it
//        exclude.add("de/fraunhofer/iem/springbench/bean/configurations/MyConfiguration$$EnhancerBySpringCGLIB");
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
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

    private byte[] enhanceClass(String className, byte[] classfileBuffer, boolean isLibraryCall) {
        String currentlyProcessingMethod = "";
        ClassPool pool = ClassPool.getDefault();
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        pool.insertClassPath(new LoaderClassPath(cl));
        CtClass clazz = null;
        byte[] byteCode = classfileBuffer;
        try {
            clazz = pool.makeClass(new ByteArrayInputStream(classfileBuffer));
            if (!clazz.isInterface()) {
                CtMethod[] methodss = clazz.getDeclaredMethods();
                for (CtMethod method : methodss) {
                    currentlyProcessingMethod = method.getLongName();
                    if (!method.isEmpty()) {
                        String beforeCode = "de.fraunhofer.iem.DynamicCallStackManager.methodCall(\"" + method.getLongName() + "\"," + isLibraryCall + ");";
                        method.insertBefore(beforeCode);

                        String afterCode = "de.fraunhofer.iem.DynamicCallStackManager.methodReturn(\"" + method.getLongName() + "\"," + isLibraryCall + ");";
                        method.insertAfter(afterCode);
                    }
                }

                byteCode = clazz.toBytecode();
            }
        } catch (CannotCompileException | IOException e) {
            try {
                BufferedWriter out = new BufferedWriter(new FileWriter("error.txt", true));
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
}
