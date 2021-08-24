package de.dlyt.yanndroid.oneui.recyclerview;

import android.view.View;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

class SeslViewBoundsCheck {
    static final int CVE_PVE_POS = 12;
    static final int CVE_PVS_POS = 8;
    static final int CVS_PVE_POS = 4;
    static final int CVS_PVS_POS = 0;
    static final int EQ = 2;
    static final int FLAG_CVE_EQ_PVE = 8192;
    static final int FLAG_CVE_EQ_PVS = 512;
    static final int FLAG_CVE_GT_PVE = 4096;
    static final int FLAG_CVE_GT_PVS = 256;
    static final int FLAG_CVE_LT_PVE = 16384;
    static final int FLAG_CVE_LT_PVS = 1024;
    static final int FLAG_CVS_EQ_PVE = 32;
    static final int FLAG_CVS_EQ_PVS = 2;
    static final int FLAG_CVS_GT_PVE = 16;
    static final int FLAG_CVS_GT_PVS = 1;
    static final int FLAG_CVS_LT_PVE = 64;
    static final int FLAG_CVS_LT_PVS = 4;
    static final int GT = 1;
    static final int LT = 4;
    static final int MASK = 7;
    final Callback mCallback;
    BoundFlags mBoundFlags = new BoundFlags();

    SeslViewBoundsCheck(Callback callback) {
        mCallback = callback;
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

    boolean isViewWithinBoundFlags(View child, @ViewBounds int boundsFlags) {
        mBoundFlags.setBounds(mCallback.getParentStart(), mCallback.getParentEnd(), mCallback.getChildStart(child), mCallback.getChildEnd(child));
        if (boundsFlags != 0) {
            mBoundFlags.resetFlags();
            mBoundFlags.addFlags(boundsFlags);
            return mBoundFlags.boundsMatch();
        }
        return false;
    }

    ;

    @Retention(RetentionPolicy.SOURCE)
    public @interface ViewBounds {
    }

    interface Callback {
        int getChildCount();

        View getParent();

        View getChildAt(int index);

        int getParentStart();

        int getParentEnd();

        int getChildStart(View view);

        int getChildEnd(View view);
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

        void setFlags(@ViewBounds int flags, int mask) {
            mBoundFlags = (mBoundFlags & ~mask) | (flags & mask);
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
    }
}
