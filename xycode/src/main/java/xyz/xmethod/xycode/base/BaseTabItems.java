package xyz.xmethod.xycode.base;

import android.content.Context;
import android.support.design.widget.TabLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by XiuYe on 2016-08-09.
 *
 */
public abstract class BaseTabItems {

    protected Context context;
    protected TabLayout tabMain;
    protected int layoutId;
    protected int textViewId;
    protected int imageViewId;
    protected String[] titles;
    protected int[] iconsNormal;
    protected int[] iconsSelected;
    protected int selectedTextColor = 0;
    protected int normalTextColor = 0;

    public BaseTabItems(Context context, TabLayout tabMain, int layoutId, int textViewId, int imageViewId, String[] titles, int[] tabIconsNormalId, int[] tabIconsSelectedId, int selectedTextColor, int normalTextColor) {
        this.context = context;
        this.tabMain = tabMain;
        this.layoutId = layoutId;
        this.imageViewId = imageViewId;
        this.textViewId = textViewId;
        this.iconsNormal = tabIconsNormalId;
        this.iconsSelected = tabIconsSelectedId;
        this.titles = titles;
        this.selectedTextColor = selectedTextColor;
        this.normalTextColor = normalTextColor;
        setupTabIcons();
    }

    private void setupTabIcons() {
        int currentIndex = tabMain.getSelectedTabPosition();
        if (currentIndex == -1) currentIndex = 0;
        for (int i = 0; i < titles.length; i++) {
            View view = LayoutInflater.from(context).inflate(layoutId, null);
            TextView tvTab = (TextView) view.findViewById(textViewId);
            ImageView ivTab = (ImageView) view.findViewById(imageViewId);
            if (ivTab != null) {
                ivTab.setImageResource(i == currentIndex ? iconsSelected[i] : iconsNormal[i]);
            }
            if (tvTab != null) {
                tvTab.setText(titles[i]);
                tvTab.setTextColor(context.getResources().getColor(i == currentIndex ? selectedTextColor : normalTextColor));
            }
            TabLayout.Tab tab;
            if (tabMain.getTabCount() == i) {
                tab = tabMain.newTab();
                tabMain.addTab(tab);
            } else {
                tab = tabMain.getTabAt(i);
            }
            initTabItemView(layoutId, view, i);
            tab.setCustomView(view);
        }
    }

    public int setTabSelected(TabLayout.Tab tab) {
        int position = tab.getPosition();
        View view = tab.getCustomView();
        TextView tvTab = (TextView) view.findViewById(textViewId);
        ImageView ivTab = (ImageView) view.findViewById(imageViewId);
        ivTab.setImageResource(iconsSelected[position]);
        tvTab.setTextColor(context.getResources().getColor(selectedTextColor));
        return position;
    }

    public void setTabUnselected(TabLayout.Tab tab) {
        int position = tab.getPosition();
        View view = tab.getCustomView();
        TextView tvTab = (TextView) view.findViewById(textViewId);
        ImageView ivTab = (ImageView) view.findViewById(imageViewId);
        ivTab.setImageResource(iconsNormal[position]);
        tvTab.setTextColor(context.getResources().getColor(normalTextColor));
    }

    protected abstract void initTabItemView(int layoutId, View view, int pos);

    public View getTabViewItemById(int pos, int viewId) {
        if (tabMain.getTabCount() > pos) {
            return tabMain.getTabAt(pos).getCustomView().findViewById(viewId);
        }
        return null;
    }
}
