package xyz.xmethod.xycode.views.nicespinner;

import android.content.Context;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import xyz.xmethod.xycode.R;
import xyz.xmethod.xycode.unit.StringData;

import java.util.List;

/**
 * @author angelo.marchesin
 */

@SuppressWarnings("unused")
public abstract class NiceSpinnerBaseAdapter extends BaseAdapter {
    protected Context mContext;
    protected int mSelectedIndex;
    protected int mTextColor;
    protected int mBackgroundSelector;
    public abstract List getItems();
    public abstract static class SpinnerAble {
        public abstract String getSpinnerText();
    }

    public NiceSpinnerBaseAdapter(Context context, int textColor, int backgroundSelector) {
        mContext = context;
        mTextColor = textColor;
        mBackgroundSelector = backgroundSelector;
    }

    @Override
    @SuppressWarnings("unchecked")
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView textView;

        if (convertView == null) {
            convertView = View.inflate(mContext, R.layout.spinner_list_item, null);
            textView = (TextView) convertView.findViewById(R.id.tv_tinted_spinner);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                textView.setBackground(ContextCompat.getDrawable(mContext, mBackgroundSelector));
            }

            convertView.setTag(new ViewHolder(textView));
        } else {
            textView = ((ViewHolder) convertView.getTag()).textView;
        }

        textView.setText(getItem(position).getString());
        textView.setTextColor(mTextColor);
        return convertView;
    }

    public int getSelectedIndex() {
        return mSelectedIndex;
    }

    public void notifyItemSelected(int index) {
        mSelectedIndex = index;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public abstract StringData getItem(int position);

    @Override
    public abstract int getCount();

    public abstract StringData getItemInDataset(int position);

    protected static class ViewHolder {

        public TextView textView;

        public ViewHolder(TextView textView) {
            this.textView = textView;
        }
    }

    public abstract StringData getCurrentItem();

}
