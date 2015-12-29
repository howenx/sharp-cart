package filters

import javax.inject.Inject

import play.api.http.HttpFilters

/**
  * Created by howen on 15/12/28.
  */
class Filters @Inject()(log: LoggingFilter,
                        gzip: play.filters.gzip.GzipFilter
                       ) extends HttpFilters {
  var filters = Seq(log, gzip)

}
