package de.dlyt.yanndroid.oneui.sesl.picker.widget;

import android.os.Parcel;
import android.view.View;

public class SeslSpinningDatePicker$SavedState extends View.BaseSavedState {
    public static final Creator<SeslSpinningDatePicker$SavedState> CREATOR = new Creator<SeslSpinningDatePicker$SavedState>() {
        /* class androidx.picker.widget.SeslSpinningDatePicker$SavedState.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public SeslSpinningDatePicker$SavedState createFromParcel(Parcel parcel) {
            return new SeslSpinningDatePicker$SavedState(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public SeslSpinningDatePicker$SavedState[] newArray(int i) {
            return new SeslSpinningDatePicker$SavedState[i];
        }
    };
    public final long mMaxDate;
    public final long mMinDate;
    public final int mSelectedDay;
    public final int mSelectedMonth;
    public final int mSelectedYear;

    public SeslSpinningDatePicker$SavedState(Parcel parcel) {
        super(parcel);
        this.mSelectedYear = parcel.readInt();
        this.mSelectedMonth = parcel.readInt();
        this.mSelectedDay = parcel.readInt();
        this.mMinDate = parcel.readLong();
        this.mMaxDate = parcel.readLong();
    }

    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        parcel.writeInt(this.mSelectedYear);
        parcel.writeInt(this.mSelectedMonth);
        parcel.writeInt(this.mSelectedDay);
        parcel.writeLong(this.mMinDate);
        parcel.writeLong(this.mMaxDate);
    }
}
