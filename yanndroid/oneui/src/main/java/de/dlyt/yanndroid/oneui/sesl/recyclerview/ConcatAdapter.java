package de.dlyt.yanndroid.oneui.sesl.recyclerview;

import static de.dlyt.yanndroid.oneui.sesl.recyclerview.ConcatAdapter.Config.StableIdMode.NO_STABLE_IDS;

import android.util.Pair;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import de.dlyt.yanndroid.oneui.view.RecyclerView;
import de.dlyt.yanndroid.oneui.view.RecyclerView.Adapter;
import de.dlyt.yanndroid.oneui.view.RecyclerView.ViewHolder;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("unchecked")
public final class ConcatAdapter extends Adapter<ViewHolder> {
    static final String TAG = "ConcatAdapter";
    private final ConcatAdapterController mController;

    @SafeVarargs
    public ConcatAdapter(@NonNull Adapter<? extends ViewHolder>... adapters) {
        this(Config.DEFAULT, adapters);
    }

    @SafeVarargs
    public ConcatAdapter(@NonNull Config config, @NonNull Adapter<? extends ViewHolder>... adapters) {
        this(config, Arrays.asList(adapters));
    }

    public ConcatAdapter(@NonNull List<? extends Adapter<? extends ViewHolder>> adapters) {
        this(Config.DEFAULT, adapters);
    }

    public ConcatAdapter(@NonNull Config config, @NonNull List<? extends Adapter<? extends ViewHolder>> adapters) {
        mController = new ConcatAdapterController(this, config);
        for (Adapter<? extends ViewHolder> adapter : adapters) {
            addAdapter(adapter);
        }
        super.setHasStableIds(mController.hasStableIds());
    }

    public boolean addAdapter(@NonNull Adapter<? extends ViewHolder> adapter) {
        return mController.addAdapter((Adapter<ViewHolder>) adapter);
    }

    public boolean addAdapter(int index, @NonNull Adapter<? extends ViewHolder> adapter) {
        return mController.addAdapter(index, (Adapter<ViewHolder>) adapter);
    }

    public boolean removeAdapter(@NonNull Adapter<? extends ViewHolder> adapter) {
        return mController.removeAdapter((Adapter<ViewHolder>) adapter);
    }

    @Override
    public int getItemViewType(int position) {
        return mController.getItemViewType(position);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return mController.onCreateViewHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        mController.onBindViewHolder(holder, position);
    }

    @Override
    public void setHasStableIds(boolean hasStableIds) {
        throw new UnsupportedOperationException("Calling setHasStableIds is not allowed on the ConcatAdapter. Use the Config object passed in the constructor to control this behavior");
    }

    @Override
    public void setStateRestorationPolicy(@NonNull StateRestorationPolicy strategy) {
        throw new UnsupportedOperationException("Calling setStateRestorationPolicy is not allowed on the ConcatAdapter. This value is inferred from added adapters");
    }

    @Override
    public long getItemId(int position) {
        return mController.getItemId(position);
    }

    void internalSetStateRestorationPolicy(@NonNull StateRestorationPolicy strategy) {
        super.setStateRestorationPolicy(strategy);
    }

    @Override
    public int getItemCount() {
        return mController.getTotalCount();
    }

    @Override
    public boolean onFailedToRecycleView(@NonNull ViewHolder holder) {
        return mController.onFailedToRecycleView(holder);
    }

    @Override
    public void onViewAttachedToWindow(@NonNull ViewHolder holder) {
        mController.onViewAttachedToWindow(holder);
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull ViewHolder holder) {
        mController.onViewDetachedFromWindow(holder);
    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        mController.onViewRecycled(holder);
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        mController.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        mController.onDetachedFromRecyclerView(recyclerView);
    }

    @NonNull
    public List<? extends Adapter<? extends ViewHolder>> getAdapters() {
        return Collections.unmodifiableList(mController.getCopyOfAdapters());
    }

    @Override
    public int findRelativeAdapterPositionIn(@NonNull Adapter<? extends ViewHolder> adapter, @NonNull ViewHolder viewHolder, int localPosition) {
        return mController.getLocalAdapterPosition(adapter, viewHolder, localPosition);
    }

    @NonNull
    public Pair<Adapter<? extends ViewHolder>, Integer> getWrappedAdapterAndPosition(int globalPosition) {
        return mController.getWrappedAdapterAndPosition(globalPosition);
    }


    public static final class Config {
        public final boolean isolateViewTypes;
        @NonNull
        public final StableIdMode stableIdMode;
        @NonNull
        public static final Config DEFAULT = new Config(true, NO_STABLE_IDS);

        Config(boolean isolateViewTypes, @NonNull StableIdMode stableIdMode) {
            this.isolateViewTypes = isolateViewTypes;
            this.stableIdMode = stableIdMode;
        }

        public enum StableIdMode {
            NO_STABLE_IDS,
            ISOLATED_STABLE_IDS,
            SHARED_STABLE_IDS
        }

        public static final class Builder {
            private boolean mIsolateViewTypes = DEFAULT.isolateViewTypes;
            private StableIdMode mStableIdMode = DEFAULT.stableIdMode;

            @NonNull
            public Builder setIsolateViewTypes(boolean isolateViewTypes) {
                mIsolateViewTypes = isolateViewTypes;
                return this;
            }

            @NonNull
            public Builder setStableIdMode(@NonNull StableIdMode stableIdMode) {
                mStableIdMode = stableIdMode;
                return this;
            }

            @NonNull
            public Config build() {
                return new Config(mIsolateViewTypes, mStableIdMode);
            }
        }
    }
}
