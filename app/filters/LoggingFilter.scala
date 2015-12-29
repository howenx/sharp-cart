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

        accessLogger.error(s"${requestHeader.method} ${requestHeader.domain}${requestHeader.uri}" +
          s" remoteAddress:${requestHeader.remoteAddress} "+
          s" id-token:${requestHeader.getQueryString("id-token")} "+
          s" took:${requestTime}ms status: ${result.header.status}")
        result.withHeaders("Request-Time" -> requestTime.toString)
      }
    }
  }
}