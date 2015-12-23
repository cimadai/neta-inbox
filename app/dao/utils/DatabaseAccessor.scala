package dao.utils

import slick.driver.{H2Driver, JdbcProfile}

object DatabaseAccessor {
  val jdbcProfile: JdbcProfile = H2Driver
}
