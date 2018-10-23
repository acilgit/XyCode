package xyz.xmethod.xycode.interfaces;

import java.io.Serializable;

/**
 * Created by XY on 2016-09-02.
 */
public class Interfaces {

    @FunctionalInterface
    public interface OnStringData<T> extends Serializable {
        String getDataString(T data);
    }

    /**
     * 回调接口
     *
     * @param <T>
     */
    @FunctionalInterface
    public interface CB<T> extends Serializable {
        void go(T obj);
    }

    /**
     * 回调并反馈接口
     *
     * @param <T>
     * @param <Y>
     */
    @FunctionalInterface
    public interface FeedBack<T, Y> extends Serializable {
        Y go(T obj);
    }

    public interface OnCommitListener<T> extends Serializable {
        void onCommit(T var1);

        void onCancel(T var1);
    }
}
