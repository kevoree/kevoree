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
 	EV_PORT_OUTPUT,
 	EV_DICO_SET
   } Type;

 typedef struct Events
 {
     Type ev_type;
     int id_port;
     char key[512];
     char value[1024];
 } Events;


 typedef struct _EventBroker {

   /* TCP & UDP */
 	unsigned short  port; /* Port */
 	int sckServer; /* descripteur socket  Serveur*/
 	int sckClient; /* Descripteur du socket du client */

 	/* named pipe */
 	char name_pipe[512];


 	/* DISPACHER */
 	void (*dispatch)(Events ev);
 } EventBroker;


 typedef struct _Publisher {
 	/* int socket; point rdv
 	unsigned short 	     port;
 	struct hostent*      hp ;
 	struct sockaddr_in   adr ;
 	char hostname[512];
   	socklen_t            lgradr ;
                                     */

   /* named pipe */
   char name_pipe[512];


 } Publisher;


 #endif