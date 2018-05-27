package com.example.dataforlife.loginservice;

import com.example.dataforlife.R;

/**
 * Author Yousria
 */
public enum ModelObject {

    BLUETOOTH(R.string.agree, R.layout.bluetooth_view_pager),
    BATTERIE(R.string.agree, R.layout.batterie_view_pager),
    SELECTION(R.string.agree, R.layout.selection_view_pager),
    PAIRED(R.string.agree, R.layout.paired_view_pager);

    private int textId;
    private int layoutId;

    ModelObject(int titleResId, int layoutResId) {
        textId = titleResId;
        layoutId = layoutResId;
    }

    public int getTextId() {
        return textId;
    }

    public int getLayoutId() {
        return layoutId;
    }

}
