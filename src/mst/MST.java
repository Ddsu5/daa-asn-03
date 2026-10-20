package mst;

import graph.*;
import dsu.DSU;
import java.util.*;

public class MST {
    public record Result(List<Edge> edges, long weight, boolean connected) {}

    public static Result kruskal(Graph graph, Stats stats) {
        requireUndirected(graph);
        List<Edge> sorted = new ArrayList<>(graph.edges());
        sorted.sort((a, b) -> {
            stats.comparisons++;
            return Integer.compare(a.weight(), b.weight());
        });
        DSU dsu = new DSU(graph.size(), stats);
        List<Edge> chosen = new ArrayList<>();
        long total = 0;
        for (Edge edge : sorted) {
            stats.edges++;
            if (dsu.union(edge.from(), edge.to())) {
                chosen.add(edge);
                total += edge.weight();
                if (chosen.size() == graph.size() - 1) break;
            }
        }
        return new Result(chosen, total, chosen.size() == graph.size() - 1);
    }

    public static Result prim(Graph graph, Stats stats) {
        requireUndirected(graph);
        boolean[] visited = new boolean[graph.size()];
        PriorityQueue<Edge> queue = new PriorityQueue<>((a, b) -> {
            stats.comparisons++;
            return Integer.compare(a.weight(), b.weight());
        });
        List<Edge> chosen = new ArrayList<>();
        long total = 0;
        int components = 0;
        // Restart in each component to return a minimum spanning forest.
        for (int start = 0; start < graph.size(); start++) {
            if (visited[start]) continue;
            components++;
            visited[start] = true;
            addFrontier(graph, start, visited, queue, stats);
            while (!queue.isEmpty()) {
                Edge edge = queue.remove();
                stats.queueOps++;
                if (visited[edge.to()]) continue;
                visited[edge.to()] = true;
                chosen.add(edge);
                total += edge.weight();
                addFrontier(graph, edge.to(), visited, queue, stats);
            }
        }
        return new Result(chosen, total, components == 1);
    }

    private static void addFrontier(Graph graph, int vertex, boolean[] visited,
                                    PriorityQueue<Edge> queue, Stats stats) {
        for (Edge edge : graph.neighbors(vertex)) {
            stats.edges++;
            if (!visited[edge.to()]) {
                queue.add(edge);
                stats.queueOps++;
            }
        }
    }

    private static void requireUndirected(Graph graph) {
        if (graph.directed()) throw new IllegalArgumentException("MST requires an undirected graph");
    }
}
