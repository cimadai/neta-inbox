package dao.utils

import dao.DatabaseAccessor.jdbcProfile.api._
import slick.lifted.{AbstractTable, ForeignKeyQuery, Index}

abstract class TableWithId[T](tag: Tag, name: String) extends Table[T](tag, name) {
  def id = column[Long]("ID", O.AutoInc, O.PrimaryKey)

  def createForeignKey[P, PU, TT <: AbstractTable[_], U](sourceColumns: P, targetTableQuery: TableQuery[TT])
                                                        (targetColumns: TT => P, onUpdate: ForeignKeyAction = ForeignKeyAction.NoAction, onDelete: ForeignKeyAction = ForeignKeyAction.NoAction)
                                                        (implicit unpack: Shape[_ <: FlatShapeLevel, TT, U, _], unpackp: Shape[_ <: FlatShapeLevel, P, PU, _]): ForeignKeyQuery[TT, U] = {

    foreignKey(
      "FK_%s_%s".format(sourceColumns.toString, targetTableQuery.baseTableRow.tableName),
      sourceColumns,
      targetTableQuery)(targetColumns, onUpdate, onDelete)
  }

  def createIndex[U](on: U, unique: Boolean = false)(implicit shape: Shape[_ <: FlatShapeLevel, U, _, _]): Index = {
    index("IDX_%S".format(on.toString), on, unique)
  }
}
