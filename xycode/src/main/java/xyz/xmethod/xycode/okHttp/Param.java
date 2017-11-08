package xyz.xmethod.xycode.okHttp;

import java.util.LinkedHashMap;

/**
 * Created by XY on 2016/7/11.
 */
public class Param extends LinkedHashMap<String, String> {

    public Param() {
        super();
    }

    public Param(String key, String value) {
        super();
        this.put(key, value);
    }

    public Param add(String key, String value) {
        this.put(key, value);
        return this;
    }

    public String getKey(String key) {
        return this.get(key) == null ? "" : this.get(key);
    }

    @Override
    public String toString() {
        return "[Param]";
    }
}