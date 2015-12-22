package dao.utils

import dao.DatabaseAccessor.jdbcProfile.api._
import org.slf4j.LoggerFactory

import scala.annotation.tailrec
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.{Duration, DurationInt}
import scala.language.{higherKinds, postfixOps}

object QueryExtensions {
  val logger = LoggerFactory.getLogger(getClass)

  implicit class QueryExtension[A, T, E[_]](query: Query[A, T, E]) {
    /**
     * DB表から1ページ分の行を取得する
     * @param pageNo ページ番号 最初のページは1
     * @param pageSize 1ページあたりの行数
     * @return
     */
    def page(pageNo: Int, pageSize: Int) = {
      query.drop((pageNo - 1) * pageSize).take(pageSize)
    }

    def numPages(pageSize: Int) = {
      query.length.result.map { numRows =>
        numRows / pageSize + (if (numRows % pageSize == 0) 0 else 1)
      }
    }
  }

  implicit class NoStreamRunExtension[R](action: DBIOAction[R, NoStream, Nothing]) {
    def run(implicit db: Database) = {
      db.run(action)
    }
    def runAndAwait(implicit db: Database):Option[R] = {
      doExecuteWithRetry()
    }

    private val MAX_RETRY_COUNT = 2
    @tailrec
    private def doExecuteWithRetry(triedNum: Int = 0)(implicit db: Database):Option[R] = {
      try {
        Some(Await.result(db.run(action), Duration.Inf))
      } catch {
        case e: InterruptedException =>
          logger.warn("<QuerySynchronousExecutable> Interrupted.")
          None
        case e: Throwable =>
          logger.warn("<QuerySynchronousExecutable> ErrorOccurred.", e)
          if (triedNum > MAX_RETRY_COUNT) {
            logger.warn("<QuerySynchronousExecutable> Fully failed to execute query %d times.".format(triedNum))
            None
          } else {
            logger.warn("<QuerySynchronousExecutable> Failed to execute query %d times and retry.".format(triedNum))
            Thread.sleep((5 seconds).toMillis)
            doExecuteWithRetry(triedNum + 1)
          }
      }
    }
  }

  implicit class StreamRunExtension[R](action: DBIOAction[_, Streaming[R], Nothing]) {
    def streamWithCallback(callback: (R) => Unit)(implicit db: Database) = {
      (try {
        Some(Await.result(
          db.stream(action).foreach(callback),
          Duration.Inf
        ))
      } catch {
        case e: Throwable =>
          e.printStackTrace()
          None
      }).get
    }
  }
}
