package de.dlyt.yanndroid.oneui.sesl.recyclerview;

import android.view.View;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class ViewBoundsCheck {
    static final int GT = 1 << 0;
    static final int EQ = 1 << 1;
    static final int LT = 1 << 2;
    static final int CVS_PVS_POS = 0;
    public static final int FLAG_CVS_GT_PVS = GT << CVS_PVS_POS;
    public static final int FLAG_CVS_EQ_PVS = EQ << CVS_PVS_POS;
    static final int FLAG_CVS_LT_PVS = LT << CVS_PVS_POS;
    static final int CVS_PVE_POS = 4;
    static final int FLAG_CVS_GT_PVE = GT << CVS_PVE_POS;
    static final int FLAG_CVS_EQ_PVE = EQ << CVS_PVE_POS;
    static final int FLAG_CVS_LT_PVE = LT << CVS_PVE_POS;
    static final int CVE_PVS_POS = 8;
    static final int FLAG_CVE_GT_PVS = GT << CVE_PVS_POS;
    static final int FLAG_CVE_EQ_PVS = EQ << CVE_PVS_POS;
    static final int FLAG_CVE_LT_PVS = LT << CVE_PVS_POS;
    static final int CVE_PVE_POS = 12;
    static final int FLAG_CVE_GT_PVE = GT << CVE_PVE_POS;
    public static final int FLAG_CVE_EQ_PVE = EQ << CVE_PVE_POS;
    public static final int FLAG_CVE_LT_PVE = LT << CVE_PVE_POS;
    static final int MASK = GT | EQ | LT;
    final Callback mCallback;
    BoundFlags mBoundFlags;

    @IntDef(flag = true, value = {FLAG_CVS_GT_PVS, FLAG_CVS_EQ_PVS, FLAG_CVS_LT_PVS, FLAG_CVS_GT_PVE, FLAG_CVS_EQ_PVE, FLAG_CVS_LT_PVE, FLAG_CVE_GT_PVS, FLAG_CVE_EQ_PVS, FLAG_CVE_LT_PVS, FLAG_CVE_GT_PVE, FLAG_CVE_EQ_PVE, FLAG_CVE_LT_PVE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ViewBounds {}

    public ViewBoundsCheck(Callback callback) {
        mCallback = callback;
        mBoundFlags = new BoundFlags();
    }

    View findOneViewWithinBoundFlags(int fromIndex, int toIndex, @ViewBounds int preferredBoundFlags, @ViewBounds int acceptableBoundFlags) {
        final int start = mCallback.getParentStart();
        final int end = mCallback.getParentEnd();
        final int next = toIndex > fromIndex ? 1 : -1;
        View acceptableMatch = null;
        for (int i = fromIndex; i != toIndex; i += next) {
            final View child = mCallback.getChildAt(i);
            final int childStart = mCallback.getChildStart(child);
            final int childEnd = mCallback.getChildEnd(child);
            mBoundFlags.setBounds(start, end, childStart, childEnd);
            if (preferredBoundFlags != 0) {
                mBoundFlags.resetFlags();
                mBoundFlags.addFlags(preferredBoundFlags);
                if (mBoundFlags.boundsMatch()) {
                    return child;
                }
            }
            if (acceptableBoundFlags != 0) {
                mBoundFlags.resetFlags();
                mBoundFlags.addFlags(acceptableBoundFlags);
                if (mBoundFlags.boundsMatch()) {
                    acceptableMatch = child;
                }
            }
        }
        return acceptableMatch;
    }

    public boolean isViewWithinBoundFlags(View child, @ViewBounds int boundsFlags) {
        mBoundFlags.setBounds(mCallback.getParentStart(), mCallback.getParentEnd(), mCallback.getChildStart(child), mCallback.getChildEnd(child));
        if (boundsFlags != 0) {
            mBoundFlags.resetFlags();
            mBoundFlags.addFlags(boundsFlags);
            return mBoundFlags.boundsMatch();
        }
        return false;
    }


    static class BoundFlags {
        int mBoundFlags = 0;
        int mRvStart, mRvEnd, mChildStart, mChildEnd;

        void setBounds(int rvStart, int rvEnd, int childStart, int childEnd) {
            mRvStart = rvStart;
            mRvEnd = rvEnd;
            mChildStart = childStart;
            mChildEnd = childEnd;
        }

        void addFlags(@ViewBounds int flags) {
            mBoundFlags |= flags;
        }

        void resetFlags() {
            mBoundFlags = 0;
        }

        int compare(int x, int y) {
            if (x > y) {
                return GT;
            }
            if (x == y) {
                return EQ;
            }
            return LT;
        }

        boolean boundsMatch() {
            if ((mBoundFlags & (MASK << CVS_PVS_POS)) != 0) {
                if ((mBoundFlags & (compare(mChildStart, mRvStart) << CVS_PVS_POS)) == 0) {
                    return false;
                }
            }

            if ((mBoundFlags & (MASK << CVS_PVE_POS)) != 0) {
                if ((mBoundFlags & (compare(mChildStart, mRvEnd) << CVS_PVE_POS)) == 0) {
                    return false;
                }
            }

            if ((mBoundFlags & (MASK << CVE_PVS_POS)) != 0) {
                if ((mBoundFlags & (compare(mChildEnd, mRvStart) << CVE_PVS_POS)) == 0) {
                    return false;
                }
            }

            if ((mBoundFlags & (MASK << CVE_PVE_POS)) != 0) {
                if ((mBoundFlags & (compare(mChildEnd, mRvEnd) << CVE_PVE_POS)) == 0) {
                    return false;
                }
            }
            return true;
        }
    };

    public interface Callback {
        View getChildAt(int index);

        int getParentStart();

        int getParentEnd();

        int getChildStart(View view);

        int getChildEnd(View view);
    }
}
