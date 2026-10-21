import graph.*;
import mst.MST;
import connectivity.Connectivity;
import shortestpath.Dijkstra;

public class Main {
    public static void main(String[] args) {
        Graph roads = new Graph(5, false);
        roads.addEdge(0, 1, 4);
        roads.addEdge(0, 2, 1);
        roads.addEdge(2, 1, 2);
        roads.addEdge(1, 3, 1);
        roads.addEdge(2, 3, 5);
        roads.addEdge(3, 4, 3);
        System.out.println("City districts: 0, 1, 2, 3, 4");
        System.out.println("Kruskal: " + MST.kruskal(roads, new Stats()));
        System.out.println("Prim:    " + MST.prim(roads, new Stats()));
        int source = args.length == 0 ? 0 : Integer.parseInt(args[0]);
        Dijkstra.Result routes = Dijkstra.run(roads, source, new Stats());
        for (int v = 0; v < roads.size(); v++) {
            String distance = routes.distances()[v] == Long.MAX_VALUE
                    ? "unreachable" : Long.toString(routes.distances()[v]);
            System.out.println(source + " -> " + v + ": distance=" + distance
                    + ", path=" + routes.pathTo(v));
        }
        Graph services = new Graph(5, true);
        services.addEdge(0, 1, 1);
        services.addEdge(0, 2, 1);
        services.addEdge(1, 3, 1);
        services.addEdge(2, 3, 1);
        services.addEdge(3, 4, 1);
        System.out.println("DAG: " + Connectivity.topological(services, new Stats()));
        services.addEdge(4, 1, 1);
        System.out.println("After adding 4 -> 1: " + Connectivity.topological(services, new Stats()));
        System.out.println("SCCs: " + Connectivity.kosaraju(services, new Stats()));
    }
}
