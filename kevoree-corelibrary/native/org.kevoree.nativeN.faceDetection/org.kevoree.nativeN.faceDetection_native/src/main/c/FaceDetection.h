#include "thirdparty/component.h" 


void faceDetected(void *input) {
 process_output(0,input);
}
void dispatch(int port,int id_queue)
{
    kmessage *msg = NULL;
    do
    {
          msg = dequeue(id_queue);
          if(msg !=NULL)
          {
             switch(port)
             {                     }
                     }

    } while(msg != NULL);
}int main (int argc,char *argv[])
{
   	if(argc >2)
    {
	    key_t key =   atoi(argv[1]);
	    int port=   atoi(argv[2]);

	     bootstrap(key,port);
        ctx->start= &start;
        ctx->stop = &stop;
        ctx->update   = &update;
        ctx->dispatch = &dispatch;
	    ctx->start();
    pause();
     }
}