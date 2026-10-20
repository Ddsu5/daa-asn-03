package shortestpath;

import graph.*;
import java.util.*;

public class Dijkstra {
    private record Entry(int vertex, long distance) {}
    public record Result(long[] distances, int[] previous) {
        public List<Integer> pathTo(int target) {
            if (distances[target] == Long.MAX_VALUE) return List.of();
            List<Integer> path = new ArrayList<>();
            for (int v = target; v != -1; v = previous[v]) path.add(v);
            Collections.reverse(path);
            return path;
        }
    }

    public static Result run(Graph graph, int source, Stats stats) {
        graph.checkVertex(source);
        for (Edge edge : graph.edges()) {
            stats.edges++;
            if (edge.weight() < 0) throw new IllegalArgumentException("Negative weight");
        }
        long[] distance = new long[graph.size()];
        int[] previous = new int[graph.size()];
        Arrays.fill(distance, Long.MAX_VALUE);
        Arrays.fill(previous, -1);
        distance[source] = 0;
        PriorityQueue<Entry> queue = new PriorityQueue<>((a, b) -> {
            stats.comparisons++;
            return Long.compare(a.distance(), b.distance());
        });
        queue.add(new Entry(source, 0));
        stats.queueOps++;
        while (!queue.isEmpty()) {
            Entry current = queue.remove();
            stats.queueOps++;
            if (current.distance() != distance[current.vertex()]) continue;
            for (Edge edge : graph.neighbors(current.vertex())) {
                stats.edges++;
                long candidate = current.distance() + edge.weight();
                if (candidate < distance[edge.to()]) {
                    distance[edge.to()] = candidate;
                    previous[edge.to()] = current.vertex();
                    queue.add(new Entry(edge.to(), candidate));
                    stats.queueOps++;
                }
            }
        }
        return new Result(distance, previous);
    }
}
