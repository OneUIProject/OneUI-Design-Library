package com.samsung.android.sdk.pen.settingui.colorpicker;

import android.content.Context;
import android.graphics.Rect;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.widget.RelativeLayout;

import com.samsung.android.sdk.pen.settingui.util.SpenSettingUtil;

import java.util.ArrayList;
import java.util.List;

import de.dlyt.yanndroid.samsung.R;

public class SpenColorSwatchView extends RelativeLayout implements SpenColorViewInterface, SpenPickerColorEventListener {
    private static final int DARK_COLOR_NAME_IDX = 2;
    private static final int LIGHT_COLOR_NAME_IDX = 1;
    private static final int STANDARD_DARK_ROW = 7;
    private static final int STANDARD_LIGHT_ROW = 5;
    private static final String TAG = "SpenColorSwatchView";
    private int mColumNum;
    private int mCurrentPosition;
    private final float[][] mHSV = {new float[]{0.0f, 0.0f, 0.99f}, new float[]{0.0f, 0.11f, 1.0f}, new float[]{25.0f, 0.13f, 1.0f}, new float[]{43.0f, 0.14f, 1.0f}, new float[]{57.0f, 0.14f, 1.0f}, new float[]{98.0f, 0.09f, 1.0f}, new float[]{143.0f, 0.1f, 0.99f}, new float[]{172.0f, 0.11f, 1.0f}, new float[]{212.0f, 0.14f, 1.0f}, new float[]{228.0f, 0.14f, 1.0f}, new float[]{272.0f, 0.14f, 1.0f}, new float[]{302.0f, 0.11f, 1.0f}, new float[]{337.0f, 0.11f, 1.0f}, new float[]{0.0f, 0.0f, 0.9f}, new float[]{0.0f, 0.21f, 1.0f}, new float[]{26.0f, 0.23f, 1.0f}, new float[]{44.0f, 0.25f, 1.0f}, new float[]{57.0f, 0.25f, 1.0f}, new float[]{98.0f, 0.15f, 1.0f}, new float[]{144.0f, 0.18f, 0.98f}, new float[]{172.0f, 0.2f, 1.0f}, new float[]{211.0f, 0.25f, 1.0f}, new float[]{228.0f, 0.25f, 1.0f}, new float[]{272.0f, 0.21f, 1.0f}, new float[]{302.0f, 0.19f, 1.0f}, new float[]{337.0f, 0.2f, 1.0f}, new float[]{0.0f, 0.0f, 0.8f}, new float[]{1.0f, 0.37f, 1.0f}, new float[]{26.0f, 0.36f, 1.0f}, new float[]{45.0f, 0.4f, 1.0f}, new float[]{57.0f, 0.4f, 1.0f}, new float[]{98.0f, 0.24f, 1.0f}, new float[]{144.0f, 0.29f, 0.98f}, new float[]{172.0f, 0.32f, 1.0f}, new float[]{211.0f, 0.4f, 1.0f}, new float[]{227.0f, 0.4f, 1.0f}, new float[]{272.0f, 0.35f, 1.0f}, new float[]{302.0f, 0.3f, 1.0f}, new float[]{337.0f, 0.32f, 1.0f}, new float[]{0.0f, 0.0f, 0.7f}, new float[]{2.0f, 0.51f, 1.0f}, new float[]{26.0f, 0.49f, 1.0f}, new float[]{45.0f, 0.55f, 1.0f}, new float[]{57.0f, 0.55f, 1.0f}, new float[]{98.0f, 0.33f, 1.0f}, new float[]{144.0f, 0.4f, 0.97f}, new float[]{172.0f, 0.44f, 1.0f}, new float[]{211.0f, 0.55f, 1.0f}, new float[]{227.0f, 0.55f, 1.0f}, new float[]{272.0f, 0.48f, 1.0f}, new float[]{302.0f, 0.41f, 1.0f}, new float[]{337.0f, 0.44f, 1.0f}, new float[]{0.0f, 0.0f, 0.6f}, new float[]{0.0f, 0.65f, 1.0f}, new float[]{26.0f, 0.63f, 1.0f}, new float[]{45.0f, 0.66f, 1.0f}, new float[]{57.0f, 0.7f, 1.0f}, new float[]{98.0f, 0.4f, 1.0f}, new float[]{144.0f, 0.49f, 0.96f}, new float[]{172.0f, 0.53f, 1.0f}, new float[]{211.0f, 0.7f, 1.0f}, new float[]{227.0f, 0.7f, 1.0f}, new float[]{270.0f, 0.58f, 1.0f}, new float[]{302.0f, 0.53f, 1.0f}, new float[]{337.0f, 0.56f, 1.0f}, new float[]{0.0f, 0.0f, 0.5f}, new float[]{0.0f, 0.79f, 1.0f}, new float[]{26.0f, 0.76f, 1.0f}, new float[]{45.0f, 0.81f, 1.0f}, new float[]{58.0f, 0.9f, 0.99f}, new float[]{98.0f, 0.6f, 1.0f}, new float[]{144.0f, 0.68f, 0.96f}, new float[]{172.0f, 0.8f, 1.0f}, new float[]{211.0f, 0.8f, 1.0f}, new float[]{227.0f, 0.8f, 1.0f}, new float[]{270.0f, 0.69f, 1.0f}, new float[]{302.0f, 0.6f, 1.0f}, new float[]{337.0f, 0.64f, 1.0f}, new float[]{0.0f, 0.0f, 0.4f}, new float[]{0.0f, 0.93f, 1.0f}, new float[]{26.0f, 0.9f, 1.0f}, new float[]{45.0f, 1.0f, 0.97f}, new float[]{57.0f, 1.0f, 0.97f}, new float[]{97.0f, 0.6f, 0.97f}, new float[]{143.0f, 0.7f, 0.93f}, new float[]{172.0f, 0.8f, 0.97f}, new float[]{210.0f, 0.94f, 1.0f}, new float[]{227.0f, 0.92f, 1.0f}, new float[]{270.0f, 0.8f, 1.0f}, new float[]{302.0f, 0.75f, 1.0f}, new float[]{337.0f, 0.73f, 1.0f}, new float[]{0.0f, 0.0f, 0.3f}, new float[]{0.0f, 0.93f, 0.91f}, new float[]{26.0f, 0.9f, 0.92f}, new float[]{45.0f, 1.0f, 0.92f}, new float[]{57.0f, 1.0f, 0.92f}, new float[]{98.0f, 0.6f, 0.92f}, new float[]{144.0f, 0.75f, 0.87f}, new float[]{172.0f, 0.8f, 0.92f}, new float[]{211.0f, 1.0f, 0.92f}, new float[]{227.0f, 0.97f, 0.92f}, new float[]{270.0f, 0.91f, 1.0f}, new float[]{302.0f, 0.75f, 0.92f}, new float[]{337.0f, 0.75f, 0.93f}, new float[]{0.0f, 0.0f, 0.25f}, new float[]{0.0f, 0.92f, 0.83f}, new float[]{26.0f, 0.9f, 0.85f}, new float[]{45.0f, 1.0f, 0.85f}, new float[]{57.0f, 1.0f, 0.85f}, new float[]{98.0f, 0.6f, 0.85f}, new float[]{144.0f, 0.75f, 0.81f}, new float[]{172.0f, 0.8f, 0.85f}, new float[]{211.0f, 1.0f, 0.85f}, new float[]{227.0f, 1.0f, 0.85f}, new float[]{270.0f, 1.0f, 0.92f}, new float[]{302.0f, 0.75f, 0.85f}, new float[]{337.0f, 0.8f, 0.85f}, new float[]{0.0f, 0.0f, 0.2f}, new float[]{0.0f, 0.93f, 0.72f}, new float[]{26.0f, 0.9f, 0.72f}, new float[]{45.0f, 1.0f, 0.72f}, new float[]{57.0f, 1.0f, 0.72f}, new float[]{98.0f, 0.6f, 0.72f}, new float[]{143.0f, 0.75f, 0.69f}, new float[]{172.0f, 0.81f, 0.72f}, new float[]{211.0f, 1.0f, 0.72f}, new float[]{227.0f, 1.0f, 0.72f}, new float[]{270.0f, 1.0f, 0.8f}, new float[]{302.0f, 0.75f, 0.72f}, new float[]{337.0f, 0.8f, 0.72f}, new float[]{0.0f, 0.0f, 0.145f}, new float[]{0.0f, 0.93f, 0.6f}, new float[]{26.0f, 1.0f, 0.6f}, new float[]{45.0f, 1.0f, 0.6f}, new float[]{57.0f, 1.0f, 0.6f}, new float[]{98.0f, 0.6f, 0.6f}, new float[]{144.0f, 0.75f, 0.57f}, new float[]{171.0f, 0.8f, 0.58f}, new float[]{211.0f, 1.0f, 0.6f}, new float[]{227.0f, 1.0f, 0.6f}, new float[]{270.0f, 1.0f, 0.64f}, new float[]{302.0f, 0.75f, 0.65f}, new float[]{337.0f, 0.8f, 0.62f}, new float[]{0.0f, 0.0f, 0.1f}, new float[]{0.0f, 0.93f, 0.43f}, new float[]{26.0f, 1.0f, 0.43f}, new float[]{39.0f, 1.0f, 0.43f}, new float[]{57.0f, 1.0f, 0.43f}, new float[]{98.0f, 0.6f, 0.42f}, new float[]{144.0f, 0.75f, 0.41f}, new float[]{172.0f, 0.8f, 0.39f}, new float[]{211.0f, 1.0f, 0.43f}, new float[]{227.0f, 1.0f, 0.43f}, new float[]{270.0f, 1.0f, 0.5f}, new float[]{302.0f, 0.75f, 0.43f}, new float[]{338.0f, 0.8f, 0.49f}, new float[]{0.0f, 0.0f, 0.0f}, new float[]{0.0f, 0.94f, 0.25f}, new float[]{26.0f, 1.0f, 0.32f}, new float[]{40.0f, 1.0f, 0.32f}, new float[]{57.0f, 1.0f, 0.32f}, new float[]{99.0f, 0.61f, 0.29f}, new float[]{144.0f, 0.75f, 0.28f}, new float[]{172.0f, 0.81f, 0.29f}, new float[]{211.0f, 1.0f, 0.32f}, new float[]{228.0f, 1.0f, 0.32f}, new float[]{270.0f, 1.0f, 0.38f}, new float[]{302.0f, 0.76f, 0.29f}, new float[]{337.0f, 0.81f, 0.37f}};
    private final int mItemLayoutID;
    private int mLastCenterX = 0;
    private int mLastPosIndex = -1;
    private SpenPickerColor mPickerColor;
    private int mReqPosIndex = -1;
    private int mSelectElevation;
    private SpenColorSwatchSelectorView mSelectedView;
    private SpenColorSwatchAdapter mSwatchAdapter;
    private List<SpenColorSwatchItem> mSwatchItemList;
    private int mSwatchStartMargin;
    private int mSwatchTopMargin;
    private GridView mSwatchView;
    private ViewTreeObserver.OnGlobalLayoutListener mSwatchViewObserver;
    private final AdapterView.OnItemClickListener mSwatchItemClickListener = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long j) {
            if (SpenColorSwatchView.this.mSwatchAdapter != null) {
                SpenColorSwatchView.this.releaseDetected();
                SpenColorSwatchView.this.updatePosition(i);
            }
        }
    };

    public SpenColorSwatchView(Context context, int i, int i2, int i3, int i4, int i5) {
        super(context);
        this.mItemLayoutID = i;
        construct(context, i2, i3, i4, i5);
    }

    @Override
    public void setPickerColor(SpenPickerColor spenPickerColor) {
        this.mPickerColor = spenPickerColor;
        float[] fArr = {0.0f, 0.0f, 0.0f};
        this.mPickerColor.getColor(fArr);
        setColor(fArr[0], fArr[1], fArr[2]);
        this.mPickerColor.addEventListener(this);
    }

    @Override
    public void release() {
        releaseDetected();
        SpenPickerColor spenPickerColor = this.mPickerColor;
        if (spenPickerColor != null) {
            spenPickerColor.removeEventListener(this);
            this.mPickerColor = null;
        }
        this.mSwatchView = null;
        this.mSwatchAdapter = null;
        this.mSwatchItemList = null;
    }

    @Override
    public void update(String str, int i, float f, float f2, float f3) {
        if (!str.equals(TAG)) {
            setColor(f, f2, f3);
        }
    }

    private void setColor(float f, float f2, float f3) {
        int findMatchedSwatch = findMatchedSwatch(f, f2, f3);
        SpenColorSwatchAdapter spenColorSwatchAdapter = this.mSwatchAdapter;
        if (spenColorSwatchAdapter != null) {
            spenColorSwatchAdapter.setSelectedPosition(findMatchedSwatch);
            if (findMatchedSwatch == -1 || getChildRect(findMatchedSwatch) != null) {
                updateSelector(findMatchedSwatch);
            } else {
                needUpdate(findMatchedSwatch);
            }
        }
    }

    private void construct(Context context, int i, int i2, int i3, int i4) {
        this.mSwatchItemList = null;
        this.mCurrentPosition = -1;
        this.mColumNum = context.getResources().getInteger(R.integer.setting_color_picker_column_count);
        this.mSelectElevation = context.getResources().getDimensionPixelOffset(R.dimen.setting_color_picker_selector_elevation);
        this.mSwatchStartMargin = i;
        this.mSwatchTopMargin = i2;
        this.mSwatchView = (GridView) ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.setting_color_swatch_layout, this, false);
        LayoutParams layoutParams = new LayoutParams(-1, -2);
        layoutParams.setMarginStart(i);
        layoutParams.setMarginEnd(i3);
        layoutParams.topMargin = i2;
        layoutParams.bottomMargin = i4;
        addView(this.mSwatchView, layoutParams);
        this.mSelectedView = new SpenColorSwatchSelectorView(context);
        addView(this.mSelectedView, new LayoutParams(1, 1));
        initSwatchList(context);
    }

    private void initSwatchList(Context context) {
        String string = context.getResources().getString(R.string.pen_string_button);
        this.mSwatchItemList = new ArrayList();
        int i = 0;
        while (true) {
            float[][] fArr = this.mHSV;
            if (i < fArr.length) {
                int i2 = this.mColumNum;
                SpenColorSwatchItem spenColorSwatchItem = new SpenColorSwatchItem(fArr[i][0], fArr[i][1], fArr[i][2]);
                this.mSwatchItemList.add(spenColorSwatchItem);
                i++;
            } else {
                this.mSwatchAdapter = new SpenColorSwatchAdapter(context, this.mSwatchItemList, this.mItemLayoutID);
                this.mSwatchView.setAdapter(this.mSwatchAdapter);
                this.mSwatchView.setOnItemClickListener(this.mSwatchItemClickListener);
                return;
            }
        }
    }

    private void notifyColorChanged(int i) {
        float[][] fArr = this.mHSV;
        float[] fArr2 = {fArr[i][0], fArr[i][1], fArr[i][2]};
        if (this.mPickerColor != null) {
            this.mPickerColor.setColor(TAG, 255, fArr2[0], fArr2[1], fArr2[2]);
        }
    }

    public boolean dispatchTouchEvent(MotionEvent motionEvent) {
        if (getParent() != null) {
            getParent().requestDisallowInterceptTouchEvent(true);
        }
        if (this.mSwatchView != null) {
            releaseDetected();
            int pointToPosition = this.mSwatchView.pointToPosition(((int) motionEvent.getX()) - this.mSwatchStartMargin, ((int) motionEvent.getY()) - this.mSwatchTopMargin);
            if (pointToPosition >= 0 && pointToPosition < this.mSwatchView.getChildCount() && pointToPosition != this.mCurrentPosition) {
                updatePosition(pointToPosition);
            }
        }
        if (motionEvent.getAction() == 0 && getParent() != null) {
            ((View) getParent()).playSoundEffect(0);
        }
        return true;
    }

    private void updatePosition(int i) {
        this.mSwatchAdapter.setSelectedPosition(i);
        notifyColorChanged(i);
        this.mCurrentPosition = i;
        updateSelector(i);
    }

    private boolean updateSelector(int i) {
        if (i == -1) {
            this.mSelectedView.setVisibility(View.GONE);
            return true;
        }
        Rect childRect = getChildRect(i);
        if (childRect == null) {
            return false;
        }
        int width = (int) (((float) childRect.width()) * 1.16f);
        int height = (int) (((float) childRect.height()) * 1.16f);
        updateSelector(width, height, (float) ((childRect.centerX() - (width / 2)) + this.mSwatchStartMargin), (float) ((childRect.centerY() - (height / 2)) + this.mSwatchTopMargin), ((Integer) this.mSwatchAdapter.getItem(i)).intValue());
        return true;
    }

    private Rect getChildRect(int i) {
        View childAt;
        GridView gridView = this.mSwatchView;
        if (gridView == null || i <= -1 || (childAt = gridView.getChildAt(i)) == null) {
            return null;
        }
        return new Rect(childAt.getLeft(), childAt.getTop(), childAt.getRight(), childAt.getBottom());
    }

    private void updateSelector(int i, int i2, float f, float f2, int i3) {
        SpenColorSwatchSelectorView spenColorSwatchSelectorView = this.mSelectedView;
        if (spenColorSwatchSelectorView != null) {
            if (spenColorSwatchSelectorView.getVisibility() != View.VISIBLE) {
                this.mSelectedView.setVisibility(View.VISIBLE);
            }
            LayoutParams layoutParams = (LayoutParams) this.mSelectedView.getLayoutParams();
            layoutParams.width = i;
            layoutParams.height = i2;
            this.mSelectedView.setLayoutParams(layoutParams);
            this.mSelectedView.setX(f);
            this.mSelectedView.setY(f2);
            this.mSelectedView.setBackgroundColor(i3);
            this.mSelectedView.setElevation((float) this.mSelectElevation);
        }
    }

    private void needUpdate(int i) {
        this.mReqPosIndex = i;
        if (this.mSwatchView != null && this.mSwatchViewObserver == null) {
            this.mSwatchViewObserver = new ViewTreeObserver.OnGlobalLayoutListener() {

                public void onGlobalLayout() {
                    if (SpenColorSwatchView.this.mSwatchView != null && SpenColorSwatchView.this.mSwatchViewObserver != null) {
                        SpenColorSwatchView spenColorSwatchView = SpenColorSwatchView.this;
                        Rect childRect = spenColorSwatchView.getChildRect(spenColorSwatchView.mReqPosIndex);
                        if (childRect != null) {
                            boolean z = SpenColorSwatchView.this.mReqPosIndex == SpenColorSwatchView.this.mLastPosIndex && SpenColorSwatchView.this.mLastCenterX == childRect.centerX();
                            if (!z) {
                                SpenColorSwatchView spenColorSwatchView2 = SpenColorSwatchView.this;
                                spenColorSwatchView2.mLastPosIndex = spenColorSwatchView2.mReqPosIndex;
                                SpenColorSwatchView.this.mLastCenterX = childRect.centerX();
                            }
                            SpenColorSwatchView spenColorSwatchView3 = SpenColorSwatchView.this;
                            spenColorSwatchView3.updateSelector(spenColorSwatchView3.mLastPosIndex);
                            if (z) {
                                SpenColorSwatchView.this.releaseDetected();
                            }
                        }
                    }
                }
            };
            this.mSwatchView.getViewTreeObserver().addOnGlobalLayoutListener(this.mSwatchViewObserver);
        }
    }

    private void releaseDetected() {
        if (this.mSwatchView != null && this.mSwatchViewObserver != null) {
            this.mSwatchView.getViewTreeObserver().removeOnGlobalLayoutListener(this.mSwatchViewObserver);
            this.mSwatchViewObserver = null;
            this.mLastPosIndex = -1;
            this.mReqPosIndex = -1;
            this.mLastCenterX = -1;
        }
    }

    private int findMatchedSwatch(float f, float f2, float f3) {
        int HSVToColor = SpenSettingUtil.HSVToColor(new float[]{f, f2, f3});
        if (this.mSwatchItemList != null) {
            for (int i = 0; i < this.mSwatchItemList.size(); i++) {
                if (this.mSwatchItemList.get(i).getColor() == HSVToColor) {
                    return i;
                }
            }
        }
        return -1;
    }

    public interface ActionListener {
        void onColorSelected(float f, float f2);
    }
}
