package de.fraunhofer.iem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
            if (isLibraryCall)
                return;

            dynamicCallStack = new DynamicCallStack(Thread.currentThread().getId());
            myDynamicCallStack.add(dynamicCallStack);
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
            if (isLibraryCall)
                return;

            dynamicCallStack = new DynamicCallStack(Thread.currentThread().getId());
     //       myDynamicCallStack.add(dynamicCallStack);
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
