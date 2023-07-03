package com.admin4j.framework.mybatis.util;

import org.apache.commons.lang3.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author andanyang
 * @since 2023/7/3 10:51
 */
public class MapperAnnotationUtil {

    private static Map<String, Object> annotationCache = new ConcurrentHashMap<>();
    private static final Object EMPTY_Annotation = new Object();

    public static <T extends Annotation> T getAnnotationById(String id, Class<T> anClass) throws ClassNotFoundException {

        Object cacheObject = annotationCache.get(id);
        if (EMPTY_Annotation.equals(cacheObject)) {
            return null;
        }
        T annotation = (T) cacheObject;
        if (annotation != null) {
            return annotation;
        }

        String className = StringUtils.substringBeforeLast(id, ".");
        String methodName = StringUtils.substringAfterLast(id, ".");

        Class<?> aClass = MapperAnnotationUtil.class.getClassLoader().loadClass(className);
        Method[] methods = aClass.getMethods();
        for (Method method : methods) {
            if (method.getName().equals(methodName)) {
                annotation = method.getAnnotation(anClass);
                break;
            }
        }
        if (annotation == null) {
            annotation = aClass.getAnnotation(anClass);
        }

        annotationCache.put(id, annotation == null ? EMPTY_Annotation : annotation);
        return annotation;
    }
}
