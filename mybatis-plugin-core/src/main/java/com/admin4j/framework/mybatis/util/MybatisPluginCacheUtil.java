package com.admin4j.framework.mybatis.util;

import java.util.HashMap;
import java.util.Map;

/**
 * @author andanyang
 * @since 2024/4/18 13:58
 */
public class MybatisPluginCacheUtil {

    protected static ThreadLocal<Map<String, Object>> CACHE = new ThreadLocal<>();

    public static void init() {
        Map<String, Object> map = CACHE.get();
        if (map == null) {
            map = new HashMap<>();
            CACHE.set(map);
        }
        map.clear();
    }

    public static void clear() {
        CACHE.remove();
    }


    public static void setDisabled(String plugin) {

        Map<String, Object> map = CACHE.get();
        if (map == null) {
            map = new HashMap<>();
            CACHE.set(map);
        }
        map.put("_enable:" + plugin, false);
    }

    public static Boolean getEnabled(String plugin) {
        Map<String, Object> map = CACHE.get();
        Object object = map.get("_enabled:" + plugin);
        if (object == null) {
            return null;
        }
        return (Boolean) object;
    }
}
