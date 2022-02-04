package de.dlyt.yanndroid.oneui.sesl.indexscroll;

import android.database.DataSetObservable;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseIntArray;

import java.text.Collator;
import java.util.HashMap;

/* access modifiers changed from: package-private */
public abstract class SeslAbsIndexer extends DataSetObserver {
    private static final char DIGIT_CHAR = '#';
    private static final char FAVORITE_CHAR = 9733;
    private static final String GROUP_CHAR = "👥︎";
    private static final char GROUP_CHECKER = 55357;
    static final String INDEXSCROLL_INDEX_COUNTS = "indexscroll_index_counts";
    static final String INDEXSCROLL_INDEX_TITLES = "indexscroll_index_titles";
    private static final char SYMBOL_BASE_CHAR = '!';
    private static final char SYMBOL_CHAR = '&';
    private final boolean DEBUG;
    private final String TAG;
    private SparseIntArray mAlphaMap;
    private CharSequence mAlphabet;
    private String[] mAlphabetArray;
    private int mAlphabetLength;
    private Bundle mBundle;
    private int[] mCachingValue;
    protected Collator mCollator;
    private final DataSetObservable mDataSetObservable;
    private int mDigitItemCount;
    private int mFavoriteItemCount;
    private int mGroupItemCount;
    private boolean mIsInitialized;
    private String[] mLangAlphabetArray;
    private HashMap<Integer, Integer> mLangIndexMap;
    private int mProfileItemCount;
    private boolean mRegisteredDataSetObservable;
    private boolean mUseDigitIndex;
    private boolean mUseFavoriteIndex;
    private boolean mUseGroupIndex;

    /* access modifiers changed from: protected */
    public abstract Bundle getBundle();

    /* access modifiers changed from: protected */
    public abstract String getItemAt(int i);

    /* access modifiers changed from: protected */
    public abstract int getItemCount();

    /* access modifiers changed from: protected */
    public abstract boolean isDataToBeIndexedAvailable();

    /* access modifiers changed from: package-private */
    public void onBeginTransaction() {
    }

    /* access modifiers changed from: package-private */
    public void onEndTransaction() {
    }

    SeslAbsIndexer(CharSequence charSequence) {
        this.TAG = "SeslAbsIndexer";
        this.DEBUG = false;
        this.mDataSetObservable = new DataSetObservable();
        this.mRegisteredDataSetObservable = false;
        this.mProfileItemCount = 0;
        this.mFavoriteItemCount = 0;
        this.mGroupItemCount = 0;
        this.mDigitItemCount = 0;
        this.mUseFavoriteIndex = false;
        this.mUseGroupIndex = false;
        this.mUseDigitIndex = false;
        this.mIsInitialized = false;
        this.mLangIndexMap = new HashMap<>();
        this.mUseFavoriteIndex = false;
        this.mProfileItemCount = 0;
        this.mFavoriteItemCount = 0;
        initIndexer(charSequence);
    }

    SeslAbsIndexer(String[] strArr, int i) {
        this.TAG = "SeslAbsIndexer";
        this.DEBUG = false;
        this.mDataSetObservable = new DataSetObservable();
        this.mRegisteredDataSetObservable = false;
        this.mProfileItemCount = 0;
        this.mFavoriteItemCount = 0;
        this.mGroupItemCount = 0;
        this.mDigitItemCount = 0;
        this.mUseFavoriteIndex = false;
        this.mUseGroupIndex = false;
        this.mUseDigitIndex = false;
        this.mIsInitialized = false;
        this.mLangIndexMap = new HashMap<>();
        this.mUseFavoriteIndex = false;
        this.mProfileItemCount = 0;
        this.mFavoriteItemCount = 0;
        this.mLangAlphabetArray = strArr;
        setIndexerArray();
    }

    /* access modifiers changed from: package-private */
    public String[] getLangAlphabetArray() {
        return this.mLangAlphabetArray;
    }

    /* access modifiers changed from: package-private */
    public int getCachingValue(int i) {
        if (i < 0 || i >= this.mAlphabetLength) {
            return -1;
        }
        return this.mCachingValue[i];
    }

    /* access modifiers changed from: package-private */
    public int getIndexByPosition(int i) {
        int i2 = -1;
        if (this.mCachingValue == null) {
            return -1;
        }
        int i3 = 0;
        while (true) {
            i2 = i3;
            if (i2 >= this.mAlphabetLength) {
                return i2;
            }
            int[] iArr = this.mCachingValue;
            if (iArr[i2] == i) {
                return i2;
            }
            if (iArr[i2] > i) {
                return i2 - 1;
            }
            i3 = i2 + 1;
        }
    }

    private void setIndexerArray() {
        StringBuilder sb = new StringBuilder();
        if (this.mUseFavoriteIndex) {
            sb.append(FAVORITE_CHAR);
        }
        if (this.mUseGroupIndex) {
            sb.append(GROUP_CHECKER);
        }
        int i = 0;
        while (i < this.mLangAlphabetArray.length) {
            for (int i2 = 0; i2 < this.mLangAlphabetArray[i].length(); i2++) {
                this.mLangIndexMap.put(Integer.valueOf(sb.length()), Integer.valueOf(i));
                sb.append(this.mLangAlphabetArray[i].charAt(i2));
            }
            i++;
        }
        if (this.mUseDigitIndex) {
            this.mLangIndexMap.put(Integer.valueOf(sb.length()), Integer.valueOf(i - 1));
            sb.append(DIGIT_CHAR);
        }
        if (sb.length() != 0) {
            initIndexer(sb.toString());
        } else {
            Log.w("SeslAbsIndexer", "The array received from App is empty. Indexer must be initialized through additional API.");
        }
    }

    /* access modifiers changed from: package-private */
    public void setProfileItem(int i) {
        if (i >= 0) {
            this.mProfileItemCount = i;
        }
    }

    /* access modifiers changed from: package-private */
    public void setFavoriteItem(int i) {
        if (i > 0) {
            this.mFavoriteItemCount = i;
            this.mUseFavoriteIndex = true;
            setIndexerArray();
        }
    }

    /* access modifiers changed from: package-private */
    public void setGroupItem(int i) {
        if (i > 0) {
            this.mGroupItemCount = i;
            this.mUseGroupIndex = true;
            setIndexerArray();
        }
    }

    /* access modifiers changed from: package-private */
    public void setDigitItem(int i) {
        if (i > 0) {
            this.mDigitItemCount = i;
            this.mUseDigitIndex = true;
            setIndexerArray();
        }
    }

    private void initIndexer(CharSequence charSequence) {
        if (charSequence == null || charSequence.length() == 0) {
            throw new IllegalArgumentException("Invalid indexString :" + ((Object) charSequence));
        }
        this.mAlphabet = charSequence;
        int length = charSequence.length();
        this.mAlphabetLength = length;
        this.mCachingValue = new int[length];
        this.mAlphabetArray = new String[length];
        for (int i = 0; i < this.mAlphabetLength; i++) {
            if (!this.mUseGroupIndex || this.mAlphabet.charAt(i) != 55357) {
                this.mAlphabetArray[i] = Character.toString(this.mAlphabet.charAt(i));
            } else {
                this.mAlphabetArray[i] = GROUP_CHAR;
            }
        }
        this.mAlphaMap = new SparseIntArray(this.mAlphabetLength);
        Collator instance = Collator.getInstance();
        this.mCollator = instance;
        instance.setStrength(0);
        this.mIsInitialized = true;
    }

    /* access modifiers changed from: package-private */
    public boolean isInitialized() {
        return this.mIsInitialized;
    }

    /* access modifiers changed from: package-private */
    public String[] getAlphabetArray() {
        return this.mAlphabetArray;
    }

    private int compare(String str, String str2) {
        return this.mCollator.compare(str, str2);
    }

    /* access modifiers changed from: package-private */
    public void cacheIndexInfo() {
        if (isDataToBeIndexedAvailable() && getItemCount() != 0) {
            Bundle bundle = getBundle();
            this.mBundle = bundle;
            if (bundle == null || !bundle.containsKey("indexscroll_index_titles") || !this.mBundle.containsKey("indexscroll_index_counts")) {
                onBeginTransaction();
                for (int i = 0; i < this.mAlphabetLength; i++) {
                    this.mCachingValue[i] = getPositionForString("" + this.mAlphabet.charAt(i));
                }
                onEndTransaction();
                return;
            }
            getBundleInfo();
        }
    }

    public final int getPositionForString(String var1) {
        SparseIntArray var2 = this.mAlphaMap;
        int var3 = this.getItemCount();
        if (var3 != 0 && this.mAlphabet != null) {
            int var4 = var3;
            if (var1 != null) {
                if (var1.length() == 0) {
                    var4 = var3;
                } else {
                    char var5;
                    int var6;
                    int var7;
                    label112:
                    {
                        var5 = var1.charAt(0);
                        var4 = var2.get(var5, -2147483648);
                        if (-2147483648 != var4) {
                            var6 = Math.abs(var4);
                        } else {
                            CharSequence var8;
                            label109:
                            {
                                var7 = this.mAlphabet.toString().indexOf(var5);
                                if (var7 > 0) {
                                    var8 = this.mAlphabet;
                                    var4 = var7 - 1;
                                    if (var5 > var8.charAt(var4)) {
                                        var4 = var2.get(this.mAlphabet.charAt(var4), -2147483648);
                                        if (var4 != -2147483648) {
                                            var4 = Math.abs(var4);
                                            break label109;
                                        }
                                    }
                                }

                                var4 = 0;
                            }

                            var6 = var4;
                            if (var7 < this.mAlphabet.length() - 1) {
                                var8 = this.mAlphabet;
                                ++var7;
                                var6 = var4;
                                if (var5 < var8.charAt(var7)) {
                                    var7 = var2.get(this.mAlphabet.charAt(var7), -2147483648);
                                    var6 = var4;
                                    if (var7 != -2147483648) {
                                        var7 = Math.abs(var7);
                                        var6 = var4;
                                        break label112;
                                    }
                                }
                            }
                        }

                        var7 = var3;
                    }

                    char var9 = var1.charAt(0);
                    String var14;
                    if (var9 == '&') {
                        var14 = "!";
                    } else {
                        var14 = var1;
                    }

                    int var10;
                    if (var9 == 9733) {
                        var10 = this.mProfileItemCount;
                        var4 = var6;
                        if (var6 < var10) {
                            var4 = var10;
                        }
                    } else {
                        int var11;
                        if (var9 == '\ud83d') {
                            var10 = this.mProfileItemCount;
                            var11 = this.mFavoriteItemCount;
                            var4 = var6;
                            if (var6 < var10 + var11) {
                                var4 = var10 + var11;
                            }
                        } else {
                            var10 = this.mProfileItemCount;
                            var11 = this.mFavoriteItemCount;
                            int var12 = this.mGroupItemCount;
                            var4 = var6;
                            if (var6 < var10 + var11 + var12) {
                                var4 = var10 + var11 + var12;
                            }
                        }
                    }

                    var10 = var7 - this.mDigitItemCount;
                    var6 = var4;
                    if (var9 == '#') {
                        var6 = var10;
                    }

                    var4 = (var10 + var6) / 2;
                    var7 = var6;
                    var6 = var10;

                    label98:
                    {
                        while (var4 >= var7 && var4 < var6) {
                            String var13 = this.getItemAt(var4);
                            if (var13 != null && !var13.equals("")) {
                                var10 = this.compare(var13, var14);
                                if (var9 == 9733 || var9 == '&' || var9 == '#') {
                                    var10 = 1;
                                }

                                label85:
                                {
                                    if (var10 != 0) {
                                        if (var10 < 0) {
                                            var7 = var4 + 1;
                                            if (var7 >= var3) {
                                                break label98;
                                            }
                                            break label85;
                                        }
                                    } else if (var7 == var4) {
                                        break;
                                    }

                                    var6 = var4;
                                }

                                var4 = (var7 + var6) / 2;
                            } else {
                                if (var4 <= var7) {
                                    break;
                                }

                                --var4;
                            }
                        }

                        var3 = var4;
                    }

                    var4 = var3;
                    if (var1.length() == 1) {
                        var2.put(var5, var3);
                        var4 = var3;
                    }
                }
            }

            return var4;
        } else {
            return 0;
        }
    }

    public final void getBundleInfo() {
        String[] var1 = this.mBundle.getStringArray("indexscroll_index_titles");
        int[] var2 = this.mBundle.getIntArray("indexscroll_index_counts");
        int var3 = this.mProfileItemCount;
        int var4 = 0;

        int var9;
        for(int var5 = var4; var4 < this.mAlphabetLength; var5 = var9) {
            char var6;
            int var7;
            label23: {
                var6 = this.mAlphabet.charAt(var4);
                this.mCachingValue[var4] = var3;
                if (var6 == 9733) {
                    var7 = this.mFavoriteItemCount;
                } else {
                    var7 = var3;
                    if (var6 != '\ud83d') {
                        break label23;
                    }

                    var7 = this.mGroupItemCount;
                }

                var7 += var3;
            }

            int var8 = var5;

            while(true) {
                var3 = var7;
                var9 = var5;
                if (var8 >= var1.length) {
                    break;
                }

                if (var6 == var1[var8].charAt(0)) {
                    var3 = var7 + var2[var8];
                    var9 = var8;
                    break;
                }

                ++var8;
            }

            ++var4;
        }

    }

    public void onChanged() {
        super.onChanged();
        this.mAlphaMap.clear();
        this.mDataSetObservable.notifyChanged();
    }

    public void onInvalidated() {
        super.onInvalidated();
        this.mAlphaMap.clear();
        this.mDataSetObservable.notifyInvalidated();
    }

    /* access modifiers changed from: package-private */
    public void registerDataSetObserver(DataSetObserver dataSetObserver) {
        if (!this.mRegisteredDataSetObservable) {
            this.mRegisteredDataSetObservable = true;
            this.mDataSetObservable.registerObserver(dataSetObserver);
            return;
        }
        Log.e("SeslAbsIndexer", "Observer " + dataSetObserver + " is already registered.");
    }

    /* access modifiers changed from: package-private */
    public void unregisterDataSetObserver(DataSetObserver dataSetObserver) {
        if (this.mRegisteredDataSetObservable) {
            this.mRegisteredDataSetObservable = false;
            this.mDataSetObservable.unregisterObserver(dataSetObserver);
            return;
        }
        Log.e("SeslAbsIndexer", "Observer " + dataSetObserver + " was not registered.");
    }
}
