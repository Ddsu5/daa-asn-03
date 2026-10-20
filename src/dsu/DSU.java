package dsu;

import graph.Stats;

public class DSU {
    private final int[] parent, rank;
    private final Stats stats;

    public DSU(int n, Stats stats) {
        parent = new int[n];
        rank = new int[n];
        this.stats = stats;
        for (int i = 0; i < n; i++) parent[i] = i;
    }

    public int find(int x) {
        stats.finds++;
        int root = x;
        while (root != parent[root]) {
            stats.parentSteps++;
            root = parent[root];
        }
        // Full path compression: every visited vertex points directly to root.
        while (x != root) {
            int next = parent[x];
            parent[x] = root;
            x = next;
        }
        return root;
    }

    public boolean union(int a, int b) {
        stats.unions++;
        int rootA = find(a);
        int rootB = find(b);
        if (rootA == rootB) return false;
        if (rank[rootA] < rank[rootB]) parent[rootA] = rootB;
        else {
            parent[rootB] = rootA;
            if (rank[rootA] == rank[rootB]) rank[rootA]++;
        }
        return true;
    }
}
