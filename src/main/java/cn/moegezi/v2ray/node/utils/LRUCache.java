package cn.moegezi.v2ray.node.utils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class LRUCache {

    private final ConcurrentHashMap<Integer, Long> cache = new ConcurrentHashMap<>();
    private final List<Integer> removeList = new ArrayList<>();
    private long timeout;

    public LRUCache(long timeout) {
        this.timeout = timeout;
    }

    public void sweep() {
        long start = System.currentTimeMillis();
        if (!cache.isEmpty()) {
            for (Map.Entry<Integer, Long> e : cache.entrySet()) {
                if (start - e.getValue() > timeout) {
                    removeList.add(e.getKey());
                }
            }
            for (Integer key : removeList) {
                cache.remove(key);
            }
            removeList.clear();
        }
    }

    public void put(Integer key) {
        cache.put(key, System.currentTimeMillis() / 1000);
    }

    public void remove(Integer key) {
        cache.remove(key);
    }

    public long size() {
        return cache.size();
    }
}
