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
#include <string.h>
#include "settings.h"
#include "kqueue.h"
#include "events_fifo.h"
#include "HashMap.h"


const char * getDictionary(const char *key);

char fifo_name[SIZE_FIFO_NAME];

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
  struct HashMap  *map;
  Ports inputs[NUMBER_PORTS];
  int outputs_count;
  Ports outputs[NUMBER_PORTS];
  Publisher   p_event_fifo;
  int ipc_key;
  int alive;
  int (*start) ();
  int (*stop) ();
  int (*update) ();
  void (*dispatch) (int id_port,int id_queue);
} Context;


Context * getContext(int key)
{
    void *addr;
   // create memory shared
   int shmid = shmget(key,sizeof(Context), S_IRUSR | S_IWUSR );
   if(shmid < 0)
   {
       perror("shmid");
       return NULL;
   }
   addr = shmat(shmid, 0, 0);
   if(addr < 0)
   {
      perror("shmat");
      return NULL;
   }
    // bind to memory shared
  return  (Context *) addr;
}



int start ();
int stop ();
int update ();
int alive_component = 0;

Context *ctx;

EventBroker event_broker_fifo;

const char * getDictionary(const char *key)
{
     if(ctx != NULL)
     {
        if(ctx->map != NULL)
        {
                  return getFromHashMap(ctx->map,key);
        }else
        {
             fprintf(stderr,"Fatal Error the getDictionary is null [%s] \n",key);
              return NULL;
        }
     }
     else
     {
      	 fprintf(stderr,"Fatal Error the  getDictionary ctx is null %s \n",key);
         return NULL;
     }
}

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

        case EV_DICO_SET:
              #ifdef DEBUG
                 fprintf(stderr,"addToHashMap %s %s \n",ev.key,ev.value);
              #endif
              addToHashMap(ctx->map,strdup(ev.key),strdup(ev.value));
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
              /*
              close(event_broker_tcp.sckServer);
              close(event_broker_udp.sckServer);
              */
              // todo close fifo
               destroy_fifo(event_broker_fifo.name_pipe);
              exit (0);
        break;

        case EV_PORT_OUTPUT:
              // ignore
        break;

    }
}

void *   t_broker_fifo(void *p)
{

  fprintf(stderr,"event_broker_fifo %s \n",(char*)p);
  createEventBroker_fifo (&event_broker_fifo);
  pthread_exit (NULL);
}


int bootstrap (key_t key,int port_event_broker)
{
    pthread_t t_event_broker_fifo;
    ctx = getContext(key);
    ctx->pid = getpid ();

    sprintf(fifo_name,"%d.fifo",key);
    strcpy (event_broker_fifo.name_pipe,fifo_name);
    // dispather
    event_broker_fifo.dispatch = &notify;

    ctx->map = newHashMap();
    if(ctx->map == NULL)
    {
         perror ("newHashMap");
         return -1;
    }

    if (pthread_create (&t_event_broker_fifo, NULL, &t_broker_fifo, fifo_name) != 0)
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
