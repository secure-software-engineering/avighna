package de.fraunhofer.iem;

import de.fraunhofer.iem.util.LoggerUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class DynamicCallStackManager {
    private static final List<DynamicCallStack> myDynamicCallStack = Collections.synchronizedList(new ArrayList<>());

    private static DynamicCallStack getDynamicCallStack() {
        long id = Thread.currentThread().getId();

        for (DynamicCallStack dynamicCallStack : myDynamicCallStack) {
            if (dynamicCallStack.getPid() == id) {
                return dynamicCallStack;
            }
        }

        return null;
    }

    public static void methodCall(String methodSignature, boolean isLibraryCall) {
        DynamicCallStack dynamicCallStack = getDynamicCallStack();

        if (dynamicCallStack == null) {
            if (isLibraryCall) return;

            dynamicCallStack = new DynamicCallStack(Thread.currentThread().getId());
            myDynamicCallStack.add(dynamicCallStack);
            LoggerUtil.LOGGER.log(Level.INFO, "ENTERED");
            System.out.println(System.identityHashCode("----> " + DynamicCallStackManager.class.getClassLoader()));
        }

        if (isLibraryCall) {
            dynamicCallStack.libraryCall(methodSignature);
        } else {
            dynamicCallStack.methodCall(methodSignature);
        }
    }

    public static void methodReturn(String methodSignature, boolean isLibraryCall) {
        DynamicCallStack dynamicCallStack = getDynamicCallStack();

        if (dynamicCallStack == null) {
            if (isLibraryCall) return;

            dynamicCallStack = new DynamicCallStack(Thread.currentThread().getId());
        }

        if (isLibraryCall) {
            dynamicCallStack.libraryReturnCall(methodSignature);
        } else {
            dynamicCallStack.methodReturn(methodSignature);
        }
    }

    public static void writeForcefully() {
        DynamicCallStack dynamicCallStack = getDynamicCallStack();

        if (dynamicCallStack == null) {
            dynamicCallStack = new DynamicCallStack(Thread.currentThread().getId());
            myDynamicCallStack.add(dynamicCallStack);
        }

        dynamicCallStack.writeRequest();
    }
}
