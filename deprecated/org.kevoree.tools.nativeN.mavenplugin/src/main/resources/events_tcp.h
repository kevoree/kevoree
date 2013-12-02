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
/**
 * Created by jed
 * User: jedartois@gmail.com
 * Date: 03/10/12
 * Time: 11:47
 */
 #ifndef EVENTS_H_TCP
 #define EVENTS_H_TCP

#include "events_common.h"

#include <sys/types.h>
#include <sys/ipc.h>
#include <sys/sem.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <stdlib.h>
#include <signal.h>
#include <sys/wait.h>
#include <stdio.h>
#include <errno.h>
#include <netdb.h>
#include <string.h>
#include <unistd.h>


#define FILE_ATTENTE 5000


/**
* Permet de creer un Socket.
* @param port Port à utiliser pour le socket
* @param type TCP ou UDP
* @return -1 erreur la socket si succes
*/
int createSocket(unsigned short *port, int type) {

  int    la_socket, autorisation , ok;

  struct sockaddr_in adresse;
  socklen_t          lgr;

  la_socket=socket(PF_INET, type, 0);
  if (la_socket==-1) { perror("socket"); return -1; }

   /* Pour pouvoir relancer immediatement un serveur TCP  */
   /* ou lancer, sur une meme machine,                    */
   /*    plusieurs recepteurs UDP sur                     */
   /*    le meme port de diffusion (broadcast, multicast) */
  autorisation=1;
  ok = setsockopt(la_socket, SOL_SOCKET, SO_REUSEADDR,&autorisation, sizeof(int));
  if (ok==-1) { perror("setsockopt"); return -1; }

  lgr = sizeof(struct sockaddr_in);
  memset(&adresse, 0, lgr) ;
  adresse.sin_family      = AF_INET ;
  adresse.sin_port        = htons(*port);
  adresse.sin_addr.s_addr = htonl(INADDR_ANY);

  ok = bind (la_socket, (struct  sockaddr*)&adresse, lgr);
  if (ok==-1) { perror("bind"); return -1; }

  ok = getsockname (la_socket, (struct sockaddr*)&adresse, &lgr);
  if (ok==-1) { perror("getsockname"); return -1; }

  *port = ntohs (adresse.sin_port);
  return (la_socket) ;
}


int createEventBroker_tcp(EventBroker *eventbroker)
{
   //   pid_t  pid;
      struct sockaddr_in adr ;
      socklen_t          lgradr ;
       Events ev;
      eventbroker->sckServer = createSocket(&(eventbroker->port), SOCK_STREAM);
      if(eventbroker->sckServer == -1){ return -1; }

     if (listen(eventbroker->sckServer,FILE_ATTENTE)==-1)
     {
     		perror("listen");
      		return -1;
     }
       while (1)
       {

	   //fprintf(stderr,"EventBroker is listenning... %d\n",eventbroker->sckServer);

     lgradr=sizeof(struct sockaddr_in);
     eventbroker->sckClient = accept(eventbroker->sckServer, (struct sockaddr*)&adr, &lgradr);
     if (eventbroker->sckClient==-1)
      	 	    {
        		                    if (errno!=EINTR) perror("accept_tcp");
        		                    continue;
        	  	  }

                                            if(read(eventbroker->sckClient,&ev,sizeof(Events)) > 0)
                                            {

                                                eventbroker->dispatch(ev);
                                               	 close(eventbroker->sckClient);

                                            }

      }


}



/**
* Crée un client
* @param encours Structure client contenant les sockets, adresses, qui sera initialisée
* @return 0 aucun problème, != 0 : erreur
*/
int createPublisher_tcp(Publisher *publisher)
{
	int ok;
 	((publisher)->socket) = socket(AF_INET, SOCK_STREAM, 0);
   	if(((publisher)->socket) == -1){ 	  return -1; }

   	publisher->hp = gethostbyname (publisher->hostname);
  	if ((publisher)->hp == NULL) { perror("gethostbyname");  	                fprintf(stderr,"gethostbyname_tcp");  return -1; }

  	(publisher)->lgradr = sizeof(struct sockaddr_in);
  	memset(&(publisher)->adr, 0, (publisher)->lgradr);
  	(publisher)->adr.sin_family = AF_INET;
 	(publisher)->adr.sin_port = htons ((publisher)->port);

  	(publisher)->adr.sin_addr = *((struct in_addr *) (((publisher)->hp) -> h_addr_list[0]));

 	 ok = connect((publisher)->socket, (struct sockaddr*)&(publisher)->adr, (publisher)->lgradr);
 	 if (ok == -1)
 	 {
                	 perror("connect_tcp");
                        fprintf(stderr,"connect_tcp");
 	  return -1;

 	  }
return 0;
}



int send_event_tcp(Publisher publisher,Events ev)
{

    if(createPublisher_tcp (&publisher) != -1)
    {

        if(send (publisher.socket, &ev, sizeof (Events),0)< 0)
        {
                   fprintf(stderr,"send_event_tcp ");
                   return -1;
        }

    } else
    {
      fprintf(stderr,"Create Publisher_tcp");
                     return -1;
    }
    close(publisher.socket);
    usleep(100);
    return 0;
}

 #endif