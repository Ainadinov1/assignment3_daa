# Assignment 3: Graph Algorithms Analysis

## 1. System & Environment
- **Device:** Apple MacBook Air
- **OS:** macOS
- **Runtime:** OpenJDK 17 (Java 17)
- **Measurement Tool:** System.nanoTime() wall-clock timing excluding compile time, with a warm-up phase of 5 runs.

---

## 2. Algorithm Details & Correctness

### 2.1. Disjoint Set Union (DSU)
- **Steps:** Maintains representative parent pointers and tree sizes. `find` performs two-pass path compression iteratively to prevent stack overflow. `union` merges the smaller component into the larger one (union by size), updating total connected components and the maximum component size.
- **Invariants:**
    - Each set is a rooted tree where the root points to itself (`parent[root] == root`).
    - `size[root]` always reflects the exact number of nodes in that disjoint partition.
- **Complexity:**
    - Time: O(m * alpha(n)) where alpha is the Inverse Ackermann function.
    - Space: O(n) for storing parent and size arrays.
- **Edge Cases:** Isolates, repeated union on already merged vertices, and operations beyond components.

### 2.2. Kruskal's Algorithm (MSF)
- **Steps:** Extracts all undirected edges with original 0-based IDs, sorts them in ascending order of edge weights, and greedily includes edges that connect disjoint components using DSU.
- **Correctness (Cut Property):** At every step, the lowest-weight crossing edge across any cut not yet spanning a cycle is safe to add.
- **Complexity:**
    - Time: O(m log m) dominated by edge sorting, plus O(m * alpha(n)) for DSU queries.
    - Space: O(n + m) for edge index mapping and DSU arrays.
- **Edge Cases:** Handles negative edge weights, parallel edges, self-loops (filtered out by DSU check), and disconnected components (builds minimum spanning forest).

### 2.3. Prim's Algorithm (MSF)
- **Steps:** Constructs an adjacency list containing target nodes, weights, and edge IDs. Iterates through all vertices; if unvisited, starts an expansion loop using a binary min-heap (PriorityQueue).
- **Correctness:** By restarting across all disconnected components, Prim discovers the MSF without leaving isolated subgraphs unvisited.
- **Complexity:**
    - Time: O(m log m) with standard priority queue (edge pushes).
    - Space: O(n + m) for adjacency representations and the priority queue.
- **Edge Cases:** Self-loops are ignored when popped to an already visited vertex; multi-edges with smaller weights naturally get popped first.

### 2.4. Strongly Connected Components (Kosaraju)
- **Steps:**
    1. Forward iterative DFS traversal pushes vertices to an order stack upon post-order completion.
    2. Transposes the directed graph by reversing all edge orientations.
    3. Reverse iterative DFS extracts vertices according to the stack order to assign SCC component IDs.
    4. Condensation graph is constructed by recording unique cross-component edges (`cu != cv`).
- **Complexity:**
    - Time: O(n + m) for two complete DFS traversals and edge reversal.
    - Space: O(n + m) for explicit stacks and graph transposition.
- **Edge Cases:** Deep long chains are executed iteratively via ArrayDeque to eliminate StackOverflowError.

### 2.5. Topological Sort (topo)
- **Steps:** Executes a three-state DFS traversal (State 0: unvisited, State 1: visiting / active stack, State 2: processed). Traversal is driven iteratively using an explicit DFS stack.
- **Cycle Detection:** If an adjacent node is encountered in State 1, a directed cycle exists -> immediately returns `acyclic: false` and empty order `[]`.
- **Complexity:**
    - Time: O(n + m) visiting every node and edge at most once.
    - Space: O(n + m) for state tracking and explicit stack frames.

### 2.6. DAG Shortest Paths (dag)
- **Steps:**
    1. Evaluates cycle presence across the whole graph via topo (identifies cycles even if unreachable from source).
    2. If acyclic, edges are relaxed strictly in topological order starting from source.
    3. Unreachable vertices maintain `distance: null` and `parent_edge: -1`.
- **Complexity:**
    - Time: O(n + m) linear time shortest path relaxation on DAG.
    - Space: O(n + m).

---

## 3. Route Reconstruction Helper Scenarios
The helper function `reconstructRoute(source, target, parentEdge, edges)` provides path recovery:
1. **Reachable Target:** Traceback from target node following parentEdge until source is reached, then reverse. Returns chronological edge IDs.
2. **Source Vertex:** When source == target, returns an empty list [] (0 edges required).
3. **Unreachable Target:** parent_edge[target] == -1 returns null.
4. **Tied / Parallel Edges:** Returns the first relaxed optimal edge ID recorded during topological updates.

---

## 4. Benchmark: Kruskal vs Prim
Evaluated on random connected undirected graphs over 3 runs (medians recorded):

| Graph Benchmark | Nodes (n) | Edges (m) | Kruskal Median | Prim Median |
| :--- | :--- | :--- | :--- | :--- |
| **Small** | 100 | 300 | 0.39 ms | 0.49 ms |
| **Medium** | 2,000 | 10,000 | 8.34 ms | 10.92 ms |
| **Limit / Large** | 10,000 | 250,000 | 132.8 ms | 174.1 ms |

**Observations:** For sparse graphs, Kruskal achieves lower execution overhead due to fast primitive sorting and near constant-time DSU unions, while Prim maintains continuous heap re-insertions.

---

## 5. Sources & AI Assistance
- **Algorithms Reference:** Cormen, Leiserson, Rivest, Stein (CLRS), Introduction to Algorithms (Chapters on MST, SCC, and DAG shortest paths).
- **AI Tool Usage:** Google Gemini was utilized for architectural guidance, debugging JSON data models, and drafting analysis summaries in accordance with course guidelines.