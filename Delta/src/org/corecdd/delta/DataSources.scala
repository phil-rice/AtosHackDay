package org.corecdd.delta

import org.apache.commons.dbcp.BasicDataSource
import org.springframework.jdbc.core.JdbcTemplate

object DataSources {
  val dataSource = new BasicDataSource();
  dataSource.setDriverClassName("com.mysql.jdbc.Driver");
  dataSource.setUrl("jdbc:mysql://localhost/hackday");
  dataSource.setUsername("hackday");
  dataSource.setPassword("hackday");

  val template = new JdbcTemplate(dataSource)
}