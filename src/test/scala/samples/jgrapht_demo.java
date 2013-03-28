package samples;

import java.util.Iterator;
import java.util.Set;

import org.jgrapht.alg.CycleDetector;
import org.jgrapht.traverse.TopologicalOrderIterator;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

/**
 This class is a demonstration program for creating a depencency chart,
 directed graph, then locating and outputting any implicit loops,
 cycles.
 */
public class jgrapht_demo {

   /**
    Test creating a directed graph, checking it for cycles and either
    outputting cycle information or ordering.

    @param createCycles true - create a directed graph which contains
    cycles.  false - create a directed graph which does not contain
    any cycles.
    */
   public static void test(boolean createCycles) {
      CycleDetector<String, DefaultEdge> cycleDetector;
      DefaultDirectedGraph<String, DefaultEdge> g;

      g = new DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge.class);

      // Add vertices, e.g. equations.
      g.addVertex("a");
      g.addVertex("b");
      g.addVertex("c");
      g.addVertex("d");
      g.addVertex("e");

      // Add edges, e.g. dependencies.
      // 2 cycles,
      //    a = f(b)
      //    b = f(c)
      //    c = f(a)
      // and
      //    d = f(e)
      //    e = f(d)
      g.addEdge("b", "a");
      g.addEdge("c", "b");
      if (createCycles) {
         g.addEdge("a", "c");
      }
      g.addEdge("e", "d");
      if (createCycles) {
         g.addEdge("d", "e");
      }

      System.out.println(g.toString());

      // Are there cycles in the dependencies.
      cycleDetector = new CycleDetector<String, DefaultEdge>(g);
      // Cycle(s) detected.
      if (cycleDetector.detectCycles()) {
         Iterator<String> iterator;
         Set<String> cycleVertices;
         Set<String> subCycle;
         String cycle;

         System.out.println("Cycles detected.");

         // Get all vertices involved in cycles.
         cycleVertices = cycleDetector.findCycles();

         // Loop through vertices trying to find disjoint cycles.
         while (! cycleVertices.isEmpty()) {
            System.out.println("Cycle:");

            // Get a vertex involved in a cycle.
            iterator = cycleVertices.iterator();
            cycle = iterator.next();

            // Get all vertices involved with this vertex.
            subCycle = cycleDetector.findCyclesContainingVertex(cycle);
            for (String sub : subCycle) {
               System.out.println("   " + sub);
               // Remove vertex so that this cycle is not encountered
               // again.
               cycleVertices.remove(sub);
            }
         }
      }

      // No cycles.  Just output properly ordered vertices.
      else {
         String v;
         TopologicalOrderIterator<String, DefaultEdge> orderIterator;

         orderIterator =
            new TopologicalOrderIterator<String, DefaultEdge>(g);
         System.out.println("\nOrdering:");
         while (orderIterator.hasNext()) {
            v = orderIterator.next();
            System.out.println(v);
         }
      }
   }

   /**
    Generate two cases, one with cycles, this is depencencies and one
    without.

    @param args Ignored.
    */
   public static void main(String [] args) {
      System.out.println("\nCase 1: There are cycles.");
      test(true);

      System.out.println("\nCase 2: There are no cycles.");
      test(false);

      System.out.println("\nAll done");
      System.exit(0);
   }
}