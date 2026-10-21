import graph.*;
import dsu.DSU;
import mst.MST;
import connectivity.Connectivity;
import shortestpath.Dijkstra;
import java.util.*;

public class Tests {
    private static int checks;

    private static void check(boolean condition, String message) {
        checks++;
        if (!condition) throw new AssertionError(message);
    }

    private static void rejects(Runnable action, String message) {
        boolean rejected = false;
        try { action.run(); }
        catch (IllegalArgumentException expected) { rejected = true; }
        check(rejected, message);
    }

    private static void mst(Graph graph, long weight, boolean connected) {
        MST.Result k = MST.kruskal(graph, new Stats());
        MST.Result p = MST.prim(graph, new Stats());
        check(k.weight() == weight && p.weight() == weight, "Expected MST/forest weight");
        check(k.connected() == connected && p.connected() == connected, "Connectivity");
        for (MST.Result result : List.of(k, p)) {
            DSU dsu = new DSU(graph.size(), new Stats());
            for (Edge edge : result.edges())
                check(dsu.union(edge.from(), edge.to()), "MST must be acyclic");
            if (connected) check(result.edges().size() == graph.size() - 1, "MST edge count");
        }
    }

    private static void validOrder(Graph graph) {
        var result = Connectivity.topological(graph, new Stats());
        check(result.dag(), "DAG accepted");
        check(new HashSet<>(result.order()).size() == graph.size(), "Every vertex in order");
        int[] position = new int[graph.size()];
        for (int i = 0; i < result.order().size(); i++) position[result.order().get(i)] = i;
        for (Edge edge : graph.edges())
            check(position[edge.from()] < position[edge.to()], "Topological edge direction");
    }

    // Independent reachability oracle: same SCC iff mutually reachable.
    private static void verifyScc(Graph graph) {
        boolean[][] reachable = new boolean[graph.size()][graph.size()];
        for (int v = 0; v < graph.size(); v++) reachable[v][v] = true;
        for (Edge edge : graph.edges()) reachable[edge.from()][edge.to()] = true;
        for (int k = 0; k < graph.size(); k++)
            for (int i = 0; i < graph.size(); i++)
                for (int j = 0; j < graph.size(); j++)
                    reachable[i][j] |= reachable[i][k] && reachable[k][j];
        int[] component = new int[graph.size()];
        Arrays.fill(component, -1);
        var groups = Connectivity.kosaraju(graph, new Stats());
        for (int i = 0; i < groups.size(); i++) {
            for (int vertex : groups.get(i)) {
                check(component[vertex] == -1, "No duplicate SCC member");
                component[vertex] = i;
            }
        }
        for (int i = 0; i < graph.size(); i++) {
            check(component[i] != -1, "Every vertex belongs to an SCC");
            for (int j = 0; j < graph.size(); j++)
                check((component[i] == component[j]) == (reachable[i][j] && reachable[j][i]),
                        "SCC matches mutual reachability");
        }
    }

    // Bellman-Ford is used only as a test oracle, not as the submitted route algorithm.
    private static void verifyDistances(Graph graph) {
        for (int source = 0; source < graph.size(); source++) {
            long[] expected = new long[graph.size()];
            Arrays.fill(expected, Long.MAX_VALUE);
            expected[source] = 0;
            for (int pass = 1; pass < graph.size(); pass++) {
                for (int v = 0; v < graph.size(); v++) {
                    if (expected[v] == Long.MAX_VALUE) continue;
                    for (Edge edge : graph.neighbors(v))
                        expected[edge.to()] = Math.min(expected[edge.to()], expected[v] + edge.weight());
                }
            }
            var actual = Dijkstra.run(graph, source, new Stats());
            check(Arrays.equals(expected, actual.distances()), "Distances match Bellman-Ford");
            for (int target = 0; target < graph.size(); target++) {
                var path = actual.pathTo(target);
                if (expected[target] == Long.MAX_VALUE) {
                    check(path.isEmpty(), "Unreachable path is empty");
                    continue;
                }
                check(path.get(0) == source && path.get(path.size() - 1) == target, "Path endpoints");
                long sum = 0;
                for (int i = 1; i < path.size(); i++) {
                    int from = path.get(i - 1), to = path.get(i);
                    int best = Integer.MAX_VALUE;
                    for (Edge edge : graph.neighbors(from))
                        if (edge.to() == to) best = Math.min(best, edge.weight());
                    check(best != Integer.MAX_VALUE, "Path uses actual edges");
                    sum += best;
                }
                check(sum == expected[target], "Path weight equals distance");
            }
        }
    }

    public static void main(String[] args) {
        DSU dsu = new DSU(5, new Stats());
        check(dsu.union(0, 1), "Union different sets");
        check(dsu.union(1, 2), "Union transitive sets");
        check(!dsu.union(0, 2), "Reject redundant union");
        check(dsu.find(0) == dsu.find(2) && dsu.find(0) != dsu.find(3), "Find roots");

        Graph small = new Graph(4, false);
        small.addEdge(0, 1, 1);
        small.addEdge(1, 2, 2);
        small.addEdge(2, 3, 3);
        small.addEdge(0, 3, 9);
        mst(small, 6, true);
        verifyDistances(small);
        Graph ties = new Graph(4, false);
        ties.addEdge(0, 1, 1);
        ties.addEdge(1, 2, 1);
        ties.addEdge(2, 3, 1);
        ties.addEdge(3, 0, 1);
        mst(ties, 3, true);
        Graph disconnected = new Graph(5, false);
        disconnected.addEdge(0, 1, 2);
        disconnected.addEdge(2, 3, 4);
        mst(disconnected, 6, false);
        verifyDistances(disconnected);
        mst(new Graph(1, false), 0, true);
        verifyDistances(new Graph(1, false));
        Graph singleDirected = new Graph(1, true);
        validOrder(singleDirected);
        verifyScc(singleDirected);
        singleDirected.addEdge(0, 0, 0);
        check(!Connectivity.topological(singleDirected, new Stats()).dag(), "Self-loop cycle");
        verifyScc(singleDirected);

        Graph directed = new Graph(6, true);
        directed.addEdge(0, 1, 0);
        directed.addEdge(1, 2, 2);
        directed.addEdge(2, 0, 3);
        directed.addEdge(2, 3, 1);
        directed.addEdge(3, 4, 2);
        directed.addEdge(4, 3, 2);
        check(!Connectivity.topological(directed, new Stats()).dag(), "Cycle rejected");
        check(Connectivity.kosaraju(directed, new Stats()).size() == 3, "Three SCCs");
        verifyScc(directed);
        verifyDistances(directed);
        Graph negative = new Graph(2, true);
        negative.addEdge(0, 1, -1);
        rejects(() -> Dijkstra.run(negative, 0, new Stats()), "Negative weights rejected");
        rejects(() -> MST.prim(directed, new Stats()), "Directed MST rejected");
        rejects(() -> Connectivity.topological(small, new Stats()), "Undirected topo rejected");

        for (int seed = 0; seed < 20; seed++) {
            Graph undirected = GraphGenerator.generate(10, 30, seed, "undirected");
            mst(undirected, MST.kruskal(undirected, new Stats()).weight(), true);
            verifyDistances(undirected);
            Graph dag = GraphGenerator.generate(10, 30, seed, "dag");
            validOrder(dag);
            verifyScc(dag);
            Graph cyclic = GraphGenerator.generate(10, 30, seed, "cyclic");
            check(!Connectivity.topological(cyclic, new Stats()).dag(), "Generated cycle rejected");
            verifyScc(cyclic);
            verifyDistances(cyclic);
            check(undirected.edges().equals(
                    GraphGenerator.generate(10, 30, seed, "undirected").edges()), "Fixed seed");
        }
        System.out.println("PASS: " + checks + " correctness checks");
    }
}
