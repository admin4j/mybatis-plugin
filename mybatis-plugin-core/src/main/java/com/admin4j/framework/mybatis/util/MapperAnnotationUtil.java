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

    private static Map<String, Map<Class<?>, Object>> annotationCache = new ConcurrentHashMap<>();
    private static final Object EMPTY_Annotation = new Object();

    private static Object getCache(String msId, Class<?> anClass) {
        Map<Class<?>, Object> map = annotationCache.get(msId);
        if (map == null) {
            return null;
        }
        return map.get(anClass);
    }

    private static Object putCache(String msId, Class<?> anClass, Object value) {
        Map<Class<?>, Object> map = annotationCache.get(msId);
        if (map == null) {
            map = new ConcurrentHashMap<>(128);
        }
        return map.put(anClass, value);
    }

    /**
     * 通过msId 获取 注解信息
     *
     * @param msId
     * @param anClass
     * @param <T>
     * @return
     * @throws ClassNotFoundException
     */
    public static <T extends Annotation> T getAnnotationById(String msId, Class<T> anClass) throws ClassNotFoundException {

        Object cacheObject = getCache(msId, anClass);
        if (EMPTY_Annotation.equals(cacheObject)) {
            return null;
        }
        T annotation = (T) cacheObject;
        if (annotation != null) {
            return annotation;
        }

        annotation = getAnnotationByIdNoCache(msId, anClass);

        putCache(msId, anClass, annotation == null ? EMPTY_Annotation : annotation);
        return annotation;
    }

    public static <T extends Annotation> T getAnnotationByIdNoCache(String msId, Class<T> anClass) throws ClassNotFoundException {

        T annotation = null;

        String className = StringUtils.substringBeforeLast(msId, ".");
        String methodName = StringUtils.substringAfterLast(msId, ".");

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
        return annotation;
    }
}
