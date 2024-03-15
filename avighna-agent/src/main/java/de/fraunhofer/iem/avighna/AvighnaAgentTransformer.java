package de.fraunhofer.iem.avighna;

import de.fraunhofer.iem.avighna.util.LoggerUtil;
import javassist.bytecode.Descriptor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;
import java.util.*;
import java.util.logging.Level;

import static org.objectweb.asm.Opcodes.*;

public class AvighnaAgentTransformer implements ClassFileTransformer {
    private final List<String> exclude = new ArrayList<>();
    private final List<String> rootPackageNameOfApplication;
    private final String rootOutputDirectory;
    public final Set<String> fakeEdges = new HashSet<>();
    public final boolean isTrackEdges;

    public AvighnaAgentTransformer(
            List<String> rootPackageNameOfApplication,
            String rootOutputDirectory,
            List<String> excludeClasses,
            List<String> fakeEdges,
            boolean isTrackEdges) {
        this.rootPackageNameOfApplication = rootPackageNameOfApplication;
        this.rootOutputDirectory = rootOutputDirectory;
        this.isTrackEdges = isTrackEdges;
        this.exclude.addAll(excludeClasses);
        this.exclude.add("de/fraunhofer/iem/avighna/DynamicCGStack");
        this.exclude.add("de/fraunhofer/iem/avighna/DynamicCallStack");
        this.exclude.add("de/fraunhofer/iem/avighna/DynamicCallStackManager");
        this.exclude.add("de/fraunhofer/iem/avighna/DynamicCGAgent");
        this.exclude.add("de/fraunhofer/iem/avighna/CallType");
        this.exclude.add("de/fraunhofer/iem/avighna/MyFirstJavaAgent");
        this.exclude.add("de/fraunhofer/iem/avighna/hybridCG/HybridCallGraph");
        this.exclude.add("de/fraunhofer/iem/avighna/util/DirectedEdge");
        this.exclude.add("de/fraunhofer/iem/avighna/util/EdgesInAGraph");
        this.exclude.add("de/fraunhofer/iem/avighna/util/FakeSerializableDotGraph");
        this.exclude.add("de/fraunhofer/iem/avighna/util/SerializableDotGraph");
        this.exclude.add("de/fraunhofer/iem/avighna/util/SerializableUtility");
        this.exclude.add("de/fraunhofer/iem/avighna/util/LoggerUtil");
        this.exclude.add("com/code_intelligence/jazzer");
        this.exclude.add("com/sun/tools/attach");
        this.exclude.add("sun/tools/attach");
        this.exclude.add("sun/reflect");
        this.exclude.add("sun/jvmstat/monitor");
        this.exclude.add("javassist/compiler");
        this.exclude.add("sun/jvmstat/perfdata/monitor");
        //TODO: the below will cause termination of the tool, test it and fix it
//        exclude.add("de/fraunhofer/iem/springbench/bean/configurations/MyConfiguration$$EnhancerBySpringCGLIB");

        this.fakeEdges.addAll(fakeEdges);
    }

    public String generateMethodSignature(String className,String methodName, String methodDesc){
        return className.replaceAll("/", ".") +
                ": " +
                extractReturnType(methodDesc) +
                " " +
                methodName+extractParameterList(methodDesc);
    }

    public String extractReturnType(String methodDesc){
        if (methodDesc == null)
            return "NA";

        int startIndex = methodDesc.indexOf(")");

        if(startIndex < 0){
            return "NA";
        }

        return Descriptor.toString(methodDesc.substring(startIndex + 1));
    }

    public String extractParameterList(String methodDesc){
        if (methodDesc == null)
            return "NA";

        return Descriptor.toString(methodDesc);
    }

    private boolean isFakeEdge(String className) {
        if (className.contains("$")) {
            for (String rootPackage : this.rootPackageNameOfApplication) {
                if (className.startsWith(rootPackage)) {
                    return true;
                }
            }
        }

        for (String fakeEdge : fakeEdges) {
            if (className.contains(fakeEdge)) {
                for (String rootPackage : this.rootPackageNameOfApplication) {
                    if (className.startsWith(rootPackage)) {
                        return true;
                    }
                }
            }
        }

        return false;
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

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) {
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

        for (String rootPackage : this.rootPackageNameOfApplication) {
            if (className.startsWith(rootPackage)) {
                return enhanceClass(className, classfileBuffer, false);
            } else {
                return enhanceClass(className, classfileBuffer, true);
            }
        }

        return classfileBuffer;
    }

    private byte[] enhanceClass(String className, byte[] classfileBuffer, boolean isLibraryCall) {
        String currentlyProcessingMethod = "";

        ClassReader reader = new ClassReader(classfileBuffer);
        ClassNode classNode=new ClassNode(ASM4);
        reader.accept(classNode, 0);

        List<MethodNode> methods = classNode.methods;

        LoggerUtil.getLOGGER().info("Instrumenting = " + className);
        for(MethodNode method: methods) {
            if ((method.access & ACC_ABSTRACT) != 0 && method.instructions.size() == 0)
                continue;

            if ((method.access & ACC_NATIVE) != 0)
                continue;

            InsnList instructions = method.instructions;
            Iterator<?> itr = instructions.iterator();

            currentlyProcessingMethod = generateMethodSignature(className,method.name,method.desc);

            InsnList instForOnMethodEntry = new InsnList();
            instForOnMethodEntry.add(new LdcInsnNode(currentlyProcessingMethod));
            instForOnMethodEntry.add(new LdcInsnNode(isLibraryCall));
            instForOnMethodEntry.add(
                    new MethodInsnNode(
                            INVOKESTATIC,
                            "de/fraunhofer/iem/avighna/DynamicCallStackManager",
                            "methodCall",
                            "(Ljava/lang/String;Z)V"));
            instructions.insert(instForOnMethodEntry);
            method.maxStack +=2;

            while(itr.hasNext()) {
                AbstractInsnNode singleInstruction = (AbstractInsnNode) itr.next();
                int opCode = singleInstruction.getOpcode();

                if ((opCode >= IRETURN && opCode <= RETURN) || opCode == ATHROW) {
                    InsnList instForOnMethodExit = new InsnList();

                    instForOnMethodExit.add(new LdcInsnNode(currentlyProcessingMethod));
                    instForOnMethodExit.add(new LdcInsnNode(isLibraryCall));
                    instForOnMethodExit.add(
                            new MethodInsnNode(
                                    INVOKESTATIC,
                                    "de/fraunhofer/iem/avighna/DynamicCallStackManager",
                                    "methodReturn",
                                    "(Ljava/lang/String;Z)V"));
                    instructions.insert(singleInstruction.getPrevious(), instForOnMethodExit);
                    method.maxStack +=2;
                }
            }
        }


        ClassWriter out=new ClassWriter(0);
        classNode.accept(out);
        return out.toByteArray();
    }
}
