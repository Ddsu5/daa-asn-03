import graph.*;
import mst.MST;
import connectivity.Connectivity;
import shortestpath.Dijkstra;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.function.Function;

public class Benchmark {
    private static volatile Object sink;

    private static void measure(PrintWriter raw, PrintWriter summary, String name,
                                Graph graph, Function<Stats, Object> algorithm) {
        for (int i = 0; i < 10; i++) sink = algorithm.apply(new Stats());
        long total = 0;
        long[] counts = new long[6];
        for (int run = 1; run <= 5; run++) {
            Stats stats = new Stats();
            long start = System.nanoTime();
            Object result = algorithm.apply(stats);
            long elapsed = System.nanoTime() - start;
            sink = result;
            total += elapsed;
            long[] current = {stats.edges, stats.comparisons, stats.finds,
                    stats.unions, stats.parentSteps, stats.queueOps};
            raw.printf(Locale.ROOT, "%s,%d,%d,%d,%d", name, graph.size(),
                    graph.edges().size(), run, elapsed);
            for (int i = 0; i < counts.length; i++) {
                counts[i] += current[i];
                raw.print("," + current[i]);
            }
            raw.println();
        }
        summary.printf(Locale.ROOT, "%s,%d,%d,%.6f", name, graph.size(),
                graph.edges().size(), total / 5.0 / 1_000_000);
        for (long count : counts) summary.printf(Locale.ROOT, ",%.1f", count / 5.0);
        summary.println();
    }

    public static void main(String[] args) throws IOException {
        Path directory = Path.of("results/tables");
        Files.createDirectories(directory);
        try (PrintWriter raw = new PrintWriter(Files.newBufferedWriter(directory.resolve("raw.csv")));
             PrintWriter summary = new PrintWriter(Files.newBufferedWriter(directory.resolve("summary.csv")));
             PrintWriter checks = new PrintWriter(Files.newBufferedWriter(directory.resolve("graph_checks.csv")))) {
            String counters = ",edge_examinations,comparisons,find_calls,union_calls,parent_steps,queue_operations";
            raw.println("algorithm,V,E,run,nanoseconds" + counters);
            summary.println("algorithm,V,E,average_ms" + counters);
            checks.println("V,E,kruskal_weight,prim_weight,connected,dag_accepted,cycle_rejected,scc_count");
            int[][] sizes = {{10, 30}, {100, 500}, {1000, 5000}};
            for (int[] size : sizes) {
                int v = size[0], e = size[1];
                Graph roads = GraphGenerator.generate(v, e, 2530L + v, "undirected");
                Graph dag = GraphGenerator.generate(v, e, 2530L + v, "dag");
                Graph cyclic = GraphGenerator.generate(v, e, 2530L + v, "cyclic");
                var k = MST.kruskal(roads, new Stats());
                var p = MST.prim(roads, new Stats());
                boolean dagOk = Connectivity.topological(dag, new Stats()).dag();
                boolean cycleRejected = !Connectivity.topological(cyclic, new Stats()).dag();
                int sccCount = Connectivity.kosaraju(cyclic, new Stats()).size();
                if (k.weight() != p.weight() || !k.connected() || !p.connected()
                        || !dagOk || !cycleRejected || sccCount != 1)
                    throw new AssertionError("Benchmark graph validation failed");
                checks.printf("%d,%d,%d,%d,true,%b,%b,%d%n",
                        v, e, k.weight(), p.weight(), dagOk, cycleRejected, sccCount);
                measure(raw, summary, "Kruskal", roads, s -> MST.kruskal(roads, s));
                measure(raw, summary, "Prim", roads, s -> MST.prim(roads, s));
                measure(raw, summary, "SCC", cyclic, s -> Connectivity.kosaraju(cyclic, s));
                measure(raw, summary, "Topo-DAG", dag, s -> Connectivity.topological(dag, s));
                measure(raw, summary, "Topo-cycle", cyclic, s -> Connectivity.topological(cyclic, s));
                measure(raw, summary, "Dijkstra", roads, s -> Dijkstra.run(roads, 0, s));
            }
        }
        Files.writeString(directory.resolve("environment.txt"),
                "Java: " + System.getProperty("java.version") + "\nVM: "
                + System.getProperty("java.vm.name") + "\nOS: " + System.getProperty("os.name")
                + " " + System.getProperty("os.arch") + "\nProcessors available: "
                + Runtime.getRuntime().availableProcessors() + "\nSeed: 2530 + V\nWarmups: 10\nMeasured runs: 5\n");
        System.out.println("Benchmark complete: results/tables/");
    }
}
