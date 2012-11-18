/**
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 3, 29 June 2007;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kevoree.tools.aether.framework

import org.sonatype.aether.transfer.{TransferEvent, TransferResource, AbstractTransferListener}
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap
import java.text.{DecimalFormatSymbols, DecimalFormat}
import java.util.Locale

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 18/11/12
 * Time: 10:25
 */
class SLF4JTransferListener extends AbstractTransferListener{

  val logger = LoggerFactory.getLogger(this.getClass)

  private val downloads : java.util.Map[TransferResource, Long] = new ConcurrentHashMap[TransferResource, Long]()

  private var lastLength : Int = 0

  override def transferInitiated( event :TransferEvent ){
    val message = if(event.getRequestType() == TransferEvent.RequestType.PUT){ "Uploading" } else { "Downloading" }
    logger.info(message + ": " + event.getResource().getRepositoryUrl() + event.getResource().getResourceName() )
  }

  override def transferProgressed( event : TransferEvent  )
  {
    if (logger.isDebugEnabled){
      val resource = event.getResource();
      downloads.put( resource, java.lang.Long.valueOf( event.getTransferredBytes() ) )
      val buffer = new StringBuilder( 64 )
      import scala.collection.JavaConversions._
      downloads.foreach { entry =>
        val total = entry._1.getContentLength();
        val complete = entry._2.longValue();
        buffer.append( getStatus( complete, total ) ).append( "  " );
      }

      val padV = lastLength - buffer.length
      lastLength = buffer.length
      pad( buffer, padV );
      buffer.append( '\r' );
      logger.debug( buffer.toString() )
    }

  }

  private def getStatus( complete : Long, total : Long ) : String =
  {
    if ( total >= 1024 )
    {
      return toKB( complete ) + "/" + toKB( total ) + " KB ";
    }
    else if ( total >= 0 )
    {
      return complete + "/" + total + " B ";
    }
    else if ( complete >= 1024 )
    {
      return toKB( complete ) + " KB ";
    }
    else
    {
      return complete + " B ";
    }
  }

  private def pad( buffer : StringBuilder , spaces2 : Int )
  {
    var spaces = spaces2
    val block = "                                        ";
    while ( spaces > 0 )
    {
      val n = Math.min( spaces, block.length() )
      buffer.appendAll( block.toCharArray, 0, n );
      spaces -= n;
    }
  }

  override def transferSucceeded( event :TransferEvent )
  {
    transferCompleted( event );
    val resource = event.getResource();
    val contentLength = event.getTransferredBytes();
    if ( contentLength >= 0 )
    {
      val `type` = if( event.getRequestType() == TransferEvent.RequestType.PUT){ "Uploaded"}else { "Downloaded" }
      val len = if(contentLength >= 1024){ toKB( contentLength ) + " KB" } else { contentLength + " B" }

      var throughput = "";
      val duration = System.currentTimeMillis() - resource.getTransferStartTime();
      if ( duration > 0 )
      {
        val format = new DecimalFormat( "0.0", new DecimalFormatSymbols( Locale.ENGLISH ) );
        val kbPerSec = ( contentLength / 1024.0 ) / ( duration / 1000.0 );
        throughput = " at " + format.format( kbPerSec ) + " KB/sec";
      }

      logger.info( `type` + ": " + resource.getRepositoryUrl() + resource.getResourceName() + " (" + len+ throughput + ")" );
    }
  }

  override def transferFailed( event : TransferEvent  )
  {
    transferCompleted( event );
    //event.getException().printStackTrace( out );
  }

  private def transferCompleted( event :TransferEvent  )
  {
    downloads.remove( event.getResource() );
    val buffer = new StringBuilder( 64 );
    pad( buffer, lastLength );
    buffer.append( '\r' );
    logger.debug( buffer.toString() );
  }

  override def transferCorrupted( event :TransferEvent  )
  {
    logger.info("CorruptedTransfert "+event.getResource().getResourceName())
    //event.getException().printStackTrace( out );
  }

  protected def toKB( bytes : Long ) : Long =
  {
    return ( bytes + 1023 ) / 1024;
  }


}
