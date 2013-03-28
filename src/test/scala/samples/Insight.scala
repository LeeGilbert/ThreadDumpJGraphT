package samples

import java.util.Comparator


/**
*
 */
object Insight {

  trait NaturalComparable[T <: Comparable[T]] extends Comparator[T] with Comparable[T] {
    override def compare(o1 : T, o2 : T) : Int
    override def compareTo(o: T) : Int
  }
  class Employee extends NaturalComparable[Employee] {
    override def compareTo(e :Employee): Int = {
      if (e==this) return 0;
      -1
    }
    override def compare(e :Employee, e1 :Employee): Int = {e.compareTo(e1)}
  }
  class Manager extends Employee


  def main(args: Array[String]): Unit = {
    val e = new Employee
    val e2 = new Employee
    val m = new Manager
    val m2 = new Manager
    val lm : List[Manager]  = {m :: m2}
    val lm2 : List[Manager]  = {m :: m2}

    println(lm.compareTo(lm2))
  }

}
