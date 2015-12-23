package dao.utils

import DatabaseAccessor.jdbcProfile.api._

/**
 * DDLへのアクセスIF
 */
trait SchemaAccessible {
  def createDDL: DBIOAction[_, NoStream, Effect.Schema]
}

