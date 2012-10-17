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

#include <jni.h>
#include <nativelib.h>
#include <stdio.h>
#include <sys/ipc.h>
#include <sys/shm.h>
#include <pthread.h>
#include <unistd.h>
#include <stdio.h>
#include <sys/shm.h>
#include <sys/stat.h>
#include "component.h"


void *ptr_mem_partagee;
int shmid;

JavaVM *g_vm;
jobject g_obj;
jmethodID g_mid;
 /*
Publisher   p_event_udp;
Publisher   p_event_tcp;
*/
#define SIZE_FIFO 512
char fifo_name[SIZE_FIFO];
Publisher   p_event_fifo;

int alive=0;

void   process (char *queue, void *msg)
{
  JNIEnv *g_env;
  int getEnvStat = (*g_vm)->GetEnv (g_vm, (void **) &g_env, JNI_VERSION_1_6);

  if (getEnvStat == JNI_EDETACHED)
    {
      if ((*g_vm)->AttachCurrentThread (g_vm, (void **) &g_env, NULL) != 0)
	{
	  printf ("Failed to attach\n");
	}
    }
  else if (getEnvStat == JNI_OK)
    {
      //
    }
  else if (getEnvStat == JNI_EVERSION)
    {
      printf ("GetEnv: version not supported\n");
    }

  (*g_env)->CallVoidMethod (g_env, g_obj, g_mid,(*g_env)->NewStringUTF (g_env, queue), (*g_env)->NewStringUTF (g_env, msg));
  if ((*g_env)->ExceptionCheck (g_env))
    {
      (*g_env)->ExceptionDescribe (g_env);
    }
  (*g_vm)->DetachCurrentThread (g_vm);
}

   // todo notify
void *manage_callbacks(void *p)
{
     ctx = (Context*)ptr_mem_partagee;
     int i,j;
    while(alive ==1)
    {
          if(ctx != NULL)
          {
              for(i=0;i<  ctx->outputs_count;i++)
              {
                 for(j=0;j<getQnum(ctx->outputs[i].id);j++)
                 {
                    kmessage *msg = dequeue(ctx->outputs[i].id);
                    if(msg !=NULL)
                      {
                              process(ctx->outputs[i].name,msg->value);
                        }
                 }
              }
              usleep(1000);
         }
    }
    pthread_exit(NULL);
}


JNIEXPORT jint JNICALL
JNI_OnLoad (JavaVM * jvm, void *reserved)
{
  g_vm = jvm;
  return JNI_VERSION_1_6;
}


JNIEXPORT jboolean JNICALL
Java_org_kevoree_tools_nativeN_NativeJNI_register (JNIEnv * env, jobject obj)
{
    pthread_t callbacks;
    alive = 1;
        if(pthread_create (& callbacks, NULL,&manage_callbacks, NULL) != 0){
            return -1;
        }
  // convert local to global reference
  g_obj = (*env)->NewGlobalRef (env, obj);
  // save refs for callback
  jclass g_clazz = (*env)->GetObjectClass (env, g_obj);

  if (g_clazz == NULL)
    {
      printf ("Failed to find class \n");
    }

//   (*env)->GetMethodID (env, g_clazz, "dispatchEvent", "(ILjava/lang/String;)V");    (int v,string)
// "(Ljava/lang/String;Ljava/lang/String;)V")
//http://docs.oracle.com/javase/1.5.0/docs/guide/jni/spec/types.html#wp276
  g_mid =       (*env)->GetMethodID (env, g_clazz, "dispatchEvent", "(Ljava/lang/String;Ljava/lang/String;)V");
  if (g_mid == NULL)
    {
      printf ("Unable to get method ref");

    }
  return (jboolean) 0;
}



void native_notify(Events ev)
{
    int j;
    kmessage *msg ;

    switch(ev.ev_type)
    {
        case EV_PORT_OUTPUT:

         for (j = 0; j < getQnum (ev.id_port); j++)
		{
		         msg = dequeue (ev.id_port);
             	 if (msg != NULL)
                 {
             		    process (ctx->outputs[ev.id_port].name, msg->value);
             	  }
		}
		break;

    }
}




JNIEXPORT jint JNICALL Java_org_kevoree_tools_nativeN_NativeJNI_init (JNIEnv * env, jobject obj, jint key)
{

  sprintf(fifo_name,"%d.fifo",key);
  strcpy (p_event_fifo.name_pipe,fifo_name);

  // create memory shared
  shmid = shmget (key, sizeof (Context), IPC_CREAT | S_IRUSR | S_IWUSR);
  if (shmid < 0)
    {
      perror ("shmid");
      return -1;
    }

  if ((ptr_mem_partagee = shmat (shmid, NULL, 0)) == (void *) -1)
    {
      perror ("shmat");
      return -1;
    }

  ctx = (Context *) ptr_mem_partagee;
  ctx->pid = -1;
  ctx->outputs_count = 0;
  ctx->inputs_count = 0;

  ctx->pid_jvm = getpid ();

  //    fprintf (stderr,"shared memory attached at address %p\n", ptr_mem_partagee);
  return shmid;
}

JNIEXPORT jint JNICALL
Java_org_kevoree_tools_nativeN_NativeJNI_start (JNIEnv * env, jobject obj,
					     jint key, jstring path_binary)
{

  const char *n_path_binary = (*env)->GetStringUTFChars (env, path_binary, 0);
  char cipckey[25];
    char port[25];
  int pid;
  switch (pid = fork ())
    {
    case -1:
      return -1;
    case 0:
      sprintf (cipckey, "%d", key);
      if (execl (n_path_binary, n_path_binary, cipckey, NULL) != 0)
	{
	  perror ("execlp");
	  return -1;
	}
      break;

    }
  return shmid;
}



JNIEXPORT jint JNICALL   Java_org_kevoree_tools_nativeN_NativeJNI_stop (JNIEnv * env, jobject obj,
					    jint key)
{
  ctx = (Context *) ptr_mem_partagee;
  if(ctx !=NULL)
  {
    alive =0;
    Events      ev;
    ev.ev_type = EV_STOP;

    send_event_fifo(p_event_fifo,ev);

    shmdt (ptr_mem_partagee);	/* detach segment */
    /* Deallocate the shared memory segment.  */
    shmctl (shmid, IPC_RMID, 0);

      return 0;
  } else
  {
  return -1;
  }

}


JNIEXPORT jint JNICALL
Java_org_kevoree_tools_nativeN_NativeJNI_update (JNIEnv * env, jobject obj,
					      jint key)
{
  ctx = (Context *) ptr_mem_partagee;
  Events      ev;
  ev.ev_type = EV_UPDATE;
  send_event_fifo(p_event_fifo,ev);
}


JNIEXPORT jint JNICALL
Java_org_kevoree_tools_nativeN_NativeJNI_create_1input (JNIEnv * env, jobject obj, jint key,
						     jstring queue)
{
  const char *n_port_name = (*env)->GetStringUTFChars (env, queue, 0);
  int id_queue = init_queue ();
  ctx = (Context *) ptr_mem_partagee;
  strcpy (ctx->inputs[ctx->inputs_count].name, n_port_name);
  ctx->inputs[ctx->inputs_count].id = id_queue;
  ctx->inputs_count = ctx->inputs_count + 1;
  return   ctx->inputs_count-1;
}

JNIEXPORT jint JNICALL    Java_org_kevoree_tools_nativeN_NativeJNI_create_1output (JNIEnv * env,
						      jobject obj, jint key,
						      jstring queue)
{
  const char *n_port_name = (*env)->GetStringUTFChars (env, queue, 0);
  int id_queue = init_queue ();
  ctx = (Context *) ptr_mem_partagee;
  strcpy (ctx->outputs[ctx->outputs_count].name, n_port_name);
  ctx->outputs[ctx->outputs_count].id = id_queue;
  ctx->outputs_count = ctx->outputs_count + 1;
  return  ctx->outputs_count-1;
}

JNIEXPORT jint JNICALL Java_org_kevoree_tools_nativeN_NativeJNI_putPort (JNIEnv * env, jobject obj,  jint key, jint id_port,    jstring msg)
{
  ctx = (Context *) ptr_mem_partagee;

  if(ctx != NULL)
  {
     const char *n_value = (*env)->GetStringUTFChars (env, msg, 0);
      kmessage kmsg;
      kmsg.type = 1;
      strcpy (kmsg.value, n_value);

      if(enqueue (ctx->inputs[id_port].id, kmsg) == 0)
      {
             Events      ev;
             ev.ev_type = EV_PORT_INPUT;
             ev.id_port =   id_port;
             send_event_fifo(p_event_fifo,ev);

      }
       else
      {
      return -1;
      }
      return 0;

  } else
  {
  return -1;
  }
}


JNIEXPORT jint JNICALL Java_org_kevoree_tools_nativeN_NativeJNI_setDico  (JNIEnv * env, jobject obj,  jint key, jstring dicoName, jstring value)
{

   // todo dico

 }
