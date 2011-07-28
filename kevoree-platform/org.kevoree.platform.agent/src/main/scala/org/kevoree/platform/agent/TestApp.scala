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
package org.kevoree.platform.agent

import java.net.InetSocketAddress
import com.twitter.finagle.builder.ClientBuilder
import com.twitter.finagle.Service
import com.twitter.util.Duration
import java.util.concurrent.TimeUnit


object TestApp extends Application {


  val client: Service[String, String] = ClientBuilder()
    .codec(StringCodec)
    .requestTimeout(Duration.fromTimeUnit(3000,TimeUnit.MILLISECONDS))
    .hosts(new InetSocketAddress(8080))
    .hostConnectionLimit(1)
    .build()



  // Issue a newline-delimited request, respond to the result
  // asynchronously:
  client("hi mom\n") onSuccess {
    result =>
      println("Received result asynchronously: " + result)
  } onFailure {
    error =>
      println("error")
      error.printStackTrace()
  } ensure {
    // All done! Close TCP connection(s):
    println("before releasez")
    client.release()
  }

  println("hello NIO !")


}