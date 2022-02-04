package de.dlyt.yanndroid.oneui.sesl.indexscroll;

import android.os.Bundle;

import java.util.List;

public class SeslArrayIndexer extends SeslAbsIndexer {
    private final boolean DEBUG = false;
    private final String TAG = "SeslArrayIndexer";
    protected List<String> mData;

    /* access modifiers changed from: protected */
    @Override // SeslAbsIndexer
    public Bundle getBundle() {
        return null;
    }

    @Override // SeslAbsIndexer
    public /* bridge */ /* synthetic */ void onChanged() {
        super.onChanged();
    }

    @Override // SeslAbsIndexer
    public /* bridge */ /* synthetic */ void onInvalidated() {
        super.onInvalidated();
    }

    public SeslArrayIndexer(List<String> list, CharSequence charSequence) {
        super(charSequence);
        this.mData = list;
    }

    /* access modifiers changed from: protected */
    @Override // SeslAbsIndexer
    public int getItemCount() {
        return this.mData.size();
    }

    /* access modifiers changed from: protected */
    @Override // SeslAbsIndexer
    public String getItemAt(int i) {
        return this.mData.get(i);
    }

    /* access modifiers changed from: protected */
    @Override // SeslAbsIndexer
    public boolean isDataToBeIndexedAvailable() {
        return getItemCount() > 0;
    }
}
