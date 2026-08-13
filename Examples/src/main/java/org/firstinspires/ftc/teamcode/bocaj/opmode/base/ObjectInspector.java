package org.firstinspires.ftc.teamcode.bocaj.opmode.base;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

public final class ObjectInspector {
    public static Map<String, Object> inspect(Object root) {
        Map<String, Object> result = new LinkedHashMap<>();
        Set<Object> visited = Collections.newSetFromMap(new IdentityHashMap<>());

        inspect(root, "", result, visited);
        return result;
    }

    private static void inspect(
            Object obj,
            String path,
            Map<String, Object> result,
            Set<Object> visited
    ) {
        if (obj == null) {
            result.put(path, null);
            return;
        }

        Class<?> clazz = obj.getClass();

        // Primitive-ish types
        if (isLeaf(clazz)) {
            result.put(path, obj);
            return;
        }

        // Prevent infinite recursion
        if (!visited.add(obj)) {
            result.put(path, "<circular reference>");
            return;
        }

        // Arrays
        if (clazz.isArray()) {
            int length = Array.getLength(obj);
            for (int i = 0; i < length; i++) {
                inspect(Array.get(obj, i), path + "[" + i + "]", result, visited);
            }
            return;
        }

        // Collections
        if (obj instanceof Collection<?>) {
            int i = 0;
            for (Object value : (Collection<?>) obj) {
                inspect(value, path + "[" + i++ + "]", result, visited);
            }
            return;
        }

        // Maps
        if (obj instanceof Map<?, ?>) {
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) obj).entrySet()) {
                inspect(
                        entry.getValue(),
                        path + "[" + String.valueOf(entry.getKey()) + "]",
                        result,
                        visited
                );
            }
            return;
        }

        // Objects
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            for (Field field : current.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers())
                        || field.isSynthetic()) {
                    continue;
                }

                field.setAccessible(true);

                try {
                    Object value = field.get(obj);

                    String fieldPath = path.isEmpty()
                            ? field.getName()
                            : path + "." + field.getName();

                    inspect(value, fieldPath, result, visited);
                } catch (IllegalAccessException ignored) {
                }
            }

            current = current.getSuperclass();
        }
    }

    private static boolean isLeaf(Class<?> clazz) {
        return clazz.isPrimitive()
                || Number.class.isAssignableFrom(clazz)
                || CharSequence.class.isAssignableFrom(clazz)
                || Boolean.class == clazz
                || Character.class == clazz
                || Enum.class.isAssignableFrom(clazz)
                || Date.class.isAssignableFrom(clazz)
                || UUID.class == clazz
                || clazz.getPackage() != null
                && clazz.getPackage().getName().startsWith("java.time");
    }
}