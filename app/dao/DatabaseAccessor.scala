package dao

import slick.jdbc.JdbcBackend.Database
import slick.driver.{H2Driver, JdbcProfile}

trait DatabaseAccessor {
  def database: Database
}

object DatabaseAccessor {
  val jdbcProfile: JdbcProfile = H2Driver
}
