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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.kevoree.remote.rest

import org.restlet.Application
import org.restlet.Restlet
import org.restlet.resource.Directory

class FileServerApplication(uri:String) extends Application {

  override def createInboundRoot() : Restlet = {
    val dir = new Directory(getContext(), uri);
    dir.setDeeplyAccessible(true)
    dir.setListingAllowed(true)
    dir.setNegotiatingContent(true)
    return dir
  }

}

