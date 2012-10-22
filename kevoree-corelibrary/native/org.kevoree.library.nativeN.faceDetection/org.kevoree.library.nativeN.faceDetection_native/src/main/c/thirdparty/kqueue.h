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

#ifndef QUEUE_H
#define QUEUE_H

#include<string.h>
#include<time.h>
#include<sys/ipc.h>
#include<sys/msg.h>
#include<sys/wait.h>
#include <stdio.h>
#include<sys/errno.h>


extern int errno;       // error NO.
#define MSGPERM 0777    // msg queue permission

typedef struct _kmsg
{
         long    type;
         char value[2048];
 } kmessage;


/*
struct msqid_ds {
               struct ipc_perm msg_perm;     Ownership and permissions
               time_t          msg_stime;    Time of last msgsnd(2)
               time_t          msg_rtime;     Time of last msgrcv(2)
               time_t          msg_ctime;    Time of last change
               unsigned long   __msg_cbytes; Current number of bytes in              queue (nonstandard)
               msgqnum_t       msg_qnum;    Current number of messages                                                 in queue
               msglen_t        msg_qbytes;    Maximum number of bytes       allowed in queue
               pid_t           msg_lspid;     PID of last msgsnd(2)
               pid_t           msg_lrpid;     PID of last msgrcv(2)
           };

*/
int init_queue();

int init_queue()
{
   // int rc;
    int msgqid = msgget(IPC_PRIVATE, MSGPERM|IPC_CREAT|IPC_EXCL);
     if (msgqid < 0)
     {
       perror(strerror(errno));
       fprintf(stderr,"failed to create message queue with msgqid = %d\n", msgqid);
       return 1;
     }


      /*
          struct msqid_ds conf;
      1Mo = 1 000 000 o
      2Mo = 2 000 000 o
      5Mo = 5 000 000 o
      10Mo = 10 000 000 o

    conf.msg_qbytes =1024;

   rc=msgctl(msgqid,IPC_SET,&conf);
     if (rc < 0)
     {
       perror( strerror(errno) );
       fprintf(stderr,"msgctl (return queue) failed, rc=%d\n", rc);
       return -1;
     }
            */
  return msgqid;
}

int destroy_queue(int msgqid){
    int rc;
  rc=msgctl(msgqid,IPC_RMID,NULL);
  if (rc < 0) {
    perror( strerror(errno) );
    fprintf(stderr,"msgctl (return queue) failed, rc=%d\n", rc);
    return -1;
  }

  return 0;
}

int enqueue(int msgqid,kmessage msg)
{
    int rc;
    rc = msgsnd(msgqid, &msg, sizeof(msg.value), 0); // the last param can be: 0, IPC_NOWAIT, MSG_NOERROR, or IPC_NOWAIT|MSG_NOERROR.
    if (rc < 0) {
      perror( "enqueue" );
      fprintf(stderr,"msgsnd failed, rc = %d\n", rc);
      return -1;
    }
    return 0;
}

int getQnum(int msgqid)
{
  struct msqid_ds stats;
  int rc;
  rc=msgctl(msgqid,IPC_STAT,&stats);
  if (rc < 0)
  {
    perror( strerror(errno) );
    fprintf(stderr,"msgctl (return queue) failed, rc=%d\n", rc);
    return -1;
  }
  return stats.msg_qnum;
}

kmessage* dequeue(int msgqid)
{
   kmessage *msg = (kmessage*)malloc(sizeof(kmessage));

  int rc = msgrcv(msgqid, msg, sizeof(msg->value), 0,0);
    if (rc < 0)
    {
      perror( "dequeue" );
      fprintf(stderr,"msgrcv failed, rc=%d\n", rc);
      free(msg);
      return NULL;
    }
    return msg;
}

#endif