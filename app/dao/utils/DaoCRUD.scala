package dao.utils

import DatabaseAccessor.jdbcProfile.api._
import dao.utils.QueryExtensions._
import models.DatabaseObjectWithId
import slick.backend.DatabaseConfig
import slick.driver.JdbcProfile
import slick.lifted.ColumnOrdered

abstract class DaoCRUD[Q, E <: Table[Q]] {
  def baseQuery: TableQuery[E]

  implicit def database(implicit databaseAccessor: DatabaseConfig[JdbcProfile]): Database

  def count()(implicit acc: DatabaseConfig[JdbcProfile]): Int = {
    baseQuery.length.result.runAndAwait.getOrElse(0)
  }
  def countByFilter[V <: Rep[Boolean]](filter: E => V)(implicit acc: DatabaseConfig[JdbcProfile]): Int = {
    baseQuery.filter(filter).length.result.runAndAwait.get
  }

  /**
   * 一覧
   */
  def list()(implicit acc: DatabaseConfig[JdbcProfile]): Iterable[Q] = {
    baseQuery.result.runAndAwait.getOrElse(Iterable.empty[Q])
  }

  def updateByFilter[T <: Rep[Boolean], Mixed, Packed, Unpacked]
  (filter: E => T)
  (map: E => Mixed)
  (obj: Unpacked)
  (implicit shape: Shape[_ <: FlatShapeLevel, Mixed, Unpacked, Packed], acc: DatabaseConfig[JdbcProfile]): Unit = {
    baseQuery.filter(filter).map(map).update(obj).runAndAwait
  }

  /**
   * フィルタ付き削除
   */
  def deleteByFilter[V <: Rep[Boolean]](filter: E => V)(implicit acc: DatabaseConfig[JdbcProfile]): Unit = {
    baseQuery.filter(filter).delete.runAndAwait
  }

  /**
   * フィルタ付き検索(先頭一要素のみ)
   */
  def findFirstByFilter[V <: Rep[Boolean]](filter: E => V)(implicit acc: DatabaseConfig[JdbcProfile]): Option[Q] = {
    baseQuery.filter(filter).result.headOption.runAndAwait.getOrElse(None)
  }

  def findFirstByFilterWithSort[V <: Rep[Boolean]](filter: E => V)(sortBy: E => ColumnOrdered[_])(implicit acc: DatabaseConfig[JdbcProfile]): Option[Q] = {
    baseQuery.filter(filter).sortBy(sortBy).result.headOption.runAndAwait.getOrElse(None)
  }

  /**
   * フィルタ付き検索(リスト)
   */
  def findByFilter[V <: Rep[Boolean]](filter: E => V)(implicit acc: DatabaseConfig[JdbcProfile]): Iterable[Q] = {
    baseQuery.filter(filter).result.runAndAwait.getOrElse(Iterable.empty[Q])
  }

  def findByFilterWithSort[V <: Rep[Boolean]](filter: E => V)(sortBy: E => ColumnOrdered[_])(implicit acc: DatabaseConfig[JdbcProfile]): Iterable[Q] = {
    baseQuery.filter(filter).sortBy(sortBy).result.runAndAwait.getOrElse(Iterable.empty[Q])
  }

  private def getPage(pageNo: Int, pageSize: Int)(implicit acc: DatabaseConfig[JdbcProfile]): Iterable[Q] = {
    baseQuery.page(pageNo, pageSize).result.runAndAwait.get
  }

  def numPages(pageSize: Int)(implicit acc: DatabaseConfig[JdbcProfile]): Int = {
    baseQuery.numPages(pageSize).runAndAwait.get
  }

  def getPagination(pageNo: Int, pageSize: Int)(implicit acc: DatabaseConfig[JdbcProfile]): (Iterable[Q], Int) = {
    (getPage(pageNo, pageSize), numPages(pageSize))
  }

  private def getPageByFilter[V <: Rep[Boolean]](filter: E => V)(pageNo: Int, pageSize: Int)(implicit acc: DatabaseConfig[JdbcProfile]): Iterable[Q] = {
    baseQuery.filter(filter).page(pageNo, pageSize).result.runAndAwait.get
  }

  def numPagesByFilter[V <: Rep[Boolean]](filter: E => V)(pageSize: Int)(implicit acc: DatabaseConfig[JdbcProfile]): Int = {
    baseQuery.filter(filter).numPages(pageSize).runAndAwait.get
  }

  def getPaginationByFilter[V <: Rep[Boolean]](filter: E => V)(pageNo: Int, pageSize: Int)(implicit acc: DatabaseConfig[JdbcProfile]): (Iterable[Q], Int) = {
    (getPageByFilter(filter)(pageNo, pageSize), numPagesByFilter(filter)(pageSize))
  }
}

abstract class DaoCRUDWithId[Q <: DatabaseObjectWithId, E <: TableWithId[Q]] extends DaoCRUD[Q, E] {

  /**
   * 作成
   */
  def create(obj: Q)(implicit acc: DatabaseConfig[JdbcProfile]): Option[Long] = {
    (baseQuery returning baseQuery.map(_.id)).insertOrUpdate(obj).runAndAwait.getOrElse(None)
  }

  /**
   * 更新
   */
  def update(obj: Q)(implicit acc: DatabaseConfig[JdbcProfile]): Boolean = {
    val result = baseQuery.filter(_.id === obj.id).update(obj).runAndAwait
    result.nonEmpty
  }

  /**
   * 削除
   */
  def delete(obj: Q)(implicit acc: DatabaseConfig[JdbcProfile]): Unit = {
    baseQuery.filter(_.id === obj.id).delete.runAndAwait
  }

  /**
   * ID検索
   */
  def findById(id: Long)(implicit acc: DatabaseConfig[JdbcProfile]): Option[Q] = {
    baseQuery.filter(_.id === id).result.headOption.runAndAwait.getOrElse(None)
  }
}



