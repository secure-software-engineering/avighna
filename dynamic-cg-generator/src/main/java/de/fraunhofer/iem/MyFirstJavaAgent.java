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
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;

public class MyFirstJavaAgent implements ClassFileTransformer {
    private static List<String> exclude = new ArrayList<>();
    private static HashSet<String> controller = new HashSet<>();

    private static final String beginClass = "org/springframework/samples/petclinic/owner/OwnerController";
    private static boolean isEnhance = true;
    private static int counter = 0;
    private static final int MAX_COUNTER = 20;

    private String applicationRootPackage;

    public MyFirstJavaAgent(String applicationRootPackage) {
        this.applicationRootPackage = applicationRootPackage;
    }

    static {
        exclude.add("java/");
        exclude.add("de/fraunhofer/iem/DynamicCGStack");
        exclude.add("de/fraunhofer/iem/DynamicCallStack");
        exclude.add("de/fraunhofer/iem/DynamicCallStackManager");
        exclude.add("sun/");
        exclude.add("org/springframework/boot/loader/");
        exclude.add("ch/qos/logback/");
        exclude.add("org/slf4j/");
        exclude.add("org/springframework/util/StringUtils");
        exclude.add("org/springframework/util/Assert");
        exclude.add("org/springframework/util/");
        exclude.add("org/springframework/util/");
        exclude.add("org/apache/commons/logging/");
        exclude.add("de/fraunhofer/iem/springbench/bean/configurations/MyConfiguration$$EnhancerBySpringCGLIB");
    //    exclude.add("java/nio/");
   //     exclude.add("org/springframework/boot/loader");
    //    exclude.add("sun/invoke");
   //     exclude.add("org/springframework/util");
   //     exclude.add("org/springframework/core/annotation/AnnotationUtils");
   //     exclude.add("ch/qos/logback");
   //     exclude.add("org/springframework/asm");
   //     exclude.add("org/apache/");
   //     exclude.add("com/sun/");
   //     exclude.add("org/springframework/core/MethodClassKey");
   //     exclude.add("org/springframework/boot");
   //     exclude.add("org/springframework/core");
   //     exclude.add("org/hibernate");
   //     exclude.add("org/hsqldb");
   //     exclude.add("antlr");
   //     exclude.add("jdk");
   //     exclude.add("org/thymeleaf");

        controller.add("initCreationForm");
        controller.add("processCreationForm");
        controller.add("initFindForm");
        controller.add("processFindForm");
        controller.add("initUpdateOwnerForm");
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        //System.out.println("ClassName = " + className);

  //      if (className.equals(beginClass)) {
    //        return enhanceClass(className, classfileBuffer);
      //  }


    //    System.out.println("Transform = " + className);

        if (className.startsWith(this.applicationRootPackage)) {
        //    isEnhance = true;
            for (String excludeString : exclude) {
                if (className.startsWith(excludeString)) {
                    if (className.contains("DynamicCG")) {
                        System.out.println("Excluding = " + className);
                    }

            //        System.out.println("Returning 3");
                    return classfileBuffer;
                }
            }

         //   System.out.println("Returning 1");
            return enhanceClass(className, classfileBuffer);
        }

      //  System.out.println("Returning 2");
        return classfileBuffer;
    }

    private byte[] enhanceClass(String className, byte[] classfileBuffer) {
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
//                    if (className.startsWith("org/springframework/samples/petclinic/owner/OwnerController")) {
  //                      System.out.println("\n\n\n" + method.getSignature() + "\n\n\n");
    //                    System.out.println("\n\n\n" + method.getGenericSignature() + "\n\n\n");
      //                  System.out.println("\n\n\n" + className + "\n\n\n");
        //                System.out.println("\n\n\n" + clazz.getName() + "\n\n\n");
  //                  }
                   /* String beforeCode = "try {\n" +
                            "  \n" +
                            "            java.io.BufferedWriter out = new java.io.BufferedWriter(new java.io.FileWriter(\"stats.txt\", true));\n" +
                            "            out.write(\"--> " + clazz.getName() + "." + method.getName() + "\\n\");" +
                            "            out.close();\n" +
                            "        }\n" +
                            "        catch (java.io.IOException e) {\n" +
                            "            System.out.println(\"exception occurred\" + e);\n" +
                            "        }";
*/

                    if (clazz.getName().startsWith("org.springframework.util.StringUtils")) {
                        Scanner reader = new Scanner(System.in);  // Reading from System.in
                        System.out.println("Enter a number: ");
                        int n = reader.nextInt();
                    }

                    String beforeCode = "de.fraunhofer.iem.DynamicCallStackManager.methodCall(\"" + clazz.getName() + "." + method.getName() + "\");";

             //       String beforeCode = "de.fraunhofer.iem.DynamicCGStack.getStackTrace();";
/*
                    if (clazz.getName().contains("org/springframework/samples/petclinic")) {
                        System.out.println("\n\n\n" + method.getName() + "\n\n\n");
                        System.out.println("\n\n\n" + clazz.getName() + "\n\n\n");
                    }
                    if (controller.contains(method.getName())) {
                        System.out.println("\n\n\nEntered here\n\n\n");
                        beforeCode += "\n" +
                                "de.fraunhofer.iem.DynamicCGStack.printStackTrace()";
                    }
*/
                    method.insertBefore(beforeCode);

                /*    String afterCode = "try {\n" +
                            "  \n" +
                            "            java.io.BufferedWriter out = new java.io.BufferedWriter(new java.io.FileWriter(\"stats.txt\", true));\n" +
                            "            out.write(\"<-- " + clazz.getName() + "." + method.getName() + "\\n\");" +
                            "            out.close();\n" +
                            "        }\n" +
                            "        catch (java.io.IOException e) {\n" +
                            "            System.out.println(\"exception occurred\" + e);\n" +
                            "        }";
*/
                    String afterCode = "de.fraunhofer.iem.DynamicCallStackManager.methodReturn(\"" + clazz.getName() + "." + method.getName() + "\");";

    //                if (counter == MAX_COUNTER) {
      //                  afterCode += "\nde.fraunhofer.iem.DynamicCGStack.printStackTrace();";
        //                counter = 0;
          //          } else {
            //            counter++;
              //      }

                    method.insertAfter(afterCode);
                }

                //CtBehavior[] methods = clazz.getDeclaredBehaviors();

            //    System.out.println("\n\n\n ---> " + methods.length + "\n\n\n");

//                for (CtBehavior method : methods) {
  //                  if (!method.isEmpty()) {
    //                    System.out.println("\n\n\n ---> " + method.getSignature() + "\n\n\n");
      //              }
        //        }

                byteCode = clazz.toBytecode();
            }
        } catch (CannotCompileException | IOException e) {
            try {

                e.printStackTrace();
                // Open given file in append mode.
                BufferedWriter out = new BufferedWriter(
                        new FileWriter("error.txt", true));
                out.write(className + " ----- " + e.getClass().getName() + "---" + e.getMessage() + "\n");
                out.close();
            } catch (IOException ex) {
                System.out.println("exception occurred" + e);
            }

            //System.out.println("\t* " + e.getClass().getName());
            // e.printStackTrace();
            // System.out.println(className);
            return classfileBuffer;
        } finally {
            if (clazz != null) {
                clazz.detach();
            }
        }
        return byteCode;
    }

    private void enhanceMethod(CtBehavior method, String className)
            throws NotFoundException, CannotCompileException {
        String name = className.substring(className.lastIndexOf('.') + 1, className.length());
        String methodName = method.getName();

        if (method.getName().equals(name))
            methodName = "<init>";

        method.insertBefore("System.out.println(\"--> \" + \"className\" + \" : \" + \"methodName\" + \" == \" + System.nanoTime());");
        method.insertAfter("System.out.println(\"<-- \" + \"className\" + \" : \" + \"methodName\" + \" == \" + System.nanoTime());");
    }
}
