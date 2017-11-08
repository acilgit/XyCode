package xyz.xmethod.xycode.interfaces;

import android.widget.ImageView;

/**
 * Created by xiuye on 2017/11/8.
 * 在Xy中Init设置SetImageUrl的方法
 * 可以此方法中实现FrescoLoader的导入
 */

public interface ISetImageUrlMethod {
    void setImageUrl(ImageView imageView, Object urlObject, Object resizeOptions);
}
