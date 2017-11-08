package xyz.xmethod.xycode.unit;

/**
 * use for Height and Width
 * Created by XY on 2016-07-26.
 */
public class WH {
    public int width;
    public int height;

    public WH(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public float getAspectRatio() {
        if (height == 0) {
            return 0;
        }
        return width * 1.0f / height;
    }

    public boolean isAvailable() {
        return getAspectRatio() > 0;
    }

}
