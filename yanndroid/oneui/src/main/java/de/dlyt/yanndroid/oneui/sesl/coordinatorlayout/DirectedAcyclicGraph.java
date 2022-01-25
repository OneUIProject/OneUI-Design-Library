package de.dlyt.yanndroid.oneui.sesl.coordinatorlayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.SimpleArrayMap;
import androidx.core.util.Pools;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public final class DirectedAcyclicGraph<T> {
    private final Pools.Pool<ArrayList<T>> mListPool = new Pools.SimplePool<>(10);
    private final SimpleArrayMap<T, ArrayList<T>> mGraph = new SimpleArrayMap<>();
    private final ArrayList<T> mSortResult = new ArrayList<>();
    private final HashSet<T> mSortTmpMarked = new HashSet<>();

    public void addNode(@NonNull T node) {
        if (!mGraph.containsKey(node)) {
            mGraph.put(node, null);
        }
    }

    public boolean contains(@NonNull T node) {
        return mGraph.containsKey(node);
    }

    public void addEdge(@NonNull T node, @NonNull T incomingEdge) {
        if (!mGraph.containsKey(node) || !mGraph.containsKey(incomingEdge)) {
            throw new IllegalArgumentException("All nodes must be present in the graph before being added as an edge");
        }

        ArrayList<T> edges = mGraph.get(node);
        if (edges == null) {
            edges = getEmptyList();
            mGraph.put(node, edges);
        }
        edges.add(incomingEdge);
    }

    @Nullable
    public List<T> getIncomingEdges(@NonNull T node) {
        return mGraph.get(node);
    }

    @Nullable
    public List<T> getOutgoingEdges(@NonNull T node) {
        ArrayList<T> result = null;
        for (int i = 0, size = mGraph.size(); i < size; i++) {
            ArrayList<T> edges = mGraph.valueAt(i);
            if (edges != null && edges.contains(node)) {
                if (result == null) {
                    result = new ArrayList<>();
                }
                result.add(mGraph.keyAt(i));
            }
        }
        return result;
    }

    public boolean hasOutgoingEdges(@NonNull T node) {
        for (int i = 0, size = mGraph.size(); i < size; i++) {
            ArrayList<T> edges = mGraph.valueAt(i);
            if (edges != null && edges.contains(node)) {
                return true;
            }
        }
        return false;
    }

    public void clear() {
        for (int i = 0, size = mGraph.size(); i < size; i++) {
            ArrayList<T> edges = mGraph.valueAt(i);
            if (edges != null) {
                poolList(edges);
            }
        }
        mGraph.clear();
    }

    @NonNull
    public ArrayList<T> getSortedList() {
        mSortResult.clear();
        mSortTmpMarked.clear();

        for (int i = 0, size = mGraph.size(); i < size; i++) {
            dfs(mGraph.keyAt(i), mSortResult, mSortTmpMarked);
        }

        return mSortResult;
    }

    private void dfs(final T node, final ArrayList<T> result, final HashSet<T> tmpMarked) {
        if (result.contains(node)) {
            return;
        }
        if (tmpMarked.contains(node)) {
            throw new RuntimeException("This graph contains cyclic dependencies");
        }
        tmpMarked.add(node);
        final ArrayList<T> edges = mGraph.get(node);
        if (edges != null) {
            for (int i = 0, size = edges.size(); i < size; i++) {
                dfs(edges.get(i), result, tmpMarked);
            }
        }
        tmpMarked.remove(node);
        result.add(node);
    }

    int size() {
        return mGraph.size();
    }

    @NonNull
    private ArrayList<T> getEmptyList() {
        ArrayList<T> list = mListPool.acquire();
        if (list == null) {
            list = new ArrayList<>();
        }
        return list;
    }

    private void poolList(@NonNull ArrayList<T> list) {
        list.clear();
        mListPool.release(list);
    }
}
