package org.corecdd.delta

import java.sql.ResultSet

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowCallbackHandler

/** This turned out to be not needed. It makes a one to many relationship from items in the pipeline to CPD codes*/
object MakeCPDCodeTable {
  import DataSources._

  def sortOutCpd(offset: String) {
    val idName = s"idAndCpv$offset"
    val pipelineName = s"pipeline$offset"
    template.update(s"delete from $idName")
    template.query(s"select id,CPDCodes from $pipelineName", new RowCallbackHandler() {
      def processRow(rs: ResultSet) {
        val id = rs.getString("id")
        val cpvCodes = rs.getString("CPDCodes")
        if (cpvCodes.trim.length > 0) {
          val cpvs = cpvCodes.split("\\|").toList
          try {
            println(s"Id: ${id} CPD: ${cpvs}")
            for (c <- cpvs)
              template.update(s"insert into $idName (id,cpd) values (?,?)", id, c)
          } catch { case t: Throwable => }
        }
      }
    })
  }

  def main(args: Array[String]) {
    sortOutCpd("")
    sortOutCpd("1")
  }

}