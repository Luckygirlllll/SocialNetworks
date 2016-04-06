package graph;

import java.util.ArrayList;
import java.util.List;

public class Vertex {

	/* The project requires egonets/SCCs to be new graphs (no object reuse)
	 * if this were not a requirement, it might be worth, instead of 
	 * declaring a single list of edges, declaring a map like
	 * "HashMap<CapGraph,List<Edge>> parentGraphs" to keep track of 
	 * all of the subgraphs a particular Vertex is a part of and the edges
	 * the Vertex has in each subgraph.
	 * To do this we also need to modify several other methods.
	 * 
	 * Could also change "List<Vertex> outEdges" to 
	 * "List<Map<Vertex,Integer>> outEdges" to store info about edge weights.
	 */
	private int vertexID;
	private String name;
	private List<Vertex> outEdges;
	
	public Vertex(int vertexID) {
		
		this(vertexID, Integer.toString(vertexID));
	}
	
	public Vertex(int vertexID, String name) {
		
		this.name = name;
		this.vertexID = vertexID;
		this.outEdges = new ArrayList<Vertex>();
	}
	
	/** Create an edge between this vertex and another vertex.
	 * 
	 * @param toVertex the vertex object the edge goes to
	 */
	public void createEdge(Vertex toVertex) {
		
		outEdges.add(toVertex);
	}
	
	public int getID() {
		
		return vertexID;
	}
	
	public String getName() {
		
		return name;
	}
	
	public List<Vertex> getOutEdges() {
		
		return outEdges;
	}
	
	public void setName(String name) {
		
		this.name = name;
	}
}
