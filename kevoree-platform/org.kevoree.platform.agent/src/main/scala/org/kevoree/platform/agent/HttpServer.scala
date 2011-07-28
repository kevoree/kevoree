package org.kevoree.platform.agent

/**
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 3, 29 June 2007;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import com.twitter.finagle.Service
import org.jboss.netty.handler.codec.http._
import org.jboss.netty.handler.codec.http.HttpResponseStatus._
import org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1
import org.jboss.netty.buffer.ChannelBuffers.copiedBuffer
import org.jboss.netty.util.CharsetUtil.UTF_8
import com.twitter.util.Future
import org.kevoree.framework.KevoreeXmiHelper
import org.jboss.netty.buffer.ChannelBufferInputStream

object HttpServer {

  /**
   * The service itself. Simply echos back "hello world"
   */
  class Respond(agent: KevoreeRuntimeAgent) extends Service[HttpRequest, HttpResponse] {
    def apply(request: HttpRequest) = {

      try {
        val model = KevoreeXmiHelper.loadStream(new ChannelBufferInputStream(request.getContent))
        val response = new DefaultHttpResponse(HTTP_1_1, OK)
        response.setContent(copiedBuffer("ok thanks for the model ;-)", UTF_8))

        agent.processModel(model)

        Future.value(response)
      } catch {
        case _@e => {
          val response = new DefaultHttpResponse(HTTP_1_1, HttpResponseStatus.NOT_FOUND)
          response.setContent(copiedBuffer("error while loading model", UTF_8))
          Future.value(response)
        }
      }


    }
  }

}