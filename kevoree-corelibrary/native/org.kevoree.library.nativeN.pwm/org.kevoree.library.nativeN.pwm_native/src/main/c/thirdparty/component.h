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

#ifndef COMPONENT_H
#define COMPONENT_H

#include <sys/ipc.h>
#include <sys/shm.h>
#include <stdlib.h>
#include <sys/shm.h>
#include <sys/stat.h>
#include <pthread.h>

#include "kqueue.h"
#include "events_udp.h"
#include "events_tcp.h"

#define LENGHT_MAX_NAME_PORT 32
#define NUMBER_PORTS 100

typedef struct _port
{
  char name[LENGHT_MAX_NAME_PORT];
  int id;
} Ports;

typedef struct _context
{
  int pid;
  int pid_jvm;
  int inputs_count;
  Ports inputs[NUMBER_PORTS];
  int outputs_count;
  Ports outputs[NUMBER_PORTS];
  int (*start) ();
  int (*stop) ();
  int (*update) ();
  void (*dispatch) (int id_port,int id_queue);
} Context;


int start ();
int stop ();
int update ();
int alive_component = 0;

Context *ctx;
EventBroker event_broker_tcp;
EventBroker event_broker_udp;
//Publisher component_event_publisher;

void notify(Events ev)
{
    int i;
    switch(ev.ev_type){

        case EV_PORT_INPUT:
                 ctx->dispatch (ev.id_port,ctx->inputs[ev.id_port].id);
        break;

        case EV_UPDATE:
              ctx->update ();
        break;

        case EV_STOP:

        for (i = 0; i < ctx->inputs_count; i++)
            {
              if (destroy_queue (ctx->inputs[i].id) != 0)
            {
              //error
              	fprintf(stderr,"destroy_queue INPUT %s \n",ctx->inputs[i].name);
            }
            }
          for (i = 0; i < ctx->outputs_count; i++)
            {
              if (destroy_queue (ctx->outputs[i].id) != 0)
            {
              //error
              fprintf(stderr,"destroy_queue OUTPUT %s \n",ctx->inputs[i].name);
            }
            }

          ctx->stop ();
          close(event_broker_tcp.sckServer);
          close(event_broker_udp.sckServer);
          exit (0);
        break;

        case EV_PORT_OUTPUT:
              // ignore
        break;

    }
}


void *   t_broker_tcp (void *p)
{
  p = NULL;
  fprintf(stderr,"createEventBroker_tcp %d \n",event_broker_tcp.port);

  createEventBroker_tcp (&event_broker_tcp);
  pthread_exit (NULL);
}

void *   t_broker_udp (void *p)
{
  p = NULL;
    fprintf(stderr,"event_broker_udp %d \n",event_broker_udp.port);

  createEventBroker_udp (&event_broker_udp);
  pthread_exit (NULL);
}


int bootstrap (key_t key,int port_event_broker)
{
   int shmid;
   void *ptr_mem_partagee;
   pthread_t t_event_broker_tcp;
   pthread_t t_event_broker_udp;

  /* create memory shared   */
   shmid = shmget (key, sizeof (Context), S_IRUSR | S_IWUSR);
   if (shmid < 0)
    {
      perror ("shmid");
      return -1;
    }

    if ((ptr_mem_partagee = shmat (shmid, NULL, 0)) == (void *) -1)
    {
      perror ("shmat");
      exit (1);
    }
    ctx = (Context *) ptr_mem_partagee;
    ctx->pid = getpid ();

    event_broker_tcp.port = port_event_broker;
    event_broker_tcp.dispatch = &notify;

    event_broker_udp.port = port_event_broker+1;
    event_broker_udp.dispatch = &notify;

    if (pthread_create (&t_event_broker_tcp, NULL, &t_broker_tcp, NULL) != 0)
    {
      return -1;
    }

    if (pthread_create (&t_event_broker_udp, NULL, &t_broker_udp, NULL) != 0)
    {
       return -1;
    }
  return 0;
}


void  process_output (int id_output, void *n_value)
{
	  kmessage kmsg;
	  kmsg.type = 1;
	  strcpy (kmsg.value,(const char*) n_value);
	  enqueue (ctx->outputs[id_output].id, kmsg);
}



#endif
