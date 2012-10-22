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


    #include <sys/ipc.h>
    #include <sys/shm.h>
    #include <stdlib.h>
    #include <sys/shm.h>
    #include <sys/stat.h>
    #include <pthread.h>
    #include <string.h>
     #include <stdio.h>
#define LENGHT_MAX_NAME_PORT 128
#define NUMBER_PORTS 100
#define SIZE_FIFO 512
#define LENGHT_MAX_VALUE_DICO 512



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





int main(void){


       Context  *ctx =getContext (79910);
 fprintf(stderr,"%d\n",ctx->alive);
       ctx->alive = 41;
return 0;
}