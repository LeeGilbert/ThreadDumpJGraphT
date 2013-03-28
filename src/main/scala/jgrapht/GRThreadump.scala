package jgrapht


import scala._
import collection.{TraversableLike, mutable}
import java.util
import collection.mutable.{ArrayBuffer, WrappedArray}
import scala.collection.JavaConversions._
import org.jgrapht.graph.ListenableDirectedGraph
import org.jgraph.graph.DefaultEdge


import org.jgrapht.event.{GraphListener, GraphVertexChangeEvent, GraphEdgeChangeEvent}
import org.jgrapht.{Graph, Graphs}
import org.jgrapht.alg.CycleDetector
import jgrapht.GR.GListener
import java.io.Serializable
import scala.Serializable

object GRThreadump {
  import scala.collection.JavaConversions._
  import scala.util.parsing.combinator._

  object HexParser extends RegexParsers {
    val hexNum: Parser[Int] = """[0-9a-f]+""".r ^^
      { case s:String => Integer.parseInt(s,16) }

    def seq: Parser[Any] = repsep(hexNum, ",")
    val x = HexParser.parse(HexParser.seq, "123bf9a9")

  }

  case class Vertex(name : String)  {
    override def toString(): String = { name.substring(name.lastIndexOf("t@")) }
  }

  var vertexCycleList = new java.util.ArrayList[java.io.Serializable]()

  def getCycles() = vertexCycleList

  case class JThread(name: String, waitingOn: Option[String]= None,  locks: Option[java.util.ArrayList[String]] = None, waitingOnThread: Option[JThread]= None, dependees  : java.util.ArrayList[JThread] = new java.util.ArrayList[JThread]() )

  def main(args: Array[String]): Unit = {
    getThreadList()
  }

  //  prod_threaddump-1363636978688
  def getThreadList(filename : String = "f:\\threaddump-1360814919636.tdump" ) : mutable.LinkedList[JThread] = {
    val lockIdMatcher = "[^<]*<(.*)>[^>]*".r
    val buf = new ArrayBuffer[JThread] mapResult { xs => mutable.LinkedList(xs: _*) }
    val jmap: mutable.HashMap[String, JThread] = new mutable.HashMap[String, JThread]()
    val blocks: util.ArrayList[String] = new util.ArrayList[String];
    var block = StringBuilder.newBuilder;

    def beginNewBlock {
      if (!block.mkString.contains("thread dump")) {
        blocks.add(block.mkString)
      }
      block = StringBuilder.newBuilder
    }
    def removeJBossFromThreadDumpBlock(s: String): Any = {
      if (block.length > 0 && !s.contains("org.jboss")) {
        block.append("\n")
      }
      if (!s.contains("org.jboss")) {
        block.append(s)
      }
    }

    def getWaitingOnLock(lines: Seq[String]): Option[String] = {
      val waitingTmp = lines.filter(b => { b.contains("- waitingOn") || b.contains("- parking") || b.contains("- waiting") }).toSeq
      val waiting = if (waitingTmp.length > 0) {
        Some(waitingTmp.get(0).replace("java.util.concurrent.lock", ""))
      } else None
      val waitingOnLock: Option[String] = waiting match {
        case Some(x) => {
          val lockIdMatcher(inside) = x;
          Some(inside)
        }
        case None => None
      }
      waitingOnLock
    }
    def getRawLockedTextLines(lines: Seq[String]): Seq[String] =  lines.filter(_.contains("- locked "))

    def getLocksHeld(locks: Option[Seq[String]]): Option[util.ArrayList[String]] = {
      val lockedHeld = locks match {
        case Some(x) => {
          val blocks: util.ArrayList[String] = new util.ArrayList[String];
          x.foreach {
            e => {
              val v = lockIdMatcher.findFirstMatchIn(e) match {
                case Some(x) => {
                  val lockIdMatcher(inside) = x;
                  Some(inside)
                }
                case None => None
              }
              blocks.append(v.getOrElse(""))
            }
          }
          Some(blocks)
        }
        case None => None
      }
      lockedHeld
    }

    def parseLinesIntoBlocks = {
      val lines = fLines(filename);
      lines.foreach(s => {
        if (s.contains("Thread t@")) beginNewBlock
        removeJBossFromThreadDumpBlock(s)
      })
      if (block.length > 0) blocks.add(block.mkString)
      //      blocks.foreach(b => { println(b) })
    }

    def getLockedText(lines: Seq[String]): Seq[String] =  lines.filter(_.contains("- locked "))

    //************ Main Processing
    parseLinesIntoBlocks

    blocks.foreach( b => {
      val lines = b.toString().split("\n").toSeq
      val name = lines.get(0)
      val state = lines.get(1).split(":").last
      val waitingOnLock = getWaitingOnLock(lines)
      val lockedText = getRawLockedTextLines(lines)
      val locks = if (lockedText.length > 0) { Some(lockedText) } else None

      val lockedHeld: Option[util.ArrayList[String]] = getLocksHeld(locks)

      val thread: JThread = JThread(name, waitingOnLock, lockedHeld)
      lockedHeld match {
        case Some(x) => {
          x.foreach(lock => jmap += lock -> thread)
        }
        case None =>
      }
      buf += thread
    })

    //
    val jthreads = buf.result()
    val jthreadsMapped = jthreads.map(t => {
      val waitingOn: String = t.waitingOn.getOrElse("")
      var yealdee: JThread = t;
      if (!waitingOn.isEmpty) {
        val tt = jmap.get(waitingOn)
        yealdee = t.copy(waitingOnThread = tt)
      }
      yealdee;
    })

    val waitingThreads = jthreadsMapped.filter(_.waitingOnThread.isDefined)
    jthreadsMapped.foreach(t => {
      val dependentOnMe = waitingThreads.filter(_.waitingOnThread.get.name.equals(t.name))
      if (!dependentOnMe.isEmpty) {
        dependentOnMe.foreach(x => {
          val xname = x.name
          t.dependees.add(x)
        })
      }
    })

    jthreadsMapped.filter(_.waitingOnThread.isDefined).foreach(t => println(t.name, " waitingOn: " + t.waitingOnThread.get.name, " myLocks: " + t.locks.getOrElse("NoLocksHeld"), printDependeeThreads(t.dependees)));
    jthreadsMapped
  }

  def fLines(f: String) =  scala.io.Source.fromFile(f).getLines.toList;


  def printDependeeThreads(t: java.util.ArrayList[JThread]) : String =  {
    var bldr = mutable.StringBuilder.newBuilder
    bldr.append("\n")
    t.foreach( tt => {bldr.append("         |");bldr.append(tt.name);bldr.append("\t waitingOn: ");bldr.append(tt.waitingOn.get); bldr.append("\n"); })
    return bldr.result()
  }


  def  gr() : ListenableDirectedGraph[java.io.Serializable, DefaultEdge]  = {
    val g : ListenableDirectedGraph[java.io.Serializable, DefaultEdge]  = new ListenableDirectedGraph(classOf[DefaultEdge])
 //   g addGraphListener(new GListener)

    val vertexMap : mutable.HashMap[String, Vertex] = new mutable.HashMap[String, Vertex]()
    def createVertexes {
      getThreadList().foreach(t => {
        val v: Vertex = Vertex(t.name)
        g addVertex v
        vertexMap.put(v.name, v)
      })
    }

    def addEdges {
      getThreadList().foreach(t => {
        t.dependees.foreach(dt => {
          val v1 = vertexMap.get(t.name).get
          val v2 = vertexMap.get(dt.name)
          val v2x = v2.getOrElse({
            val v: Vertex = Vertex(dt.name);
            vertexMap.put(v.name, v);
            g addVertex v;
            v
          })
          g.addEdge(v1, v2x)
        })
      })
    }

    def removeVertexesWithDegreeZero {
      val buf = new ArrayBuffer[java.io.Serializable] mapResult { xs => mutable.LinkedList(xs: _*) }
      g.vertexSet().foreach(v => { if (g.edgesOf(v).size() == 0) buf += v })
      val degreeZero = buf.result()
      degreeZero.foreach(g.removeVertex(_))
    }
    def removeVertexesWithDegreeOne {
      val buf = new ArrayBuffer[java.io.Serializable] mapResult { xs => mutable.LinkedList(xs: _*) }
      g.vertexSet().foreach(v => { if (g.edgesOf(v).size() == 1) buf += v })
      val degreeOne = buf.result()
      degreeOne.foreach(v => {
        val edges = g.edgesOf(v)
        edges.foreach(g.removeEdge(_))
        g.removeVertex(v)
      })
    }
    def detectAndLogCycles {
      val cycleDetector = new CycleDetector[java.io.Serializable, DefaultEdge](g)
      val cyclesFound = cycleDetector.detectCycles()
      if (cyclesFound) {
        cycleDetector.findCycles().foreach(cycle => {
          println("Cycle: " + cycle)
          vertexCycleList.add(cycle)
          val subCycle = cycleDetector.findCyclesContainingVertex(cycle)
          subCycle.foreach(v => println("SubCycle: " + v))
        }
        )
      }
    }

    createVertexes
    addEdges
    removeVertexesWithDegreeZero
    removeVertexesWithDegreeOne
    detectAndLogCycles
    g
  }
  }
