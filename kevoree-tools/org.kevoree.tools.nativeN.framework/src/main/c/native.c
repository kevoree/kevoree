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
#include "../../../../org.kevoree.tools.nativeN.mavenplugin/src/main/resources/component.h"


Context * getContext(int key);

#define ERROR_MEMORY_SHARED -5

JavaVM *g_vm;
jobject g_obj;
jmethodID g_mid;


void destroyHashMapKDico()
{
   // todo
}


void   process (int ipc_key,char *queue, void *msg)
{

  JNIEnv *g_env;
  int getEnvStat = (*g_vm)->GetEnv (g_vm, (void **) &g_env, JNI_VERSION_1_6);

  if (getEnvStat == JNI_EDETACHED)
    {
      if ((*g_vm)->AttachCurrentThread (g_vm, (void **) &g_env, NULL) != 0)
	{
	  fprintf (stderr,"Failed to attach {%d} \n",ipc_key);
	}
    }
  else if (getEnvStat == JNI_OK)
    {
      //
    }
  else if (getEnvStat == JNI_EVERSION)
    {
      fprintf (stderr,"GetEnv: version not supported {%d} \n",ipc_key);
    }

  (*g_env)->CallVoidMethod (g_env, g_obj, g_mid,ipc_key,(*g_env)->NewStringUTF (g_env, queue), (*g_env)->NewStringUTF (g_env, msg));
  if ((*g_env)->ExceptionCheck (g_env))
    {
      (*g_env)->ExceptionDescribe (g_env);
    }
  (*g_vm)->DetachCurrentThread (g_vm);
}

/*
 todo call on event 
 date from native
*/
void *manage_callbacks(void *c)
{
    int i,j;
    Context *ctxN = getContext(((Context *)c)->ipc_key);

    #ifdef DEBUG
        fprintf(stderr,"Starting manage_callbacks {%d} {%d}\n",ctxN->ipc_key,ctxN->alive);
    #endif

    while(ctxN->alive== 1)
    {
          if(ctxN != NULL)
          {
              for(i=0;i<  ctxN->outputs_count;i++)
              {
                 for(j=0;j<getQnum(ctxN->outputs[i].id);j++)
                 {
                    kmessage *msg = dequeue(ctxN->outputs[i].id);
                    if(msg !=NULL)
                    {
                              process(ctxN->ipc_key,ctxN->outputs[i].name,msg->value);
                     }
                 }
              }
              usleep(1000);
         }
         else
         {
            fprintf(stderr,"shared memory is null \n");
         }
    }
    #ifdef DEBUG
         fprintf(stderr,"Closing callbacks ipc key {%d}  \n",ctxN->ipc_key);
     #endif
    pthread_exit(NULL);
}


JNIEXPORT jint JNICALL
JNI_OnLoad (JavaVM * jvm, void *reserved)
{
  g_vm = jvm;
  return JNI_VERSION_1_6;
}

JNIEXPORT jint JNICALL  Java_org_kevoree_tools_nativeN_NativeJNI_register (JNIEnv * env, jobject obj,jint key)
{
    pthread_t callbacks;
    Context *ctxN = getContext(key);
    ctxN->alive = 1;

    if(pthread_create (& callbacks, NULL,&manage_callbacks, ctxN) != 0)
    {
         fprintf(stderr,"create callback");
         return (jboolean)-1;
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
// "(Ljava/lang/String;Ljava/lang/String;)V")  (I)V
//http://docs.oracle.com/javase/1.5.0/docs/guide/jni/spec/types.html#wp276
  g_mid =       (*env)->GetMethodID (env, g_clazz, "dispatchEvent", "(ILjava/lang/String;Ljava/lang/String;)V");
  if (g_mid == NULL)
    {
      fprintf (stderr,"Unable to get method ref");
      return -1;

    }
  return 0;
}


 /*
   INIT SHARED MEM
 */
JNIEXPORT jint JNICALL Java_org_kevoree_tools_nativeN_NativeJNI_init (JNIEnv * env, jobject obj, jint key)
{

  void *ptr_mem_partagee;

  // create memory shared
  int shmid = shmget (key, sizeof (Context), IPC_CREAT | S_IRUSR | S_IWUSR);
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

  Context *ctxN = (Context *) ptr_mem_partagee;

  ctxN->pid = -1;
  ctxN->outputs_count = 0;
  ctxN->inputs_count = 0;
  ctxN->ipc_key =   key;
  ctxN->alive = 0;
  ctxN->pid_jvm = getpid ();

  char fifo_name[SIZE_FIFO];
  sprintf(fifo_name,"%d.fifo",key);
  strcpy (ctxN->p_event_fifo.name_pipe,fifo_name);


  fprintf (stderr,"Component started with ipc key {%d} and shared memory attached at address {%p}\n",key, ptr_mem_partagee);

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
  return 0;
}



JNIEXPORT jint JNICALL   Java_org_kevoree_tools_nativeN_NativeJNI_stop (JNIEnv * env, jobject obj,
					    jint key)
{
  int shmid =-1;
  Context *ctxN = getContext(key);
  if(ctxN !=NULL)
  {
    Events      ev;
    ev.ev_type = EV_STOP;
    send_event_fifo(ctxN->p_event_fifo,ev);
    ctxN->alive =0;
    shmid = shmget(ctxN->ipc_key,sizeof(Context), S_IRUSR | S_IWUSR );
    shmdt (ctxN);	/* detach segment */
    shmctl (shmid, IPC_RMID, 0);     /*   Deallocate the shared memory segment.  */

    return 0;
  } else
  {
    return ERROR_MEMORY_SHARED;
  }

}


JNIEXPORT jint JNICALL
Java_org_kevoree_tools_nativeN_NativeJNI_update (JNIEnv * env, jobject obj,
					      jint key)
{
   Context *ctxN = getContext(key);
    if(ctxN != NULL)
    {
        Events      ev;
        ev.ev_type = EV_UPDATE;
        return send_event_fifo(ctxN->p_event_fifo,ev);
    }
    else
    {
        return ERROR_MEMORY_SHARED;
    }
}


JNIEXPORT jint JNICALL
Java_org_kevoree_tools_nativeN_NativeJNI_create_1input (JNIEnv * env, jobject obj, jint key,
						     jstring queue)
{
  Context *ctxN = getContext(key);
  if(ctxN != NULL)
  {
       const char *n_port_name = (*env)->GetStringUTFChars (env, queue, 0);
       int id_queue = init_queue ();
       strcpy (ctxN->inputs[ctxN->inputs_count].name, n_port_name);
       ctxN->inputs[ctxN->inputs_count].id = id_queue;
       ctxN->inputs_count = ctxN->inputs_count + 1;
       #ifdef  DEBUG
         fprintf (stderr,"Input %d : %d  \n",id_queue,       ctxN->inputs_count-1);
       #endif
       return   ctxN->inputs_count-1;
  } else
  {
      fprintf (stderr,"Error shared memory create_1input");
    return ERROR_MEMORY_SHARED;
  }

}

JNIEXPORT jint JNICALL    Java_org_kevoree_tools_nativeN_NativeJNI_create_1output (JNIEnv * env,
						      jobject obj, jint key,
						      jstring queue)
{
  Context *ctxN = getContext(key);
  if(ctxN != NULL)
  {
     const char *n_port_name = (*env)->GetStringUTFChars (env, queue, 0);
     int id_queue = init_queue ();
     strcpy (ctxN->outputs[ctxN->outputs_count].name, n_port_name);
     ctxN->outputs[ctxN->outputs_count].id = id_queue;
     ctxN->outputs_count = ctxN->outputs_count + 1;
      return  ctxN->outputs_count-1;
  } else
  {
    fprintf (stderr,"Error shared memory create_1output");
    return ERROR_MEMORY_SHARED;
  }
}

JNIEXPORT jint JNICALL Java_org_kevoree_tools_nativeN_NativeJNI_putPort (JNIEnv * env, jobject obj,  jint key, jint id_port,jstring msg)
{
  Context *ctxN = getContext(key);
  if(ctxN != NULL)
  {
      const char *n_value = (*env)->GetStringUTFChars (env, msg, 0);
      #ifdef  DEBUG
      fprintf (stderr,"PutPort %d->%s  id queue = %d\n", id_port,n_value,ctxN->inputs[id_port].id);
      #endif
      kmessage kmsg;
      kmsg.type = 1;
      strcpy (kmsg.value, n_value);

      if(enqueue (ctxN->inputs[id_port].id, kmsg) == 0)
      {
         Events      ev;
         ev.ev_type = EV_PORT_INPUT;
         ev.id_port =   id_port;

         // notify
         send_event_fifo(ctxN->p_event_fifo,ev);
         return 0;
      } else
      {
           fprintf (stderr,"Error : enqueue %d->%s \n", id_port,n_value);
        return -1;
      }
  } else
  {
    return ERROR_MEMORY_SHARED;
  }
}


JNIEXPORT jint JNICALL Java_org_kevoree_tools_nativeN_NativeJNI_setDico  (JNIEnv * env, jobject obj,  jint key, jstring key_dico, jstring value_dico)
{
    Context *ctxN = getContext(key);
     if(ctxN != NULL)
     {
         const char *key_n = (*env)->GetStringUTFChars (env, key_dico, 0);
         const char *value_n = (*env)->GetStringUTFChars (env, value_dico, 0);
         Events      ev;
         ev.ev_type = EV_DICO_SET;
         strcpy(ev.key,key_n);
         strcpy(ev.value,value_n);
         send_event_fifo(ctxN->p_event_fifo,ev);
     } else
     {
        fprintf(stderr,"update kdico");
        return -1;
     }
 }