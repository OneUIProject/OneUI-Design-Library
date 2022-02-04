package de.dlyt.yanndroid.oneui.sesl.recyclerview;

import androidx.annotation.NonNull;
import androidx.collection.LongSparseArray;

import de.dlyt.yanndroid.oneui.view.RecyclerView;

interface StableIdStorage {
    @NonNull
    StableIdLookup createStableIdLookup();

    interface StableIdLookup {
        long localToGlobal(long localId);
    }

    class NoStableIdStorage implements StableIdStorage {
        private final StableIdLookup mNoIdLookup = new StableIdLookup() {
            @Override
            public long localToGlobal(long localId) {
                return RecyclerView.NO_ID;
            }
        };

        @NonNull
        @Override
        public StableIdLookup createStableIdLookup() {
            return mNoIdLookup;
        }
    }

    class SharedPoolStableIdStorage implements StableIdStorage {
        private final StableIdLookup mSameIdLookup = new StableIdLookup() {
            @Override
            public long localToGlobal(long localId) {
                return localId;
            }
        };

        @NonNull
        @Override
        public StableIdLookup createStableIdLookup() {
            return mSameIdLookup;
        }
    }

    class IsolatedStableIdStorage implements StableIdStorage {
        long mNextStableId = 0;

        long obtainId() {
            return mNextStableId++;
        }

        @NonNull
        @Override
        public StableIdLookup createStableIdLookup() {
            return new WrapperStableIdLookup();
        }

        class WrapperStableIdLookup implements StableIdLookup {
            private final LongSparseArray<Long> mLocalToGlobalLookup = new LongSparseArray<>();

            @Override
            public long localToGlobal(long localId) {
                Long globalId = mLocalToGlobalLookup.get(localId);
                if (globalId == null) {
                    globalId = obtainId();
                    mLocalToGlobalLookup.put(localId, globalId);
                }
                return globalId;
            }
        }
    }
}
