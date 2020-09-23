package com.fanyiran.utils.recycleadapter;

/**
 * Created by fanqiang on 2019/4/16.
 */
public interface RvItemType<T extends RvItemData> {

    boolean openClick();

    boolean openLongClick();

    int getType();

    int getLayout();

    void fillContent(RvViewHolder rvViewHolder, int position, T data);

    boolean isCurrentType(T data, int position);

    int[] getOnClickViews();

    int[] getOnLongClickViews();
}
