package org.corecdd.delta

import java.sql.ResultSet
import scala.collection.JavaConverters._
import org.springframework.jdbc.core.RowCallbackHandler
import DataSources.template
import org.antlr.stringtemplate.StringTemplate
import scala.io.Source
import java.util.HashMap
import java.util.Date
import java.io.FileOutputStream
import java.io.File

class DeltaCalculator(tableName: String, tableName1: String) {
  import DataSources._

  val colNames = findColumnName

  def findColumnName: List[String] = {
    var colNames = List[String]()
    template.query(s"select * from $tableName limit 1", new RowCallbackHandler() {
      def processRow(rs: ResultSet) {
        val md = rs.getMetaData();
        for (col <- 1 to md.getColumnCount) {
          colNames = md.getColumnName(col) :: colNames
        }
      }
    })
    colNames
  }

  def selectStatement(idName: String, ids: List[String]) =
    s"select t1.$idName from $tableName t1, $tableName1 t2  where  t1.id = t2.id  and " +
      s"(${ids.map((i) => s"t1.$idName = '$i'").mkString(" or ")}) and" +
      s"(${colNames.map((s) => s"t1.$s <> t2.$s").mkString(" or ")} )"

  def itemsThatHaveChanged(idName: String, workIds: List[String]) =
    template.queryForList(selectStatement(idName, workIds)).asScala.map((s) => s.asScala(idName)).map(_.asInstanceOf[String])

  def selectStatementForMap(idName: String, tableName: String) = s"select * from $tableName where $idName=?"

  def queryMap(idName: String, tableName: String, id: String) = template.queryForMap(selectStatementForMap(idName, tableName), id).asScala

}

object DeltaCalculator {
  def printToFile(f: java.io.File)(op: java.io.PrintWriter => Unit) {
    f.delete
    f.getParentFile().mkdirs()
    appendToFile(f)(op)
  }
  def appendToFile(f: java.io.File)(op: java.io.PrintWriter => Unit) {
    val p = new java.io.PrintWriter(new FileOutputStream(f, true))
    try { op(p) } finally { p.close() }
  }

  val destinationFile = new File("c:\\Users\\Phil\\Desktop\\hello.html")

  val demo =
    <user>
      <userid>
        A234567
      </userid>
      <company>
        IT Solutions
      </company>
      <email>
        IT-Solutions@it.com
      </email>
      <contactName>John</contactName>
      <search>
        <name>
          Search One
        </name>
        <frequency>
          Weekly
        </frequency>
        <results>
          1028730|1028731|1028732|1028734
        </results>
      </search>
    </user>

  def format(t: scala.collection.mutable.Map[String, _]) =
    Map[String, Any]() ++
      t.keys.map((k) =>
        (k -> (k.toString().toLowerCase() match {
          case k if k.endsWith("date") => t(k).toString.substring(0, 10)
          case "totalcapitalcost" =>
            f"£${t(k).toString.toInt}%,10d"
          case _ => t(k)
        })))

  def main(args: Array[String]) {
    val email: String = (demo \\ "email" text).trim;
    val contactName: String = (demo \\ "contactName" text).trim;
    val noticeIdText: String = demo \\ "results" text;
    val noticeIds = noticeIdText.trim.split("\\|").toList
    //    println(noticeIds)
    val dc = new DeltaCalculator("pipeline", "pipeline1")
    val idsChanged = dc.itemsThatHaveChanged("NoticeId", noticeIds)
    println(idsChanged)
    val changedMaps = idsChanged.map(dc.queryMap("NoticeId", "pipeline", _))
    val changed: Map[String, Map[String, _]] = Map[String, Map[String, _]]() ++ idsChanged.map((id) => {
      val map0 = dc.queryMap("NoticeId", "pipeline", id)
      val map1 = dc.queryMap("NoticeId", "pipeline1", id)
      val changedKeys = map0.keys.filter((k) => map0(k) != map1(k))
      val changed = Map() ++ changedKeys.map((k: String) => id -> Map[String, Any]("field" -> k, "old" -> map0(k), "new" -> map1(k)))
      (id -> changed)
    })
    println(changed.mkString("\n"))

    val newTenders = List("1072360", "1028730").map((id) => dc.queryMap("NoticeId", "pipeline", id))

    println(newTenders)
    val stream = getClass.getClassLoader.getResourceAsStream("org/corecdd/delta/email.st")
    val text: String = Source.fromInputStream(stream).mkString

    val st = new StringTemplate(text)
    st.setAttribute("email", email)
    st.setAttribute("contactName", contactName)

    for (t <- newTenders) {
      val formattedT = format(t)
      val map = new HashMap[String, Any]()
      for (k <- formattedT.keys) {
        map.put(k, formattedT(k))
      }
      st.setAttribute("newTenders", map)
    }
    for (t <- changedMaps) {
      val formattedT = format(t)
      val map = new HashMap[String, Any]()
      for (k <- formattedT.keys) {
        map.put(k, formattedT(k))
      }
      val id: String = map.get("NoticeID").toString
      val stuffThatHadActuallyChanged = changed(id)(id).asInstanceOf[Map[String, _]]
      val changedMapInCorrectStuff = new HashMap[String, Any]()
      for (k <- stuffThatHadActuallyChanged.keys)
        changedMapInCorrectStuff.put(k, stuffThatHadActuallyChanged(k))

      map.put("changed", changedMapInCorrectStuff)
      st.setAttribute("changedMap", map)
    }
    println(st)
    printToFile(destinationFile)((p) => p.append(st.toString))
  }
}