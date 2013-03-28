package jgrapht

import scala.collection.JavaConversions._
import org.jgrapht.graph.ListenableDirectedGraph
import org.jgraph.graph.DefaultEdge
import org.jgrapht.traverse.TopologicalOrderIterator

import org.jgrapht.event.{GraphListener, GraphVertexChangeEvent, GraphEdgeChangeEvent}
import org.jgrapht.{Graph, Graphs}
import org.jgrapht.alg.CycleDetector

object GR {

  def main(args: Array[String]) { gr }

  object Stock extends Enumeration { type t = Value
    val TEL, CPO, TRY = Value
  }
  object Portfolio extends Enumeration { type t = Value
    val P_1, P_2, P_3 = Value
  }

  def  gr() : ListenableDirectedGraph[Serializable, DefaultEdge] = {

    val g : ListenableDirectedGraph[Serializable, DefaultEdge]
                = new ListenableDirectedGraph(classOf[DefaultEdge]);
    g addGraphListener(new GListener)

    g addVertex Portfolio.P_1
    g addVertex Portfolio.P_2
    g addVertex Portfolio.P_3
    g addVertex Stock.TEL
    g addVertex Stock.CPO
    g addVertex Stock.TRY

    g addEdge(Portfolio.P_1, Stock.TEL )
    g addEdge(Portfolio.P_1, Stock.CPO )
    g addEdge(Portfolio.P_2, Stock.CPO )
    g addEdge(Portfolio.P_1, Stock.TRY )
    g addEdge(Portfolio.P_3, Portfolio.P_1 )
    g addEdge(Portfolio.P_3, Portfolio.P_2 )

    printlnOppositeVertices(g, Portfolio.P_1)
    printlnOppositeVertices(g, Portfolio.P_2)
    printlnOppositeVertices(g,  Portfolio.P_3)

    printTopologicalOrder(g)

    nodePredecessors(g)
    g
  }


  def printTopologicalOrder(g: ListenableDirectedGraph[Serializable, DefaultEdge]) {
    println("Topological Order:")
    val cyclicDetector = new CycleDetector(g);
    if (cyclicDetector.detectCycles()) {
      throw new IllegalStateException("Cyclic graph: " + cyclicDetector.findCycles());
    }
    new TopologicalOrderIterator(g) foreach {
      i => println(i)
    }
  }

  def printlnOppositeVertices(g: ListenableDirectedGraph[Serializable, DefaultEdge], node : Serializable) {
    println
    println ("Nodes Connected to: " + node)  // traverse the edges connected to vertex node
    g edgesOf node foreach { i =>  println( Graphs.getOppositeVertex(g, i.asInstanceOf[DefaultEdge], node)) }
    println
  }

  class GListener extends GraphListener[Serializable, DefaultEdge]() {
    def vertexAdded(p0: GraphVertexChangeEvent[Serializable]) { println ("adding node: " + p0.getVertex()); }
    def vertexRemoved(p0: GraphVertexChangeEvent[Serializable]) { println ("removing node: " + p0.getVertex()); }
    def edgeAdded(e : GraphEdgeChangeEvent[Serializable, DefaultEdge] ) {
      val r = getEdgeSrcTarget(e)
      println ("adding link: " + r._1 + ", " + r._2 )
    }
    def edgeRemoved(e : GraphEdgeChangeEvent[Serializable, DefaultEdge] ) {
      val r = getEdgeSrcTarget(e)
      println ("removing link: " + r._1 + ", " + r._2 )
    }

    def getEdgeSrcTarget(e: GraphEdgeChangeEvent[Serializable, DefaultEdge]): (Serializable, Serializable) = {
      val edge: DefaultEdge = e.getEdge()
      val graph: Graph[Serializable, DefaultEdge] = e.getSource.asInstanceOf[Graph[Serializable, DefaultEdge]]
      val source = graph.getEdgeSource(edge)
      val target = e.getSource.asInstanceOf[Graph[Serializable, DefaultEdge]].getEdgeTarget(e.getEdge())
      (target, source)
    }
  }


  /**
   * NodePredecessors sorted in node order
   */
  def nodePredecessors(g: ListenableDirectedGraph[Serializable, DefaultEdge]) {
    println
    println("Node Predecessors:")
    new TopologicalOrderIterator(g) foreach {n => {
        g edgesOf n foreach { e => {
          val source: Serializable = g.getEdgeSource(e)
          if (n != source) {
              println(n + " Parent: " + source)
            }
          }
        }
      }
    }
  }


}
