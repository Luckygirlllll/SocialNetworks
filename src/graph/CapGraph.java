/**
 * 
 */
package graph;
/**
 * @author Iryna Kanivets.
 * 
 * implement your Graph in a class
 * named CapGraph.  Here is the stub file.
 *
 */
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

public class CapGraph implements Graph {
	
	private String name;
	private Map<Integer,Vertex> vertices;
	private List<Graph> SCCList;
	
	public CapGraph() {
		
		this("Default Graph Name");
	}
	
	public CapGraph(String name) {
		
		this.name = name;
		this.vertices = new HashMap<Integer,Vertex>();
		// might be inefficient because list will keep doubling
		this.SCCList = new ArrayList<Graph>();
	}

	/** Add a vertex to the graph.
	 * 
	 * @see graph.Graph#addVertex(int)
	 * 
	 * @param num is the numerical id of the vertex to add
	 */
	@Override 
	public void addVertex(int num) {
		
		Vertex vertex = new Vertex(num);
		vertices.put(num,vertex);
	}

	/** Add a directed edge to the graph.
	 * 
	 * @see graph.Graph#addEdge(int, int)
	 * 
	 * @param from is the id of the edge's starting vertex
	 * @param to is the id of the edge's ending vertex
	 */
	@Override
	public void addEdge(int from, int to) {
		
		Vertex fromVertex = vertices.get(from);
		Vertex toVertex = vertices.get(to);
		
		fromVertex.createEdge(toVertex);
	}

	/** Construct the egonet for a particular vertex.
	 * 
	 * An egonet is a subgraph that includes 1) the vertex center c,
	 * 2) all of vertices v that are directly connected by an edge 
	 * from c to v, 3) all of the edges that connect c to each v,
	 * and 4) and all of the edges between each v.
	 * 
	 * The returned graph does not share any objects with the original graph.
	 * 
	 * @param center is the vertex at the center of the egonet
	 * 
	 * @return the egonet centered at center, including center
	 * 
	 * @see graph.Graph#getEgonet(int)
	 */
	@Override
	public Graph getEgonet(int center) {
		
		Graph egonet = new CapGraph("Egonet for vertex " + center + 
									" within " + name); 
		
		Vertex centVertInParent = vertices.get(center);
		List<Vertex> centOutVertsInParent = centVertInParent.getOutEdges();
		
		// add the center to the egonet
		egonet.addVertex(center);
		
		// create map here or we'll have an inner loop iterating over all
		// of center's adjacency list for each of center's out verts
		Set<Vertex> centOutVertsInParentSet = 
				new HashSet<Vertex>(centOutVertsInParent.size()*2,1);
		
		for (Vertex outVertex : centOutVertsInParent) {
			
			centOutVertsInParentSet.add(outVertex);
			// add the out vertex and the edge between it and center
			egonet.addVertex(outVertex.getID());
			egonet.addEdge(center, outVertex.getID());
		}
		
		for (Vertex outVertex : centOutVertsInParent) {
			
			int outVertexID = outVertex.getID();
			
			List<Vertex> outVertOutVertsInParent = outVertex.getOutEdges();
			
			for (Vertex outVertOutVert : outVertOutVertsInParent) {
				
				// add edges between out verts if center is connected to both
				// need to use parent adjacency set because
				// we created new verts for the egonet
				if (centOutVertsInParentSet.contains(outVertOutVert)) {
					
					egonet.addEdge(outVertexID, outVertOutVert.getID());
				}
			}
		}

		return egonet;
	}

	/** Find all strongly connected components (SCCs) in a directed graph.
	 * 
	 * The returned graph(s) do not share any objects with the original graph.
	 * 
	 * @return a list of subgraphs that comprise the strongly connected components
	 * of this graph.
	 * 
	 * @see graph.Graph#getSCCs()
	 */
	@Override
	public List<Graph> getSCCs() {

		Stack<Integer> vertexIDStack = new Stack<Integer>();
		
		for (int vertexID : vertices.keySet()) {
			
			vertexIDStack.push(vertexID);
		}
		
		Stack<Integer> finishOrder = allDFS(this, vertexIDStack, false);
		CapGraph thisTranspose = getTranspose();
		// don't need the finishing order after second pass
		allDFS(thisTranspose, finishOrder, true);
		
		return SCCList;
	}
	
	/** Use depth-first search (DFS) to discover all vertices and all strongly
	 * connected components (SCCs) in a graph.
	 * 
	 * Returns vertices in a stack with later finishing DFS times on top to
	 * first finishing DFS times on bottom.  A vertex is "finished" with 
	 * DFS when DFS has discovered everything there is to discover from that
	 * vertex.
	 * 
	 * @param graph is the graph in which to do DFS and uncover SCCs.  If
	 *   secondPass is true, this should be a transpose of the original graph.
	 * @param verticesToVisit is the (possibly ordered) list of all vertices to
	 *   visit.  If secondPass is true, the stack should be ordered as if it were
	 *   given by the return value of this method on the first pass.
	 * @param secondPass is a boolean that indicates whether the graph is the
	 *   transpose of the graph in which we want to discover SCCs and whether
	 *   verticesToVisit is ordered according to the ordering mentioned above.
	 * @return 
	 */
	public Stack<Integer> allDFS(CapGraph graph, Stack<Integer> verticesToVisit,
								 boolean secondPass) {
		
		Stack<Integer> finished = new Stack<Integer>();
		Set<Integer> visited = new HashSet<Integer>(graph.vertices.size()*2,1);
		
		while (!verticesToVisit.isEmpty()) {
			
			int vertexToVisit = verticesToVisit.pop();
			
			if (!visited.contains(vertexToVisit)) {

				CapGraph SCC = null;
				
				if (secondPass) {
					// if second pass, need to create an SCC
					// the SCC will include all vertexes reachable
					// from this vertex
					// TODO: copy other info (e.g. vertex name)
					SCC = new CapGraph("SCC with Parent '" + name + "' and " +
									   "Root " + vertexToVisit);
					SCC.addVertex(vertexToVisit);
				}

				singleDFS(graph, vertexToVisit, vertexToVisit, 
						  visited, finished, secondPass, SCC);
				
				if (secondPass) {
					
					SCCList.add(SCC);
				}
			}
		}

		return finished;
	}
	
	/** Do a depth-first search as a helper method for discovering SCCs.
	 * 
	 * Do a DFS in a directed graph from a particular vertex as part of 
	 * computing either the "finishing order" of all vertices in the graph 
	 * or assigning vertices to SCCs.
	 * 
	 * If second pass is false, this method is useful because it populates
	 * the "finished" stack passed to it, which gives the ordering that 
	 * should be used with the given graph's transpose to discover SCCs.
	 * 
	 * If secondPass is true, this method will populate the graph's
	 * SCC list.
	 * 
	 * @param graph is the graph in which to do the DFS.
	 * @param vertexID is the vertex from which to do the DFS.
	 * @param root is the root of the current SCC.
	 * @param visited is the set of vertices that have been discovered
	 *   by DFS so far.
	 * @param finished is the list of vertices from which DFS has already
	 *   discovered all vertices there are to discover.
	 * @param secondPass is a boolean that indicates whether the correct
	 *   finishing order has already been calculated and whether the SCC
	 *   list should be populated.
	 * @param SCC is the current SCC (this should be be null if secondPass
	 *   is false).
	 */
	public void singleDFS(CapGraph graph, int vertexID, int root,
						  Set<Integer> visited, Stack<Integer> finished,
						  boolean secondPass, CapGraph SCC) {
		
		visited.add(vertexID);
		
		Vertex vertex = graph.vertices.get(vertexID);
		
		if (secondPass && !SCC.getVertices().keySet().contains(vertexID)) {
			// TODO: copy other info (e.g. vertex name)
			SCC.addVertex(vertexID);
		}
		
		for (Vertex neighbor : vertex.getOutEdges()) {
			
			int neighborID = neighbor.getID();
			
			if (secondPass) {
				// TODO: copy other info (e.g. vertex name, edge weights)
				// if we haven't already visited it and
				// it isn't already in this SCC
				if (!visited.contains(neighborID) &&
					!SCC.getVertices().keySet().contains(neighborID)) {

					SCC.addVertex(neighborID);
				}
				
				// if we added the neighbor to the SCC, add the edge
				if (SCC.getVertices().keySet().contains(neighborID)) {
					SCC.addEdge(vertexID, neighborID);
				}
			}
			
			if (!visited.contains(neighborID)) {
				
				singleDFS(graph, neighborID, root, visited, finished,
						  secondPass, SCC);
			}
		}
		
		finished.push(vertexID);
	}
	
	/** Reverse the edges of this graph.
	 * 
	 * Returns a new graph.
	 * 
	 * @param graph the graph to be transposed
	 * @return a new CapGraph with all original graph edges reversed.
	 */
	public CapGraph getTranspose() {
		
		CapGraph transposeGraph = new CapGraph(name + " (Transpose)");
		
		Map<Integer,Vertex> transposeVertices = transposeGraph.getVertices();
		
		for (int vertexID : this.vertices.keySet()) {
			
			Vertex vertex = vertices.get(vertexID);
			
			if (!transposeVertices.keySet().contains(vertexID)) {
				
				transposeGraph.addVertex(vertexID);
			}
			
			List<Vertex> oldOutEdges = vertex.getOutEdges();
			
			// adjacency matrix representation may be useful
			// to avoid linear inner loop
			for (Vertex oldOutVert : oldOutEdges) {
				
				int oldOutVertID = oldOutVert.getID();
				
				if (!transposeVertices.keySet().contains(oldOutVertID)) {
					
					transposeGraph.addVertex(oldOutVertID);
				}
				
				transposeGraph.addEdge(oldOutVertID, vertexID);
			}
		}
		
		return transposeGraph;
	}
	
	public Map<Integer,Vertex> getVertices() {
		
		return vertices;
	}

	/** Return version of the map readable by UCSD auto-grader.
	 * 
	 * Returns a HashMap where the keys in the HashMap are all the vertices 
	 * in the graph, and the values are the Set of vertices that are reachable 
	 * from the vertex key via a directed edge. 
	 * 
	 * The returned representation ignores edge weights and multi-edges.
	 * 
	 * @see graph.Graph#exportGraph()
	 */
	@Override
	public HashMap<Integer, HashSet<Integer>> exportGraph() {
		
		HashMap<Integer,HashSet<Integer>> exportedGraph = 
				new HashMap<Integer,HashSet<Integer>>(vertices.size()*2,1);
		
		for (int vertexID : vertices.keySet()) {
			
			Vertex vertex = vertices.get(vertexID);
			
			List<Vertex> outVertices = vertex.getOutEdges();
			HashSet<Integer> outVertexIDSet = new HashSet<Integer>(outVertices.size()*2,1);
			
			for (Vertex outVertex : outVertices) {
				
				int outVertexID = outVertex.getID();
				outVertexIDSet.add(outVertexID);
			}
			
			exportedGraph.put(vertexID, outVertexIDSet);
		}
		
		return exportedGraph;
	}

	/** Print a text representation of the graph to default output.
	 * 
	 */
	public void printGraph() {
		
		System.out.println("This is a text representation of the graph " + 
						   name + ":");
		
		for (int vertexID : vertices.keySet()) {
			
			Vertex vertex = vertices.get(vertexID);
			
			System.out.print("Vertex ID/Name: " + vertex.getID() + "/" +
							 vertex.getName() + "; adjacency list: ");
			
			for (Vertex toVertex : vertex.getOutEdges()) {
				
				System.out.print(toVertex.getID() + ",");
			}
			
			System.out.println();
		}
	}
}