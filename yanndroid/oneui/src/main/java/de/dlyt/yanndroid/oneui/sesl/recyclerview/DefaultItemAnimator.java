package de.dlyt.yanndroid.oneui.sesl.recyclerview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.animation.Interpolator;
import android.view.animation.PathInterpolator;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;

import java.util.ArrayList;
import java.util.List;

import de.dlyt.yanndroid.oneui.view.RecyclerView;

public class DefaultItemAnimator extends SimpleItemAnimator {
    private static final boolean DEBUG = false;
    private static final String TAG = "DefaultItemAnimator";
    public static final int ADDITIONS_ANIM_PENDING = 8;
    public static final int CHANGE_ANIM_PENDING = 4;
    private static final long DELAY_ADD_START = 100;
    private static final long DURATION_ADD = 200;
    private static final long DURATION_CHANGE = 400;
    private static final long DURATION_MOVE = 400;
    private static final long DURATION_REMOVE = 100;
    private static final Interpolator ITEM_MOVE_INTERPOLATOR = new PathInterpolator(0.4f, 0.6f, 0.0f, 1.0f);
    public static final int MOVE_ANIM_PENDING = 2;
    public static final int PREV_ANIM_EXECUTED = 16;
    public static final int REMOVAL_ANIM_PENDING = 1;
    private static TimeInterpolator sDefaultInterpolator;
    private ArrayList<RecyclerView.ViewHolder> mPendingRemovals = new ArrayList<>();
    private ArrayList<RecyclerView.ViewHolder> mPendingAdditions = new ArrayList<>();
    private ArrayList<MoveInfo> mPendingMoves = new ArrayList<>();
    private ArrayList<ChangeInfo> mPendingChanges = new ArrayList<>();
    ArrayList<ArrayList<RecyclerView.ViewHolder>> mAdditionsList = new ArrayList<>();
    ArrayList<ArrayList<MoveInfo>> mMovesList = new ArrayList<>();
    ArrayList<ArrayList<ChangeInfo>> mChangesList = new ArrayList<>();
    ArrayList<RecyclerView.ViewHolder> mAddAnimations = new ArrayList<>();
    ArrayList<RecyclerView.ViewHolder> mMoveAnimations = new ArrayList<>();
    ArrayList<RecyclerView.ViewHolder> mRemoveAnimations = new ArrayList<>();
    ArrayList<RecyclerView.ViewHolder> mChangeAnimations = new ArrayList<>();
    private int mPendingAnimFlag = 0;
    private int mLastItemBottom = 0;
    private boolean mEnableRemoveDelay = false;

    @Override
    public long getAddDuration() {
        return DURATION_ADD;
    }

    @Override
    public long getChangeDuration() {
        return DURATION_CHANGE;
    }

    @Override
    public long getMoveDuration() {
        return DURATION_MOVE;
    }

    @Override
    public long getRemoveDuration() {
        return DURATION_REMOVE;
    }

    public void enableRemoveDelay(boolean enable) {
        mEnableRemoveDelay = enable;
    }

    public boolean isEnableRemoveDelay() {
        return mEnableRemoveDelay;
    }

    @Override
    public void runPendingAnimations() {
        boolean removalsPending = !mPendingRemovals.isEmpty();
        boolean movesPending = !mPendingMoves.isEmpty();
        boolean changesPending = !mPendingChanges.isEmpty();
        boolean additionsPending = !mPendingAdditions.isEmpty();
        if (!removalsPending && !movesPending && !additionsPending && !changesPending) {
            return;
        }
        for (RecyclerView.ViewHolder holder : mPendingRemovals) {
            animateRemoveImpl(holder);
        }
        mPendingRemovals.clear();
        if (movesPending) {
            final ArrayList<MoveInfo> moves = new ArrayList<>();
            moves.addAll(mPendingMoves);
            mMovesList.add(moves);
            mPendingMoves.clear();
            Runnable mover = new Runnable() {
                @Override
                public void run() {
                    for (MoveInfo moveInfo : moves) {
                        animateMoveImpl(moveInfo.holder, moveInfo.fromX, moveInfo.fromY, moveInfo.toX, moveInfo.toY);
                    }
                    moves.clear();
                    mMovesList.remove(moves);
                }
            };
            if (removalsPending && mEnableRemoveDelay) {
                View view = moves.get(0).holder.itemView;
                ViewCompat.postOnAnimationDelayed(view, mover, getRemoveDuration());
            } else {
                mover.run();
            }
        }
        if (changesPending) {
            final ArrayList<ChangeInfo> changes = new ArrayList<>();
            changes.addAll(mPendingChanges);
            mChangesList.add(changes);
            mPendingChanges.clear();
            Runnable changer = new Runnable() {
                @Override
                public void run() {
                    for (ChangeInfo change : changes) {
                        animateChangeImpl(change);
                    }
                    changes.clear();
                    mChangesList.remove(changes);
                }
            };
            if (removalsPending && mEnableRemoveDelay) {
                RecyclerView.ViewHolder holder = changes.get(0).oldHolder;
                ViewCompat.postOnAnimationDelayed(holder.itemView, changer, getRemoveDuration());
            } else {
                changer.run();
            }
        }
        if (additionsPending) {
            final ArrayList<RecyclerView.ViewHolder> additions = new ArrayList<>();
            additions.addAll(mPendingAdditions);
            mAdditionsList.add(additions);
            mPendingAdditions.clear();
            Runnable adder = new Runnable() {
                @Override
                public void run() {
                    for (RecyclerView.ViewHolder holder : additions) {
                        animateAddImpl(holder);
                    }
                    additions.clear();
                    mAdditionsList.remove(additions);
                }
            };
            if (removalsPending || movesPending || changesPending) {
                View view = additions.get(0).itemView;
                if (view.getTag() == null || !view.getTag().equals("preferencecategory")) {
                    ViewCompat.postOnAnimationDelayed(view, adder, DELAY_ADD_START);
                } else {
                    adder.run();
                }

            } else {
                adder.run();
            }
        }
    }

    @Override
    public boolean animateRemove(final RecyclerView.ViewHolder holder) {
        resetAnimation(holder);
        mPendingRemovals.add(holder);
        if (holder.itemView.getBottom() > mLastItemBottom) {
            mLastItemBottom = holder.itemView.getBottom();
        }
        if ((mPendingAnimFlag & REMOVAL_ANIM_PENDING) == 0) {
            mPendingAnimFlag = mPendingAnimFlag |REMOVAL_ANIM_PENDING;
        }
        return true;
    }

    private void animateRemoveImpl(final RecyclerView.ViewHolder holder) {
        final View view = holder.itemView;
        final ViewPropertyAnimator animation = view.animate();
        mRemoveAnimations.add(holder);
        animation.setDuration(view.getTag() != null && view.getTag().equals("preferencecategory") ? 0 : getRemoveDuration()).alpha(0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animator) {
                dispatchRemoveStarting(holder);
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                animation.setListener(null);
                view.setAlpha(1);
                dispatchRemoveFinished(holder);
                mRemoveAnimations.remove(holder);
                dispatchFinishedWhenDone();
                if ((mPendingAnimFlag & REMOVAL_ANIM_PENDING) != 0) {
                    mPendingAnimFlag = -2 & mPendingAnimFlag;
                }
            }
        }).start();
    }

    @Override
    public boolean animateAdd(final RecyclerView.ViewHolder holder) {
        resetAnimation(holder);
        holder.itemView.setAlpha(0);
        mPendingAdditions.add(holder);
        if ((mPendingAnimFlag & ADDITIONS_ANIM_PENDING) == 0) {
            mPendingAnimFlag = mPendingAnimFlag | ADDITIONS_ANIM_PENDING;
        }
        return true;
    }

    void animateAddImpl(final RecyclerView.ViewHolder holder) {
        final View view = holder.itemView;
        final ViewPropertyAnimator animation = view.animate();
        mAddAnimations.add(holder);
        animation.alpha(1).setDuration(view.getTag() != null && view.getTag().equals("preferencecategory") ? 0 : getAddDuration()).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animator) {
                dispatchAddStarting(holder);
            }

            @Override
            public void onAnimationCancel(Animator animator) {
                view.setAlpha(1);
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                animation.setListener(null);
                dispatchAddFinished(holder);
                mAddAnimations.remove(holder);
                dispatchFinishedWhenDone();
                if ((mPendingAnimFlag & ADDITIONS_ANIM_PENDING) != 0) {
                    mPendingAnimFlag = -9 & mPendingAnimFlag;
                }
                if ((mPendingAnimFlag & PREV_ANIM_EXECUTED) != 0) {
                    mPendingAnimFlag = -17 & mPendingAnimFlag;
                }
            }
        }).start();
    }

    @Override
    public boolean animateMove(final RecyclerView.ViewHolder holder, int fromX, int fromY, int toX, int toY) {
        final View view = holder.itemView;
        fromX += (int) holder.itemView.getTranslationX();
        fromY += (int) holder.itemView.getTranslationY();
        resetAnimation(holder);
        int deltaX = toX - fromX;
        int deltaY = toY - fromY;
        if (deltaX == 0 && deltaY == 0) {
            dispatchMoveFinished(holder);
            return false;
        }
        if (deltaX != 0) {
            view.setTranslationX(-deltaX);
        }
        if (deltaY != 0) {
            view.setTranslationY(-deltaY);
        }
        mPendingMoves.add(new MoveInfo(holder, fromX, fromY, toX, toY));
        if ((mPendingAnimFlag & MOVE_ANIM_PENDING) == 0) {
            mPendingAnimFlag = mPendingAnimFlag | MOVE_ANIM_PENDING;
        }
        return true;
    }

    void animateMoveImpl(final RecyclerView.ViewHolder holder, int fromX, int fromY, int toX, int toY) {
        final View view = holder.itemView;
        final int deltaX = toX - fromX;
        final int deltaY = toY - fromY;
        if (deltaX != 0) {
            view.animate().translationX(0);
        }
        if (deltaY != 0) {
            view.animate().translationY(0);
        }
        final ViewPropertyAnimator animation = view.animate();
        mMoveAnimations.add(holder);
        if (getHostView() instanceof RecyclerView) {
            final RecyclerView recyclerView = (RecyclerView) getHostView();
            if (recyclerView.mBlackTop != -1 && holder.getLayoutPosition() == recyclerView.mChildHelper.getChildCount() - 1) {
                animation.setUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        recyclerView.invalidate();
                    }
                });
            }
        }
        animation.setDuration(getMoveDuration()).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animator) {
                dispatchMoveStarting(holder);
            }

            @Override
            public void onAnimationCancel(Animator animator) {
                if (deltaX != 0) {
                    view.setTranslationX(0);
                }
                if (deltaY != 0) {
                    view.setTranslationY(0);
                }
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                animation.setListener(null);
                dispatchMoveFinished(holder);
                mMoveAnimations.remove(holder);
                dispatchFinishedWhenDone();
                if ((mPendingAnimFlag & MOVE_ANIM_PENDING) != 0) {
                    mPendingAnimFlag = -3 & mPendingAnimFlag;
                }
                if ((mPendingAnimFlag & ADDITIONS_ANIM_PENDING) != 0) {
                    mPendingAnimFlag = 16 | mPendingAnimFlag;
                }
            }
        }).start();
    }

    @Override
    public boolean animateChange(RecyclerView.ViewHolder oldHolder, RecyclerView.ViewHolder newHolder, int fromLeft, int fromTop, int toLeft, int toTop) {
        if (oldHolder == newHolder) {
            return animateMove(oldHolder, fromLeft, fromTop, toLeft, toTop);
        }
        final float prevTranslationX = oldHolder.itemView.getTranslationX();
        final float prevTranslationY = oldHolder.itemView.getTranslationY();
        final float prevAlpha = oldHolder.itemView.getAlpha();
        resetAnimation(oldHolder);
        int deltaX = (int) (toLeft - fromLeft - prevTranslationX);
        int deltaY = (int) (toTop - fromTop - prevTranslationY);
        oldHolder.itemView.setTranslationX(prevTranslationX);
        oldHolder.itemView.setTranslationY(prevTranslationY);
        oldHolder.itemView.setAlpha(prevAlpha);
        if (newHolder != null) {
            resetAnimation(newHolder);
            newHolder.itemView.setTranslationX(-deltaX);
            newHolder.itemView.setTranslationY(-deltaY);
            newHolder.itemView.setAlpha(0);
        }
        mPendingChanges.add(new ChangeInfo(oldHolder, newHolder, fromLeft, fromTop, toLeft, toTop));
        if ((mPendingAnimFlag & CHANGE_ANIM_PENDING) == 0) {
            mPendingAnimFlag = mPendingAnimFlag | CHANGE_ANIM_PENDING;
        }
        return true;
    }

    void animateChangeImpl(final ChangeInfo changeInfo) {
        final RecyclerView.ViewHolder holder = changeInfo.oldHolder;
        final View view = holder == null ? null : holder.itemView;
        final RecyclerView.ViewHolder newHolder = changeInfo.newHolder;
        final View newView = newHolder != null ? newHolder.itemView : null;
        if (view != null) {
            final ViewPropertyAnimator oldViewAnim = view.animate().setDuration(getChangeDuration());
            mChangeAnimations.add(changeInfo.oldHolder);
            oldViewAnim.translationX(changeInfo.toX - changeInfo.fromX);
            oldViewAnim.translationY(changeInfo.toY - changeInfo.fromY);
            oldViewAnim.alpha(0).setDuration(getChangeDuration()).setInterpolator(ITEM_MOVE_INTERPOLATOR).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animator) {
                    dispatchChangeStarting(changeInfo.oldHolder, true);
                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    oldViewAnim.setListener(null);
                    view.setAlpha(1);
                    view.setTranslationX(0);
                    view.setTranslationY(0);
                    dispatchChangeFinished(changeInfo.oldHolder, true);
                    mChangeAnimations.remove(changeInfo.oldHolder);
                    dispatchFinishedWhenDone();
                    if ((mPendingAnimFlag & CHANGE_ANIM_PENDING) != 0) {
                        mPendingAnimFlag = -5 & mPendingAnimFlag;
                    }
                }
            }).start();
        }
        if (newView != null) {
            final ViewPropertyAnimator newViewAnimation = newView.animate();
            mChangeAnimations.add(changeInfo.newHolder);
            newViewAnimation.translationX(0).translationY(0).setDuration(getChangeDuration()).alpha(1).setInterpolator(ITEM_MOVE_INTERPOLATOR).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animator) {
                    dispatchChangeStarting(changeInfo.newHolder, false);
                }
                @Override
                public void onAnimationEnd(Animator animator) {
                    newViewAnimation.setListener(null);
                    newView.setAlpha(1);
                    newView.setTranslationX(0);
                    newView.setTranslationY(0);
                    dispatchChangeFinished(changeInfo.newHolder, false);
                    mChangeAnimations.remove(changeInfo.newHolder);
                    dispatchFinishedWhenDone();
                }
            }).start();
        }
    }

    private void endChangeAnimation(List<ChangeInfo> infoList, RecyclerView.ViewHolder item) {
        for (int i = infoList.size() - 1; i >= 0; i--) {
            ChangeInfo changeInfo = infoList.get(i);
            if (endChangeAnimationIfNecessary(changeInfo, item)) {
                if (changeInfo.oldHolder == null && changeInfo.newHolder == null) {
                    infoList.remove(changeInfo);
                }
            }
        }
    }

    private void endChangeAnimationIfNecessary(ChangeInfo changeInfo) {
        if (changeInfo.oldHolder != null) {
            endChangeAnimationIfNecessary(changeInfo, changeInfo.oldHolder);
        }
        if (changeInfo.newHolder != null) {
            endChangeAnimationIfNecessary(changeInfo, changeInfo.newHolder);
        }
    }

    private boolean endChangeAnimationIfNecessary(ChangeInfo changeInfo, RecyclerView.ViewHolder item) {
        boolean oldItem = false;
        if (changeInfo.newHolder == item) {
            changeInfo.newHolder = null;
        } else if (changeInfo.oldHolder == item) {
            changeInfo.oldHolder = null;
            oldItem = true;
        } else {
            return false;
        }
        item.itemView.setAlpha(1);
        item.itemView.setTranslationX(0);
        item.itemView.setTranslationY(0);
        dispatchChangeFinished(item, oldItem);
        return true;
    }

    @Override
    public void endAnimation(RecyclerView.ViewHolder item) {
        final View view = item.itemView;
        view.animate().cancel();
        for (int i = mPendingMoves.size() - 1; i >= 0; i--) {
            MoveInfo moveInfo = mPendingMoves.get(i);
            if (moveInfo.holder == item) {
                view.setTranslationY(0);
                view.setTranslationX(0);
                dispatchMoveFinished(item);
                mPendingMoves.remove(i);
            }
        }
        endChangeAnimation(mPendingChanges, item);
        if (mPendingRemovals.remove(item)) {
            view.setAlpha(1);
            dispatchRemoveFinished(item);
        }
        if (mPendingAdditions.remove(item)) {
            view.setAlpha(1);
            dispatchAddFinished(item);
        }

        for (int i = mChangesList.size() - 1; i >= 0; i--) {
            ArrayList<ChangeInfo> changes = mChangesList.get(i);
            endChangeAnimation(changes, item);
            if (changes.isEmpty()) {
                mChangesList.remove(i);
            }
        }
        for (int i = mMovesList.size() - 1; i >= 0; i--) {
            ArrayList<MoveInfo> moves = mMovesList.get(i);
            for (int j = moves.size() - 1; j >= 0; j--) {
                MoveInfo moveInfo = moves.get(j);
                if (moveInfo.holder == item) {
                    view.setTranslationY(0);
                    view.setTranslationX(0);
                    dispatchMoveFinished(item);
                    moves.remove(j);
                    if (moves.isEmpty()) {
                        mMovesList.remove(i);
                    }
                    break;
                }
            }
        }
        for (int i = mAdditionsList.size() - 1; i >= 0; i--) {
            ArrayList<RecyclerView.ViewHolder> additions = mAdditionsList.get(i);
            if (additions.remove(item)) {
                view.setAlpha(1);
                dispatchAddFinished(item);
                if (additions.isEmpty()) {
                    mAdditionsList.remove(i);
                }
            }
        }

        if (mRemoveAnimations.remove(item) && DEBUG) {
            throw new IllegalStateException("after animation is cancelled, item should not be in mRemoveAnimations list");
        }

        if (mAddAnimations.remove(item) && DEBUG) {
            throw new IllegalStateException("after animation is cancelled, item should not be in mAddAnimations list");
        }

        if (mChangeAnimations.remove(item) && DEBUG) {
            throw new IllegalStateException("after animation is cancelled, item should not be in mChangeAnimations list");
        }

        if (mMoveAnimations.remove(item) && DEBUG) {
            throw new IllegalStateException("after animation is cancelled, item should not be in mMoveAnimations list");
        }
        dispatchFinishedWhenDone();
    }

    private void resetAnimation(RecyclerView.ViewHolder holder) {
        if (sDefaultInterpolator == null) {
            sDefaultInterpolator = new ValueAnimator().getInterpolator();
        }
        holder.itemView.animate().setInterpolator(sDefaultInterpolator);
        endAnimation(holder);
    }

    @Override
    public boolean isRunning() {
        return (!mPendingAdditions.isEmpty() || !mPendingChanges.isEmpty() || !mPendingMoves.isEmpty() || !mPendingRemovals.isEmpty() || !mMoveAnimations.isEmpty() || !mRemoveAnimations.isEmpty() || !mAddAnimations.isEmpty() || !mChangeAnimations.isEmpty() || !mMovesList.isEmpty() || !mAdditionsList.isEmpty() || !mChangesList.isEmpty());
    }

    void dispatchFinishedWhenDone() {
        if (!isRunning()) {
            dispatchAnimationsFinished();
        }
    }

    @Override
    public void endAnimations() {
        int count = mPendingMoves.size();
        for (int i = count - 1; i >= 0; i--) {
            MoveInfo item = mPendingMoves.get(i);
            View view = item.holder.itemView;
            view.setTranslationY(0);
            view.setTranslationX(0);
            dispatchMoveFinished(item.holder);
            mPendingMoves.remove(i);
        }
        count = mPendingRemovals.size();
        for (int i = count - 1; i >= 0; i--) {
            RecyclerView.ViewHolder item = mPendingRemovals.get(i);
            dispatchRemoveFinished(item);
            mPendingRemovals.remove(i);
        }
        count = mPendingAdditions.size();
        for (int i = count - 1; i >= 0; i--) {
            RecyclerView.ViewHolder item = mPendingAdditions.get(i);
            item.itemView.setAlpha(1);
            dispatchAddFinished(item);
            mPendingAdditions.remove(i);
        }
        count = mPendingChanges.size();
        for (int i = count - 1; i >= 0; i--) {
            endChangeAnimationIfNecessary(mPendingChanges.get(i));
        }
        mPendingChanges.clear();
        if (!isRunning()) {
            return;
        }

        int listCount = mMovesList.size();
        for (int i = listCount - 1; i >= 0; i--) {
            ArrayList<MoveInfo> moves = mMovesList.get(i);
            count = moves.size();
            for (int j = count - 1; j >= 0; j--) {
                MoveInfo moveInfo = moves.get(j);
                RecyclerView.ViewHolder item = moveInfo.holder;
                View view = item.itemView;
                view.setTranslationY(0);
                view.setTranslationX(0);
                dispatchMoveFinished(moveInfo.holder);
                moves.remove(j);
                if (moves.isEmpty()) {
                    mMovesList.remove(moves);
                }
            }
        }
        listCount = mAdditionsList.size();
        for (int i = listCount - 1; i >= 0; i--) {
            ArrayList<RecyclerView.ViewHolder> additions = mAdditionsList.get(i);
            count = additions.size();
            for (int j = count - 1; j >= 0; j--) {
                RecyclerView.ViewHolder item = additions.get(j);
                View view = item.itemView;
                view.setAlpha(1);
                dispatchAddFinished(item);
                additions.remove(j);
                if (additions.isEmpty()) {
                    mAdditionsList.remove(additions);
                }
            }
        }
        listCount = mChangesList.size();
        for (int i = listCount - 1; i >= 0; i--) {
            ArrayList<ChangeInfo> changes = mChangesList.get(i);
            count = changes.size();
            for (int j = count - 1; j >= 0; j--) {
                endChangeAnimationIfNecessary(changes.get(j));
                if (changes.isEmpty()) {
                    mChangesList.remove(changes);
                }
            }
        }

        cancelAll(mRemoveAnimations);
        cancelAll(mMoveAnimations);
        cancelAll(mAddAnimations);
        cancelAll(mChangeAnimations);

        dispatchAnimationsFinished();
    }

    void cancelAll(List<RecyclerView.ViewHolder> viewHolders) {
        for (int i = viewHolders.size() - 1; i >= 0; i--) {
            viewHolders.get(i).itemView.animate().cancel();
        }
    }

    @Override
    public boolean canReuseUpdatedViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, @NonNull List<Object> payloads) {
        return !payloads.isEmpty() || super.canReuseUpdatedViewHolder(viewHolder, payloads);
    }

    public int getPendingAnimFlag() {
        return mPendingAnimFlag;
    }

    public void clearPendingAnimFlag() {
        mPendingAnimFlag = 0;
    }

    public int getLastItemBottom() {
        return mLastItemBottom;
    }


    private static class MoveInfo {
        public RecyclerView.ViewHolder holder;
        public int fromX, fromY, toX, toY;

        MoveInfo(RecyclerView.ViewHolder holder, int fromX, int fromY, int toX, int toY) {
            this.holder = holder;
            this.fromX = fromX;
            this.fromY = fromY;
            this.toX = toX;
            this.toY = toY;
        }
    }

    private static class ChangeInfo {
        public RecyclerView.ViewHolder oldHolder, newHolder;
        public int fromX, fromY, toX, toY;

        private ChangeInfo(RecyclerView.ViewHolder oldHolder, RecyclerView.ViewHolder newHolder) {
            this.oldHolder = oldHolder;
            this.newHolder = newHolder;
        }

        ChangeInfo(RecyclerView.ViewHolder oldHolder, RecyclerView.ViewHolder newHolder, int fromX, int fromY, int toX, int toY) {
            this(oldHolder, newHolder);
            this.fromX = fromX;
            this.fromY = fromY;
            this.toX = toX;
            this.toY = toY;
        }

        @Override
        public String toString() {
            return "ChangeInfo{oldHolder=" + oldHolder + ", newHolder=" + newHolder + ", fromX=" + fromX + ", fromY=" + fromY + ", toX=" + toX + ", toY=" + toY + '}';
        }
    }
}
