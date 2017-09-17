package v1.twitter

import javax.inject.Inject

import play.api.routing.Router.Routes
import play.api.routing.SimpleRouter
import play.api.routing.sird._

import com.powertwitter.model._


/**
  * Routes and URLs to the PostResource controller.
  */
class TwitterRouter @Inject()(controller: TwitterController) extends SimpleRouter {
  val prefix = "/v1/posts"

  def link(id: TwitterId): String = {
    import com.netaporter.uri.dsl._
    val url = prefix / id.toString
    url.toString()
  }

  override def routes: Routes = {
    case GET(p"/") =>
      controller.index

    case POST(p"/") =>
      controller.process

    case GET(p"/$id") =>
      controller.show(id)
  }

}
