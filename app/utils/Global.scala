package utils

import org.slf4j.LoggerFactory
import play.api.{Application, GlobalSettings}

object Global extends GlobalSettings {
  private val logger = LoggerFactory.getLogger(getClass)
  override def onStart(app: Application): Unit = {
    logger.debug("Play application is starting.")
  }

  override def onStop(app: Application): Unit = {
    logger.debug("Play application is stopping.")
  }
}
