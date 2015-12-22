package dao.utils

import dao.DatabaseAccessor.jdbcProfile.api._

/**
 * DDLへのアクセスIF
 */
trait SchemaAccessible {
  def createDDL: DBIOAction[_, NoStream, Effect.Schema]
}

