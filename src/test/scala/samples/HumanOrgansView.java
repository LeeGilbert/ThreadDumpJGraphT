package samples;

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.util.Random;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import org.jgraph.JGraph;
import org.jgraph.graph.AttributeMap;
import org.jgraph.graph.CellView;
import org.jgraph.graph.DefaultEdge;
import org.jgraph.graph.EdgeView;
import org.jgraph.graph.GraphCell;
import org.jgraph.graph.GraphConstants;
import org.jgraph.graph.GraphLayoutCache;
import org.jgraph.layout.CircleGraphLayout;
import org.jgrapht.ext.JGraphModelAdapter;
import org.jgrapht.graph.ListenableDirectedGraph;


public class HumanOrgansView {

    JGraph jgraph;

    public HumanOrgansView() {
        // use a JGraphT listenable graph
        ListenableDirectedGraph<Enum, DefaultEdge> graph = new ListenableDirectedGraph<Enum, DefaultEdge>(DefaultEdge.class);
        // create the view, then add data to the model
        JGraphModelAdapter adapter = new JGraphModelAdapter(graph);
        jgraph = new JGraph(adapter);
        JScrollPane scroller = new JScrollPane(jgraph);
        JFrame frame = new JFrame("The Body");
        frame.setSize(600,600);
        frame.add(scroller);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // now add the data
        // now add the data
        HumanOrgansGraph hog = new HumanOrgansGraph(graph);
        removeEdgeLabels();
        setColors();
        CircleGraphLayout layout = new CircleGraphLayout();
        layout.run(jgraph, jgraph.getRoots(), 0);
        jgraph.getGraphLayoutCache().reload();
        jgraph.repaint();
    }

    /**
     * Removing edge labels
     */
    public void removeEdgeLabels() {
        GraphLayoutCache cache = jgraph.getGraphLayoutCache();
        CellView[] cells = cache.getCellViews();
        for (CellView cell : cells) {
            if (cell instanceof EdgeView) {
                EdgeView ev = (EdgeView) cell;
                DefaultEdge eval = (DefaultEdge) ev.getCell();
                eval.setUserObject("");
            }
        }
        cache.reload();
        jgraph.repaint();
    }

    /**
     * Setting vertex colors
     */
    public void setColors() {
        GraphLayoutCache cache = jgraph.getGraphLayoutCache();
        for (Object item : jgraph.getRoots()) {
            GraphCell cell = (GraphCell) item;
            CellView view = cache.getMapping(cell, true);
            AttributeMap map = view.getAttributes();
            map.applyValue(GraphConstants.BACKGROUND, Color.GREEN);
        }
        cache.reload();
        jgraph.repaint();
    }

    /**
     * Example of how to randomize vertex locations
     */
    public void randomizeLocations() {
        GraphLayoutCache cache = jgraph.getGraphLayoutCache();
        Random r = new Random();
        for (Object item : jgraph.getRoots()) {
            GraphCell cell = (GraphCell) item;
            CellView view = cache.getMapping(cell, true);
            Rectangle2D bounds = view.getBounds();
            bounds.setRect(r.nextDouble() * 400, r.nextDouble() * 400, bounds.getWidth(), bounds.getHeight());
        }
        cache.reload();
        jgraph.repaint();
    }

    public static void main(String[] args) {
        new HumanOrgansView();
    }
}
