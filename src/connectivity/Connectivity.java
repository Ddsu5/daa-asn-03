package connectivity;

import graph.*;
import java.util.*;

public class Connectivity {
    public record TopologicalResult(List<Integer> order, boolean dag) {}

    public static TopologicalResult topological(Graph graph, Stats stats) {
        requireDirected(graph);
        int[] indegree = new int[graph.size()];
        for (Edge edge : graph.edges()) {
            stats.edges++;
            indegree[edge.to()]++;
        }
        ArrayDeque<Integer> queue = new ArrayDeque<>();
        for (int v = 0; v < graph.size(); v++) {
            if (indegree[v] == 0) queue.add(v);
        }
        List<Integer> order = new ArrayList<>();
        while (!queue.isEmpty()) {
            int vertex = queue.remove();
            order.add(vertex);
            for (Edge edge : graph.neighbors(vertex)) {
                stats.edges++;
                if (--indegree[edge.to()] == 0) queue.add(edge.to());
            }
        }
        boolean dag = order.size() == graph.size();
        return new TopologicalResult(dag ? order : List.of(), dag);
    }

    public static List<List<Integer>> kosaraju(Graph graph, Stats stats) {
        requireDirected(graph);
        boolean[] visited = new boolean[graph.size()];
        List<Integer> finish = new ArrayList<>();
        // Iterative DFS with neighbor iterators preserves recursive finishing order.
        for (int start = 0; start < graph.size(); start++) {
            if (visited[start]) continue;
            ArrayDeque<Integer> vertices = new ArrayDeque<>();
            ArrayDeque<Iterator<Edge>> iterators = new ArrayDeque<>();
            visited[start] = true;
            vertices.push(start);
            iterators.push(graph.neighbors(start).iterator());
            while (!vertices.isEmpty()) {
                if (iterators.peek().hasNext()) {
                    Edge edge = iterators.peek().next();
                    stats.edges++;
                    if (!visited[edge.to()]) {
                        visited[edge.to()] = true;
                        vertices.push(edge.to());
                        iterators.push(graph.neighbors(edge.to()).iterator());
                    }
                } else {
                    finish.add(vertices.pop());
                    iterators.pop();
                }
            }
        }
        Graph reversed = new Graph(graph.size(), true);
        for (Edge edge : graph.edges()) {
            stats.edges++;
            reversed.addEdge(edge.to(), edge.from(), edge.weight());
        }
        Arrays.fill(visited, false);
        List<List<Integer>> components = new ArrayList<>();
        for (int i = finish.size() - 1; i >= 0; i--) {
            int start = finish.get(i);
            if (visited[start]) continue;
            List<Integer> component = new ArrayList<>();
            ArrayDeque<Integer> stack = new ArrayDeque<>();
            stack.push(start);
            visited[start] = true;
            while (!stack.isEmpty()) {
                int vertex = stack.pop();
                component.add(vertex);
                for (Edge edge : reversed.neighbors(vertex)) {
                    stats.edges++;
                    if (!visited[edge.to()]) {
                        visited[edge.to()] = true;
                        stack.push(edge.to());
                    }
                }
            }
            components.add(component);
        }
        return components;
    }

    private static void requireDirected(Graph graph) {
        if (!graph.directed()) throw new IllegalArgumentException("Directed graph required");
    }
}
