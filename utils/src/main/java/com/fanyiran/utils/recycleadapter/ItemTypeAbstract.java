package com.fanyiran.utils.recycleadapter;

public abstract class ItemTypeAbstract implements ItemType {
    @Override
    public boolean openLongClick() {
        return false;
    }

    @Override
    public boolean isCurrentType(ItemData data, int position) {
        return data.getItemType() == getType();
    }

    @Override
    public int[] getOnLongClickViews() {
        return new int[0];
    }

    @Override
    public int getType() {
        return TypeIdGenerator.INSTANCE.getTypeId(Class.class.getSimpleName());
    }
}
