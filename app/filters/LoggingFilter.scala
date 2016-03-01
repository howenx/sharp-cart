package filters

/**
  * Created by howen on 15/12/28.
  */

import play.api.Logger
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc._

class LoggingFilter extends EssentialFilter {
  def apply(nextFilter: EssentialAction) = new EssentialAction {
    def apply(requestHeader: RequestHeader) = {

      val accessLogger = Logger("access")
      val startTime = System.currentTimeMillis

      nextFilter(requestHeader).map { result =>

        val endTime = System.currentTimeMillis
        val requestTime = endTime - startTime

        accessLogger.info(
          s"${requestHeader.method} ${requestHeader.host}${requestHeader.uri}" +
          s" Remote Address:${requestHeader.remoteAddress} " +
          s" id-token:${requestHeader.headers.get("id-token").getOrElse("")} " +
          s" Time:${requestTime}ms"+
          s" Status: ${result.header.status}"+
          s" User-Agent:${requestHeader.headers.get("User-Agent").getOrElse("")}")
        result.withHeaders("Request-Time" -> requestTime.toString)
      }
    }
  }
}