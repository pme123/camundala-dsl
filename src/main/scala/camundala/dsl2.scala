package camundala

import scala.collection.mutable.ArrayBuffer

object dsl2 :
  class Table:
    val rows = new ArrayBuffer[Row]
    def add(r: Row): Unit = rows += r
    override def toString = rows.mkString("Table(", ", ", ")")

  class Row:
    val id = new ArrayBuffer[Ident]
    val cells = new ArrayBuffer[Cell]
    def set(i:Ident) = 
      id.clear()
      id += i

    def add(c: Cell): Unit = cells += c
    override def toString = cells.mkString("Row(", ", ", ")")

  case class Cell(elem: String)
  case class Ident(elem: String)

  def table(init: Table ?=> Unit) =
    given t: Table = Table()
    init
    t

  def row(init: Row ?=> Unit)(using t: Table) =
    given r: Row = Row()
    init
    t.add(r)


  def cell(str: String)(using r: Row) =
    r.add(new Cell(str))

  def ident(str: String)(using r: Row) =
    r.set(new Ident(str))

object runner extends App:
  import dsl2._
  println (table {
    row {
      ident("myId")
      cell("ok")
      cell("top left")
      cell("top right")
    }
    row {
      cell("bottom left")
      cell("bottom right")
    }
  })