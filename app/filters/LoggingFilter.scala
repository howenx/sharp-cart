package filters

/**
  * Created by howen on 15/12/28.
  */

import io.netty.channel.ChannelHandlerContext
import play.api.Logger
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc._
import play.mvc.Http

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
            s" X_FORWARDED_FOR:${requestHeader.headers.get(Http.HeaderNames.X_FORWARDED_FOR).getOrElse("")} " +
            s" Netty:${} " +
            s" id-token:${requestHeader.headers.get("id-token").getOrElse("")} " +
            s" Time:${requestTime}ms" +
            s" Status: ${result.header.status}" +
            s" User-Agent:${requestHeader.headers.get("User-Agent").getOrElse("")}")
        result.withHeaders("Request-Time" -> requestTime.toString)
      }
    }
  }
}