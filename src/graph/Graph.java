package graph;

import java.util.*;

public class Graph {
    private final boolean directed;
    private final List<List<Edge>> adjacency = new ArrayList<>();
    private final List<Edge> edges = new ArrayList<>();

    public Graph(int vertices, boolean directed) {
        if (vertices < 1) throw new IllegalArgumentException("At least one vertex required");
        this.directed = directed;
        for (int i = 0; i < vertices; i++) adjacency.add(new ArrayList<>());
    }

    public void addEdge(int from, int to, int weight) {
        checkVertex(from);
        checkVertex(to);
        Edge edge = new Edge(from, to, weight);
        edges.add(edge);
        adjacency.get(from).add(edge);
        if (!directed) adjacency.get(to).add(new Edge(to, from, weight));
    }

    public void checkVertex(int vertex) {
        if (vertex < 0 || vertex >= size()) throw new IllegalArgumentException("Invalid vertex");
    }

    public int size() { return adjacency.size(); }
    public boolean directed() { return directed; }
    public List<Edge> edges() { return Collections.unmodifiableList(edges); }
    public List<Edge> neighbors(int vertex) { return Collections.unmodifiableList(adjacency.get(vertex)); }
}
