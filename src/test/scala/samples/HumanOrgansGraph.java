package samples;

import java.util.Set;

import org.jgraph.graph.DefaultEdge;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.ListenableDirectedGraph;

public class HumanOrgansGraph {
	
	enum Organs {HEART, LUNG, LIVER, STOMACH, BRAIN, SPINAL_CORD};
	enum Systems {CIRCULATORY, DIGESTIVE, NERVOUS, RESPIRATORY};
	
	ListenableDirectedGraph graph = null;
	
	/**
	 * Creates an instance using the provided graph.
	 * @param g The graph to use, or null to create a new one.
	 */
	public HumanOrgansGraph(ListenableDirectedGraph g)
	{
		if (g == null) {
			g = new ListenableDirectedGraph<Enum, DefaultEdge>(DefaultEdge.class);
		}	
		graph = g;
		// add vertices to the graph
		g.addVertex(Organs.HEART);
		g.addVertex(Organs.LUNG);
		g.addVertex(Organs.BRAIN);
		g.addVertex(Organs.STOMACH);
		g.addVertex(Organs.LIVER);
		g.addVertex(Organs.SPINAL_CORD);
		g.addVertex(Systems.CIRCULATORY);
		g.addVertex(Systems.NERVOUS);
		g.addVertex(Systems.DIGESTIVE);
		g.addVertex(Systems.RESPIRATORY);
		// link the vertices by edges
		g.addEdge(Organs.HEART, Systems.CIRCULATORY);
		g.addEdge(Organs.LUNG, Systems.RESPIRATORY);
		g.addEdge(Organs.BRAIN, Systems.NERVOUS);
		g.addEdge(Organs.SPINAL_CORD, Systems.NERVOUS);
		g.addEdge(Organs.STOMACH, Systems.DIGESTIVE);
		g.addEdge(Organs.LIVER, Systems.DIGESTIVE);
		
		// traverse the edges connected to DIGESTIVE vertex
		Set digestiveLinks = g.edgesOf(Systems.DIGESTIVE);
		System.out.println(digestiveLinks.size() + " digestive organs in the graph");
		for (Object item : digestiveLinks) {
           DefaultEdge anEdge = (DefaultEdge) item;
           Object opposite =  Graphs.getOppositeVertex( g, anEdge, Systems.DIGESTIVE);
		   System.out.println(opposite);
		}
	}

	/**
	 * Provide access to the graph, for use by chapter 6 code.
	 */
	public Graph getGraph()
	{
		return graph;
	}
	
	public static void main(String[] args) {
		new HumanOrgansGraph(null);
	}
}
