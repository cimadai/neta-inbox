package dao.model

abstract class DatabaseObjectWithId(val id: Option[Long])

abstract class DatabaseObjectWithIdAndConfig(override val id: Option[Long], val configKey: String, val configValue: String, val configType: String) extends DatabaseObjectWithId(id)
