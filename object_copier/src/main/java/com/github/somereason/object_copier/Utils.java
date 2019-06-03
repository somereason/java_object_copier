package com.github.somereason.object_copier;

import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;

public class Utils {
    /**
     * 在list指定位置插入元素,如果指定位置超过list长度,则扩展list的长度
     *
     * @param list
     * @param index
     * @param value
     * @param <T>
     */
    public static <T> void listSafeSet(List<T> list, int index, T value) {
        if (list.size() <= index) {
            for (int i = list.size(); i <= index; i++) {
                list.add(i, null);
            }
        }
        list.set(index, value);
    }

    public static <T, K> void mapSafePut(Map<T, K> map, T key, K value) {
        if (map.containsKey(key))
            map.replace(key, value);
        else
            map.put(key, value);
    }
}
