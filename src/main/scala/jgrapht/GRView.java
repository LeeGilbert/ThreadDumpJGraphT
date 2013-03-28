package jgrapht;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.JFrame;
import javax.swing.JScrollPane;


import org.jgraph.JGraph;
import org.jgraph.graph.*;
import org.jgraph.layout.*;
import org.jgrapht.ext.JGraphModelAdapter;
import org.jgrapht.graph.ListenableDirectedGraph;
import scala.Serializable;


public class GRView {
    JGraph jgraph;

    public static void main(String[] args) {
        new GRView();
    }

    public GRView() {
        //ListenableDirectedGraph<Serializable, DefaultEdge> graph = GR.gr();
        ListenableDirectedGraph<java.io.Serializable, DefaultEdge> graph = GRThreadump.gr();
        // create the view, then add data to the model
        JGraphModelAdapter adapter = new JGraphModelAdapter(graph);
        jgraph = new JGraph(adapter);
        JScrollPane jScrollPane = new JScrollPane(jgraph);
        JFrame frame = new JFrame();
        frame.add(jScrollPane);
        jScrollPane.setMinimumSize(new Dimension(1024, 786));
        frame.setTitle("Graph");
        frame.setPreferredSize(new Dimension(1024, 786));
        frame.setSize(new Dimension(1024, 786));
        frame.setSize(1024, 786);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        randomizeLocations();
        setColors();
        for(java.io.Serializable cycle : GRThreadump.getCycles()) {
            setColor(adapter, cycle);
        }
        CircleGraphLayout layout1 = new CircleGraphLayout();
        layout1.run(jgraph, jgraph.getRoots(), 0);
//        TreeLayoutAlgorithm layout = new TreeLayoutAlgorithm();
//        layout.run(jgraph, jgraph.getRoots(), 1);

        //jgraph.doLayout();

        jgraph.repaint();
        jScrollPane.invalidate();
        frame.pack();
        frame.setVisible(true);

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

    public void setColor(JGraphModelAdapter adapter, java.io.Serializable object) {
        GraphLayoutCache cache = jgraph.getGraphLayoutCache();
        DefaultGraphCell cell = adapter.getVertexCell(object);
        CellView view = cache.getMapping(cell, true);
        AttributeMap map = view.getAttributes();
        map.applyValue(GraphConstants.BACKGROUND, Color.RED);
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

}
