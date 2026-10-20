package graph;

import java.util.*;

public class GraphGenerator {
    public static Graph generate(int vertices, int edges, long seed, String kind) {
        if (!Set.of("undirected", "dag", "cyclic").contains(kind))
            throw new IllegalArgumentException("Unknown graph kind");
        boolean directed = !kind.equals("undirected");
        long maximum = (long) vertices * (vertices - 1);
        if (!kind.equals("cyclic")) maximum /= 2;
        int minimum = kind.equals("cyclic") ? vertices : vertices - 1;
        if (vertices < 3 || edges < minimum || edges > maximum)
            throw new IllegalArgumentException("Invalid graph configuration");
        Graph graph = new Graph(vertices, directed);
        Set<Long> used = new HashSet<>();
        Random random = new Random(seed);
        // A random tree guarantees connectivity; increasing endpoints guarantee a DAG.
        if (kind.equals("cyclic")) {
            for (int v = 0; v < vertices; v++)
                add(graph, v, (v + 1) % vertices, used, random);
        } else {
            for (int v = 1; v < vertices; v++)
                add(graph, random.nextInt(v), v, used, random);
        }
        while (graph.edges().size() < edges) {
            int from = random.nextInt(vertices);
            int to = random.nextInt(vertices);
            if (from == to) continue;
            if (!kind.equals("cyclic") && from > to) {
                int temporary = from;
                from = to;
                to = temporary;
            }
            add(graph, from, to, used, random);
        }
        return graph;
    }

    private static void add(Graph graph, int from, int to, Set<Long> used, Random random) {
        long key = ((long) from << 32) | (to & 0xffffffffL);
        if (used.add(key)) graph.addEdge(from, to, 1 + random.nextInt(100));
    }
}
