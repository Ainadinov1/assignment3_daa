

import java.util.*;

public class Algorithms {

    // 1. DSU (Union-Find)
    @SuppressWarnings("unchecked")
    public static Map<String, Object> dsu(Map<String, Object> request) {
        int n = ((Number) request.get("n")).intValue();
        List<List<Number>> rawOps = (List<List<Number>>) request.get("operations");

        int[] parent = new int[n];
        int[] size = new int[n];
        for (int i = 0; i < n; i++) {
            parent[i] = i;
            size[i] = 1;
        }

        int components = n;
        int largest = n > 0 ? 1 : 0;
        List<List<Integer>> states = new ArrayList<>();

        if (rawOps != null) {
            for (List<Number> op : rawOps) {
                int u = op.get(0).intValue();
                int v = op.get(1).intValue();

                int rootU = find(parent, u);
                int rootV = find(parent, v);

                if (rootU != rootV) {
                    if (size[rootU] < size[rootV]) {
                        int tmp = rootU; rootU = rootV; rootV = tmp;
                    }
                    parent[rootV] = rootU;
                    size[rootU] += size[rootV];
                    if (size[rootU] > largest) {
                        largest = size[rootU];
                    }
                    components--;
                }
                states.add(Arrays.asList(components, largest));
            }
        }

        Map<String, Object> res = new HashMap<>();
        res.put("states", states);
        return res;
    }

    private static int find(int[] parent, int i) {
        int root = i;
        while (root != parent[root]) {
            root = parent[root];
        }
        int curr = i;
        while (curr != root) {
            int nxt = parent[curr];
            parent[curr] = root;
            curr = nxt;
        }
        return root;
    }

    // 2. Kruskal (Minimum Spanning Forest)
    @SuppressWarnings("unchecked")
    public static Map<String, Object> kruskal(Map<String, Object> request) {
        int n = ((Number) request.get("n")).intValue();
        List<List<Number>> rawEdges = (List<List<Number>>) request.get("edges");

        List<int[]> indexedEdges = new ArrayList<>();
        if (rawEdges != null) {
            for (int i = 0; i < rawEdges.size(); i++) {
                List<Number> e = rawEdges.get(i);
                int u = e.get(0).intValue();
                int v = e.get(1).intValue();
                indexedEdges.add(new int[]{u, v, i});
            }

            indexedEdges.sort((a, b) -> Long.compare(
                    rawEdges.get(a[2]).get(2).longValue(),
                    rawEdges.get(b[2]).get(2).longValue()
            ));
        }

        int[] parent = new int[n];
        int[] rank = new int[n];
        for (int i = 0; i < n; i++) parent[i] = i;

        long totalCost = 0;
        List<Integer> edgeIds = new ArrayList<>();
        int components = n;

        for (int[] edge : indexedEdges) {
            int u = edge[0];
            int v = edge[1];
            int id = edge[2];
            long w = rawEdges.get(id).get(2).longValue();

            int rootU = find(parent, u);
            int rootV = find(parent, v);

            if (rootU != rootV) {
                if (rank[rootU] < rank[rootV]) {
                    parent[rootU] = rootV;
                } else if (rank[rootU] > rank[rootV]) {
                    parent[rootV] = rootU;
                } else {
                    parent[rootV] = rootU;
                    rank[rootU]++;
                }
                totalCost += w;
                edgeIds.add(id);
                components--;
            }
        }

        Map<String, Object> res = new HashMap<>();
        res.put("cost", totalCost);
        res.put("edge_ids", edgeIds);
        res.put("components", components);
        return res;
    }

    // 3. Prim (Minimum Spanning Forest with restarts)
    @SuppressWarnings("unchecked")
    public static Map<String, Object> prim(Map<String, Object> request) {
        int n = ((Number) request.get("n")).intValue();
        List<List<Number>> rawEdges = (List<List<Number>>) request.get("edges");

        List<List<long[]>> adj = new ArrayList<>();
        for (int i = 0; i < n; i++) adj.add(new ArrayList<>());

        if (rawEdges != null) {
            for (int i = 0; i < rawEdges.size(); i++) {
                int u = rawEdges.get(i).get(0).intValue();
                int v = rawEdges.get(i).get(1).intValue();
                long w = rawEdges.get(i).get(2).longValue();
                adj.get(u).add(new long[]{v, w, i});
                adj.get(v).add(new long[]{u, w, i});
            }
        }

        boolean[] visited = new boolean[n];
        long totalCost = 0;
        List<Integer> edgeIds = new ArrayList<>();
        int components = 0;

        for (int i = 0; i < n; i++) {
            if (visited[i]) continue;
            components++;
            visited[i] = true;

            PriorityQueue<long[]> pq = new PriorityQueue<>(Comparator.comparingLong(a -> a[1]));
            for (long[] edge : adj.get(i)) {
                pq.offer(new long[]{edge[0], edge[1], edge[2]});
            }

            while (!pq.isEmpty()) {
                long[] curr = pq.poll();
                int to = (int) curr[0];
                long w = curr[1];
                int id = (int) curr[2];

                if (visited[to]) continue;
                visited[to] = true;
                totalCost += w;
                edgeIds.add(id);

                for (long[] nextEdge : adj.get(to)) {
                    if (!visited[(int) nextEdge[0]]) {
                        pq.offer(new long[]{nextEdge[0], nextEdge[1], nextEdge[2]});
                    }
                }
            }
        }

        Map<String, Object> res = new HashMap<>();
        res.put("cost", totalCost);
        res.put("edge_ids", edgeIds);
        res.put("components", components);
        return res;
    }

    // 4. SCC (Kosaraju: 2 DFS passes + transpose)
    @SuppressWarnings("unchecked")
    public static Map<String, Object> scc(Map<String, Object> request) {
        int n = ((Number) request.get("n")).intValue();
        List<List<Number>> rawEdges = (List<List<Number>>) request.get("edges");

        List<List<Integer>> graph = new ArrayList<>();
        List<List<Integer>> transposed = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            graph.add(new ArrayList<>());
            transposed.add(new ArrayList<>());
        }

        if (rawEdges != null) {
            for (List<Number> e : rawEdges) {
                int u = e.get(0).intValue();
                int v = e.get(1).intValue();
                graph.get(u).add(v);
                transposed.get(v).add(u);
            }
        }

        boolean[] visited = new boolean[n];
        List<Integer> order = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            if (!visited[i]) {
                Deque<Integer> stack = new ArrayDeque<>();
                Deque<Integer> postOrder = new ArrayDeque<>();
                stack.push(i);
                visited[i] = true;

                while (!stack.isEmpty()) {
                    int curr = stack.pop();
                    postOrder.push(curr);
                    for (int neighbor : graph.get(curr)) {
                        if (!visited[neighbor]) {
                            visited[neighbor] = true;
                            stack.push(neighbor);
                        }
                    }
                }
                while (!postOrder.isEmpty()) {
                    order.add(postOrder.pop());
                }
            }
        }

        Arrays.fill(visited, false);
        int[] component = new int[n];
        int compCount = 0;

        for (int i = order.size() - 1; i >= 0; i--) {
            int v = order.get(i);
            if (!visited[v]) {
                Deque<Integer> stack = new ArrayDeque<>();
                stack.push(v);
                visited[v] = true;

                while (!stack.isEmpty()) {
                    int curr = stack.pop();
                    component[curr] = compCount;
                    for (int neighbor : transposed.get(curr)) {
                        if (!visited[neighbor]) {
                            visited[neighbor] = true;
                            stack.push(neighbor);
                        }
                    }
                }
                compCount++;
            }
        }

        Set<String> seenEdges = new HashSet<>();
        List<List<Integer>> condensation = new ArrayList<>();

        if (rawEdges != null) {
            for (List<Number> e : rawEdges) {
                int cu = component[e.get(0).intValue()];
                int cv = component[e.get(1).intValue()];
                if (cu != cv) {
                    String key = cu + "->" + cv;
                    if (!seenEdges.contains(key)) {
                        seenEdges.add(key);
                        condensation.add(Arrays.asList(cu, cv));
                    }
                }
            }
        }

        List<Integer> compList = new ArrayList<>();
        for (int c : component) compList.add(c);

        Map<String, Object> res = new HashMap<>();
        res.put("component", compList);
        res.put("condensation", condensation);
        return res;
    }

    // 5. Topo Sort (Three-state iterative DFS)
    @SuppressWarnings("unchecked")
    public static Map<String, Object> topo(Map<String, Object> request) {
        int n = ((Number) request.get("n")).intValue();
        List<List<Number>> rawEdges = (List<List<Number>>) request.get("edges");

        List<List<Integer>> adj = new ArrayList<>();
        for (int i = 0; i < n; i++) adj.add(new ArrayList<>());
        if (rawEdges != null) {
            for (List<Number> e : rawEdges) {
                adj.get(e.get(0).intValue()).add(e.get(1).intValue());
            }
        }

        int[] state = new int[n]; // 0: unvisited, 1: visiting, 2: visited
        List<Integer> order = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            if (state[i] == 0) {
                Deque<Integer> stack = new ArrayDeque<>();
                stack.push(i);

                while (!stack.isEmpty()) {
                    int curr = stack.peek();
                    if (state[curr] == 0) {
                        state[curr] = 1;
                        for (int next : adj.get(curr)) {
                            if (state[next] == 1) {
                                Map<String, Object> cycle = new HashMap<>();
                                cycle.put("acyclic", false);
                                cycle.put("order", Collections.emptyList());
                                return cycle;
                            }
                            if (state[next] == 0) {
                                stack.push(next);
                            }
                        }
                    } else {
                        stack.pop();
                        if (state[curr] == 1) {
                            state[curr] = 2;
                            order.add(curr);
                        }
                    }
                }
            }
        }

        Collections.reverse(order);
        Map<String, Object> res = new HashMap<>();
        res.put("acyclic", true);
        res.put("order", order);
        return res;
    }

    // 6. DAG Shortest Paths (Cycle check -> topological relaxation)
    @SuppressWarnings("unchecked")
    public static Map<String, Object> dag(Map<String, Object> request) {
        int n = ((Number) request.get("n")).intValue();
        int source = ((Number) request.get("source")).intValue();
        List<List<Number>> rawEdges = (List<List<Number>>) request.get("edges");

        List<List<Number>> topoEdges = new ArrayList<>();
        if (rawEdges != null) {
            for (List<Number> e : rawEdges) {
                topoEdges.add(Arrays.asList(e.get(0).intValue(), e.get(1).intValue()));
            }
        }

        Map<String, Object> topoRequest = new HashMap<>();
        topoRequest.put("n", n);
        topoRequest.put("edges", topoEdges);

        Map<String, Object> topoRes = topo(topoRequest);
        if (!(boolean) topoRes.get("acyclic")) {
            Map<String, Object> cycle = new HashMap<>();
            cycle.put("acyclic", false);
            cycle.put("distance", Collections.emptyList());
            cycle.put("parent_edge", Collections.emptyList());
            return cycle;
        }

        List<Integer> topoOrder = (List<Integer>) topoRes.get("order");

        List<List<long[]>> adj = new ArrayList<>();
        for (int i = 0; i < n; i++) adj.add(new ArrayList<>());
        if (rawEdges != null) {
            for (int i = 0; i < rawEdges.size(); i++) {
                int u = rawEdges.get(i).get(0).intValue();
                int v = rawEdges.get(i).get(1).intValue();
                long w = rawEdges.get(i).get(2).longValue();
                adj.get(u).add(new long[]{v, w, i});
            }
        }

        Long[] dist = new Long[n];
        int[] parentEdge = new int[n];
        Arrays.fill(parentEdge, -1);
        dist[source] = 0L;

        for (int u : topoOrder) {
            if (dist[u] == null) continue;
            for (long[] edge : adj.get(u)) {
                int v = (int) edge[0];
                long w = edge[1];
                int id = (int) edge[2];

                if (dist[v] == null || dist[u] + w < dist[v]) {
                    dist[v] = dist[u] + w;
                    parentEdge[v] = id;
                }
            }
        }

        List<Object> distanceList = new ArrayList<>();
        List<Integer> parentList = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            distanceList.add(dist[i]);
            parentList.add(parentEdge[i]);
        }

        Map<String, Object> res = new HashMap<>();
        res.put("acyclic", true);
        res.put("distance", distanceList);
        res.put("parent_edge", parentList);
        return res;
    }

    // Route reconstruction helper
    public static List<Integer> reconstructRoute(int source, int target, int[] parentEdge, List<List<Number>> edges) {
        if (source == target) return Collections.emptyList();
        if (parentEdge[target] == -1) return null;

        List<Integer> path = new ArrayList<>();
        int curr = target;
        while (curr != source) {
            int edgeId = parentEdge[curr];
            if (edgeId == -1) return null;
            path.add(edgeId);
            curr = edges.get(edgeId).get(0).intValue();
        }
        Collections.reverse(path);
        return path;
    }
}