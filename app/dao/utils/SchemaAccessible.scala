package dao.utils

import DatabaseAccessor.jdbcProfile.api._

/**
 * DDLへのアクセスIF
 */
trait SchemaAccessible {
  self: DaoCRUD[_, _ <: Table[_]] =>

  def createDDL: DBIOAction[_, NoStream, Effect.Schema] = baseQuery.schema.create

  def truncateDDL(): DBIOAction[_, NoStream, Effect.Schema] = {
    sqlu"TRUNCATE TABLE #${baseQuery.baseTableRow.tableName}"
  }
}

