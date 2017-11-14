package xyz.xmethod.xycode.storages;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import xyz.xmethod.xycode.Xy;

/**
 * Created by XiuYe on 2016-07-27.
 * 持久化类
 */
public class ShareStorage {

    /**
     * SharedPreferences
     */
    private SharedPreferences storage;

    /**
     * SP变更监听
     */
    private SharedPreferences.OnSharedPreferenceChangeListener onSharedPreferenceChangeListener;

    public ShareStorage(@NonNull String preferenceName) {
        storage = Xy.getContext().getSharedPreferences(preferenceName, Context.MODE_PRIVATE);
    }

    public void setStorageOnChangeListener(SharedPreferences.OnSharedPreferenceChangeListener onSharedPreferenceChangeListener) {
        if (storage != null) {
            this.onSharedPreferenceChangeListener = onSharedPreferenceChangeListener;
            storage.registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);
        }
    }

    public void resetStorageOnChangeListene() {
        if (storage != null && onSharedPreferenceChangeListener != null) {
            storage.unregisterOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);
        }
    }

    /**
     * clear all content
     */
    public void clear() {
        storage.edit().clear().apply();
    }

    public void put(String key, Object value) {
        storage.edit().putString(key, value.toString()).apply();
    }

    public void put(String key, int value) {
        storage.edit().putInt(key, value).apply();
    }

    public void put(String key, float value) {
        storage.edit().putFloat(key, value).apply();
    }

    public void put(String key, boolean value) {
        storage.edit().putBoolean(key, value).apply();
    }

    public void put(String key, String value) {
        storage.edit().putString(key, value).apply();
    }

    public void put(String key, long value) {
        storage.edit().putLong(key, value).apply();
    }

    public void put(String key, double value) {
        Double newValue = value;
        storage.edit().putString(key, newValue.toString()).apply();
    }

    public String getString(String key) {
        return storage.getString(key, "");
    }

    public String getString(String key, String defValue) {
        return storage.getString(key, defValue);
    }

    public boolean getBoolean(String key, boolean defValue) {
        return storage.getBoolean(key, defValue);
    }

    public int getInt(String key, int defValue) {
        return storage.getInt(key, defValue);
    }

    public float getFloat(String key, float defValue) {
        return storage.getFloat(key, defValue);
    }

    public double getDouble(String key, double defValue) {
        String value = storage.getString(key, null);
        return value == null ? defValue : Double.parseDouble(value);
    }

    public long getLong(String key, long defValue) {
        return storage.getLong(key, defValue);
    }

    /**
     * 请使用Editor时用commit()方法
     * 可以实现同步保存，但会占用线程时间，请不要保存大量文字
     * @return
     */
    public SharedPreferences.Editor getEditor() {
        return storage.edit();
    }
}
