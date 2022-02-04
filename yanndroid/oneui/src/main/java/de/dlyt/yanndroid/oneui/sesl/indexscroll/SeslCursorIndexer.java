package de.dlyt.yanndroid.oneui.sesl.indexscroll;

import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;

public class SeslCursorIndexer extends SeslAbsIndexer {
    public static final String EXTRA_INDEX_COUNTS = "indexscroll_index_counts";
    public static final String EXTRA_INDEX_TITLES = "indexscroll_index_titles";
    private final boolean DEBUG = false;
    private final String TAG = "SeslCursorIndexer";
    protected int mColumnIndex;
    protected Cursor mCursor;
    protected int mSavedCursorPos;

    @Override // SeslAbsIndexer
    public /* bridge */ /* synthetic */ void onChanged() {
        super.onChanged();
    }

    @Override // SeslAbsIndexer
    public /* bridge */ /* synthetic */ void onInvalidated() {
        super.onInvalidated();
    }

    public SeslCursorIndexer(Cursor cursor, int i, CharSequence charSequence) {
        super(charSequence);
        this.mCursor = cursor;
        this.mColumnIndex = i;
    }

    public SeslCursorIndexer(Cursor cursor, int i, String[] strArr, int i2) {
        super(strArr, i2);
        this.mCursor = cursor;
        this.mColumnIndex = i;
    }

    /* access modifiers changed from: protected */
    @Override // SeslAbsIndexer
    public boolean isDataToBeIndexedAvailable() {
        return getItemCount() > 0 && !this.mCursor.isClosed();
    }

    /* access modifiers changed from: protected */
    @Override // SeslAbsIndexer
    public String getItemAt(int i) {
        if (this.mCursor.isClosed()) {
            Log.d("SeslCursorIndexer", "SeslCursorIndexer getItemAt : mCursor is closed.");
            return null;
        }
        this.mCursor.moveToPosition(i);
        try {
            return this.mCursor.getString(this.mColumnIndex);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /* access modifiers changed from: protected */
    @Override // SeslAbsIndexer
    public int getItemCount() {
        if (!this.mCursor.isClosed()) {
            return this.mCursor.getCount();
        }
        Log.d("SeslCursorIndexer", "SeslCursorIndexer getItemCount : mCursor is closed.");
        return 0;
    }

    /* access modifiers changed from: protected */
    @Override // SeslAbsIndexer
    public Bundle getBundle() {
        if (this.mCursor.isClosed()) {
            return null;
        }
        Log.d("SeslCursorIndexer", "Bundle was used by Indexer");
        return this.mCursor.getExtras();
    }

    /* access modifiers changed from: package-private */
    @Override // SeslAbsIndexer
    public void onBeginTransaction() {
        this.mSavedCursorPos = this.mCursor.getPosition();
    }

    /* access modifiers changed from: package-private */
    @Override // SeslAbsIndexer
    public void onEndTransaction() {
        this.mCursor.moveToPosition(this.mSavedCursorPos);
    }

    public void setProfileItemsCount(int i) {
        setProfileItem(i);
    }

    public void setFavoriteItemsCount(int i) {
        setFavoriteItem(i);
    }

    public void setGroupItemsCount(int i) {
        setGroupItem(i);
    }

    public void setMiscItemsCount(int i) {
        setDigitItem(i);
    }
}
