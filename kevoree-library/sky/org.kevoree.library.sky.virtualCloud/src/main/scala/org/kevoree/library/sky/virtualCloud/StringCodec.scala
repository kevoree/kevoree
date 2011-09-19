package org.kevoree.library.sky.virtualCloud

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
import com.twitter.finagle.{Codec}
import org.jboss.netty.handler.codec.string.{StringEncoder, StringDecoder}
import org.jboss.netty.channel.{Channels, ChannelPipelineFactory}
import org.jboss.netty.handler.codec.frame.{Delimiters, DelimiterBasedFrameDecoder}
import org.jboss.netty.util.CharsetUtil


object StringCodec extends StringCodec

class StringCodec extends Codec[String, String] {

  def pipelineFactory = new ChannelPipelineFactory {
      def getPipeline = {
        val pipeline = Channels.pipeline()
        pipeline.addLast("line",
          new DelimiterBasedFrameDecoder(100, Delimiters.lineDelimiter: _*))
        pipeline.addLast("stringDecoder", new StringDecoder(CharsetUtil.UTF_8))
        pipeline.addLast("stringEncoder", new StringEncoder(CharsetUtil.UTF_8))
        pipeline
      }
    }
}