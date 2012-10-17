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
 #ifndef EVENTS_COMMON_H
 #define EVENTS_COMMON_H

 typedef enum
   {
 	EV_STOP,
     EV_UPDATE,
 	EV_PORT_INPUT,
 	EV_PORT_OUTPUT
   } Type;

 typedef struct Events
 {
     Type ev_type;
     int id_port;
 } Events;

 typedef struct _EventBroker {
 	unsigned short  port; /* Port */
 	int sckServer; /* descripteur socket  Serveur*/
 	int sckClient; /* Descripteur du socket du client */
 	void (*dispatch)(Events ev);
 	struct sockaddr_in client;
 } EventBroker;


 typedef struct _Publisher {
 	int socket; /* point rdv */
 	unsigned short 	     port; /* Port */
 	struct hostent*      hp ;
 	struct sockaddr_in   adr ;
 	char hostname[512];
   	socklen_t            lgradr ;
 } Publisher;


 #endif