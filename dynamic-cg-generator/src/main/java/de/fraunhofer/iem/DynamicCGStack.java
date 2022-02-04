package de.fraunhofer.iem;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;

public class DynamicCGStack {
    private static final LinkedList<HashMap<CallType, String>> stack = new LinkedList<>();

    public static void methodCall(String methodSignature) {
        HashMap<CallType, String> temp = new HashMap<>();
        temp.put(CallType.MethodCall, methodSignature);
        stack.add(temp);
    }

    public static void returnCall(String methodSignature) {
        HashMap<CallType, String> temp = new HashMap<>();
        temp.put(CallType.ReturnCall, methodSignature);
        stack.add(temp);
    }

    public static void getStackTrace() {
        System.out.println(Arrays.toString(Thread.currentThread().getStackTrace()).replaceAll(",", "\n"));
    }

    public static void printStackTrace() {
        System.out.println(stack.toString().replaceAll(",", "\n"));
    }
}
