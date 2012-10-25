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

#include "events_common.h"

#include <fcntl.h>
#include <sys/stat.h>
#include <sys/types.h>
#include <unistd.h>

// named pipe

 int createEventBroker_fifo(EventBroker *eventbroker)
 {

    int fd,size;
    Events ev;
    /* create the FIFO (named pipe) */
    mkfifo(eventbroker->name_pipe, 0666);

    fd = open(eventbroker->name_pipe, O_RDONLY);
    if(fd <0)
    {
        perror("createEventBroker_fifo");
      return -1;
    }
    for(;;)
    {
       size =  read(fd, &ev,sizeof(Events));

       if(size >0)
       {
             eventbroker->dispatch(ev);
       }else
        {
            usleep(5000);
       }

    }
   /* remove the FIFO */

   return 0;
 }


void destroy_fifo(const char *name_pipe)
{
  #ifdef DEBUG
    fprintf(stderr,"Destroy fifo %s",name_pipe);
  #endif
    unlink(name_pipe);
}

int send_event_fifo(Publisher publisher,Events ev)
{
  int fd;
  #ifdef DEBUG
      switch(ev.ev_type)
      {
              case EV_PORT_INPUT:
                 fprintf(stderr,"sending event fifo %s EV_PORT_INPUT \n",publisher.name_pipe);
              break;

            case EV_UPDATE:
                 fprintf(stderr,"sending event fifo %s EV_UPDATE \n",publisher.name_pipe);
            break;

            case EV_STOP:
                 fprintf(stderr,"sending event fifo %s EV_STOP \n",publisher.name_pipe);
            break;

            case EV_PORT_OUTPUT:
                 fprintf(stderr,"sending event fifo %s EV_PORT_OUTPUT \n",publisher.name_pipe);
            break;
      }
  #endif
  fd = open(publisher.name_pipe, O_WRONLY);
  if(fd <0)
  {
      perror("send_event_fifo");
      return -1;
  }
  write(fd, &ev, sizeof(Events));
  close(fd);
 return 0;
}


