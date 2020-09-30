package datastructures.concrete;
import datastructures.interfaces.ISet;
import datastructures.interfaces.IPriorityQueue;
import datastructures.interfaces.IDisjointSet;
import datastructures.interfaces.IDictionary;
import datastructures.interfaces.IList;
import datastructures.interfaces.IEdge;

import datastructures.concrete.dictionaries.ChainedHashDictionary;
import datastructures.concrete.dictionaries.KVPair;

import misc.Sorter;
import misc.exceptions.NoPathExistsException;




/**
 * Represents an undirected, weighted graph, possibly containing self-loops, parallel edges,
 * and unconnected components.
 *
 * Note: This class is not meant to be a full-featured way of representing a graph.
 * We stick with supporting just a few, core set of operations needed for the
 * remainder of the project.
 */
public class Graph<V, E extends IEdge<V> & Comparable<E>> {
    private IDisjointSet<V> vertices;
    private IDictionary<V, ISet<E>> graph;
    private IDictionary<V, ComparableVertex<V, E>> vertexToComp;
    private IList<E> edges;
    private int graphSize;
    
    // This class uses two generic parameters: V and E.
    //
    // - 'V' is the type of the vertices in the graph. The vertices can be
    //   any type the client wants -- there are no restrictions.
    //
    // - 'E' is the type of the edges in the graph. We've constrained Graph
    //   so that E *must* always be an instance of IEdge<V> AND Comparable<E>.
    //
    //   What this means is that if you have an object of type E, you can use
    //   any of the methods from both the IEdge interface and from the Comparable
    //   interface

    /**
     * Constructs a new graph based on the given vertices and edges.
     *
     * Note that each edge in 'edges' represents a unique edge. For example, if 'edges'
     * contains an entry for '(A,B)' and for '(B,A)', that means there are two parallel
     * edges between vertex 'A' and vertex 'B'.
     *
     * @throws IllegalArgumentException if any edges have a negative weight
     * @throws IllegalArgumentException if any edges connect to a vertex not present in 'vertices'
     * @throws IllegalArgumentException if 'vertices' or 'edges' are null or contain null
     * @throws IllegalArgumentException if 'vertices' contains duplicates
     */
    
    public Graph(IList<V> vertices, IList<E> edges) {
        this.vertexToComp = new ChainedHashDictionary<>();
        this.vertices = new ArrayDisjointSet<>();
        this.graph = new ChainedHashDictionary<>();
        this.graphSize = 0;
        this.edges = edges;
        if (vertices == null || edges == null) {
            throw new IllegalArgumentException();
        }
        ISet<V> duplicates = new ChainedHashSet<>();
        for (V vertex: vertices) {
            if (vertex == null) {
                throw new IllegalArgumentException();
            }
            if (!duplicates.contains(vertex)) {
                duplicates.add(vertex);
                this.vertices.makeSet(vertex);
                graph.put(vertex, new ChainedHashSet<>());
                graphSize++;
            } else {
                throw new IllegalArgumentException();
            }
        }
        for (E edge: edges) {
            if (edge.getWeight() < 0 || !vertices.contains(edge.getVertex1())
                || !vertices.contains(edge.getVertex2()) || edge == null) {
                throw new IllegalArgumentException();
            }
            V vertex1 = edge.getVertex1();
            graph.get(vertex1).add(edge);
            V vertex2 = edge.getVertex2();
            graph.get(vertex2).add(edge);
        }
    }

    /**
     * Sometimes, we store vertices and edges as sets instead of lists, so we
     * provide this extra constructor to make converting between the two more
     * convenient.
     *
     * @throws IllegalArgumentException if any of the edges have a negative weight
     * @throws IllegalArgumentException if one of the edges connects to a vertex not
     *                                  present in the 'vertices' list
     * @throws IllegalArgumentException if vertices or edges are null or contain null
     */
    public Graph(ISet<V> vertices, ISet<E> edges) {
        // You do not need to modify this method.
        this(setToList(vertices), setToList(edges));
    }

    // You shouldn't need to call this helper method -- it only needs to be used
    // in the constructor above.
    private static <T> IList<T> setToList(ISet<T> set) {
        if (set == null) {
            throw new IllegalArgumentException();
        }
        IList<T> output = new DoubleLinkedList<>();
        for (T item : set) {
            output.add(item);
        }
        return output;
    }

    /**
     * Returns the number of vertices contained within this graph.
     */
    public int numVertices() {
        return graphSize;
    }

    /**
     * Returns the number of edges contained within this graph.
     */
    // efficiency
    public int numEdges() {
       return this.edges.size();
    }

    /**
     * Returns the set of all edges that make up the minimum spanning tree of
     * this graph.
     *
     * If there exists multiple valid MSTs, return any one of them.
     *
     * Precondition: the graph does not contain any unconnected components.
     */
    public ISet<E> findMinimumSpanningTree() {
        IList<E> sortedE = Sorter.topKSort(this.numEdges(), this.edges);
        ISet<E> result = new ChainedHashSet<>();
        for (E edge: sortedE) {
            V vertex1 = edge.getVertex1();
            V vertex2 = edge.getVertex2();
            if (vertices.findSet(vertex1) != vertices.findSet(vertex2)) {
                vertices.union(vertex1, vertex2);
                result.add(edge);
            }
        }
        return result;
    }

    /**
     * Returns the edges that make up the shortest path from the start
     * to the end.
     *
     * The first edge in the output list should be the edge leading out
     * of the starting node; the last edge in the output list should be
     * the edge connecting to the end node.
     *
     * Return an empty list if the start and end vertices are the same.
     *
     * @throws NoPathExistsException  if there does not exist a path from the start to the end
     * @throws IllegalArgumentException if start or end is null or not in the graph
     */
    public IList<E> findShortestPathBetween(V start, V end) {
        if (start == null || end == null || !graph.containsKey(start) || !graph.containsKey(end)) {
            throw new IllegalArgumentException();
        }
        if (start == end) {
            return new DoubleLinkedList<>();
        }
        ISet<V> processed = new ChainedHashSet<>();
        // initialize MPQ and set every distance to infinity
        IPriorityQueue<ComparableVertex<V, E>> minPQ = new ArrayHeap<>();
        for (KVPair<V, ISet<E>> pair: graph) {
            vertexToComp.put(pair.getKey(), new ComparableVertex<>(pair.getKey(), Double.POSITIVE_INFINITY));
        }
        // initialize the source and put it into MPQ
        ComparableVertex<V, E> initial = new ComparableVertex<>(start, 0.0);
        minPQ.add(initial);
        // update the starting vertex in the dictionary
        vertexToComp.put(start, initial);
        processed.add(start);
        while (!minPQ.isEmpty()) {
            ComparableVertex<V, E> startV = minPQ.removeMin(); // starting vertex
            V currV = startV.vertex;
            processed.add(currV);
            ISet<E> currEdges = graph.get(currV); // edges of the current vertex
            for (E edge: currEdges) {
                double weight = edge.getWeight();
                V neighbor = edge.getOtherVertex(currV); // for that edge, the neighbor of the startV
                ComparableVertex<V, E> oldEndV = vertexToComp.get(neighbor); // get its object
                double oldDist = oldEndV.dist;
                double newDist = startV.dist + weight;
                if (newDist < oldDist && !processed.contains(neighbor)) {
                    ComparableVertex<V, E> newEndV = new ComparableVertex<>(neighbor, newDist, currV, edge);
                    vertexToComp.put(neighbor, newEndV);
                    if (oldDist == Double.POSITIVE_INFINITY) {
                        minPQ.add(newEndV);
                    } else {
                        minPQ.replace(oldEndV, newEndV);
                    }
                }
            }
        }
        IList<E> path = new DoubleLinkedList<>();
        ComparableVertex<V, E> endV = vertexToComp.get(end);
        V pred = endV.pred;
        if (pred == null) {
            throw new NoPathExistsException();
        }
        path.add(endV.edge);
        while (pred != start) {
            ComparableVertex<V, E> predecessor = vertexToComp.get(pred);
            path.insert(0, predecessor.edge);
            pred = predecessor.pred;
        }
        return path;
    }

    private static class ComparableVertex<V, E>
            implements Comparable<ComparableVertex<V, E>> {
        private final V vertex;
        private final double dist;
        private final V pred;
        private final E edge;

        public ComparableVertex(V vertex, double dist) {
            this(vertex, dist, null, null);
        }

        public ComparableVertex(V vertex, double dist, V pred, E edge) {
            this.vertex = vertex;
            this.dist = dist;
            this.pred = pred;
            this.edge = edge;
        }

        @Override
        public int compareTo(ComparableVertex<V, E> other) {
            if (this.dist < other.dist) {
                return -1;
            } else if (this.dist > other.dist) {
                return 1;
            } else {
                return 0;
            }
        }
    }


}
