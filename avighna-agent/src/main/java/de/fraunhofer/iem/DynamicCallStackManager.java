package de.fraunhofer.iem;

import de.fraunhofer.iem.util.LoggerUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

/**
 * Manager to mange the Dynamic call stack, this will be instrumented in the application code to generate Dynamic CG
 *
 * @author Ranjith Krishnamurthy
 */
public class DynamicCallStackManager {
    private static final List<DynamicCallStack> myDynamicCallStack = Collections.synchronizedList(new ArrayList<>());

    /**
     * Returns the Dynamic CG of the given thread if present otherwise returns null
     *
     * @return Dynamic CG if present otherwise returns null
     */
    private static DynamicCallStack getDynamicCallStack(long id) {
        synchronized (myDynamicCallStack) {
            for (DynamicCallStack dynamicCallStack : myDynamicCallStack) {
                if (dynamicCallStack.getPid() == id) {
                    return dynamicCallStack;
                }
            }
        }
        return null;
    }

    /**
     * Method call of the application/library code
     *
     * @param methodSignature Method signature
     * @param isLibraryCall   Is the method a library code
     */
    public static void methodCall(String methodSignature, boolean isLibraryCall) {
        try {
            DynamicCallStack dynamicCallStack = getDynamicCallStack(Thread.currentThread().getId());

            if (dynamicCallStack == null) {
                if (isLibraryCall) return;

                dynamicCallStack = new DynamicCallStack(Thread.currentThread().getId());
                myDynamicCallStack.add(dynamicCallStack);
                LoggerUtil.getLOGGER().info("Created new Dynamic Call stack for the process id = " + Thread.currentThread().getId());

                long id = Thread.currentThread().getId();
                Thread thread = new Thread(() -> writeForcefully(id));
                Runtime.getRuntime().addShutdownHook(thread);
            }

            if (isLibraryCall) {
                dynamicCallStack.libraryCall(methodSignature);
            } else {
                dynamicCallStack.methodCall(methodSignature);
            }
        } catch (Exception | Error e) {
            LoggerUtil.getLOGGER().log(Level.SEVERE, "Error (" + e.getClass().getName() + ") = " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Method return call of the application/library code
     *
     * @param methodSignature Method signature
     * @param isLibraryCall   Is the method a library code
     */
    public static void methodReturn(String methodSignature, boolean isLibraryCall) {
        try {
            DynamicCallStack dynamicCallStack = getDynamicCallStack(Thread.currentThread().getId());

            if (dynamicCallStack == null) {
                if (isLibraryCall) return;

                dynamicCallStack = new DynamicCallStack(Thread.currentThread().getId());
                LoggerUtil.getLOGGER().log(Level.WARNING, "Created new Dynamic Call stack for the process id = " + Thread.currentThread().getId() +
                        "However, it happened in methodReturn, Please check the logic.");

                long id = Thread.currentThread().getId();
                Thread thread = new Thread(() -> writeForcefully(id));
                Runtime.getRuntime().addShutdownHook(thread);
            }

            if (isLibraryCall) {
                dynamicCallStack.libraryReturnCall(methodSignature);
            } else {
                dynamicCallStack.methodReturn(methodSignature);
            }
        } catch (Exception | Error e) {
            LoggerUtil.getLOGGER().log(Level.SEVERE, "Error = " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * API that allows user to forcefully write the Dynamic CG to the file
     */
    public static void writeForcefully(long id) {
        DynamicCallStack dynamicCallStack = getDynamicCallStack(id);

        if (dynamicCallStack == null) {
            dynamicCallStack = new DynamicCallStack(id);
            myDynamicCallStack.add(dynamicCallStack);
            LoggerUtil.getLOGGER().log(Level.WARNING, "Created new Dynamic Call stack for the process id = " + Thread.currentThread().getId() +
                    "However, it happened in writeForcefully, Please check the logic.");
        }

        dynamicCallStack.writeRequest();
    }
}
