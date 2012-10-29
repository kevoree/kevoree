#include "thirdparty/component.h" 


void faceDetected(void *input) {
 process_output(0,input);
}
void dispatch(int port,int id_queue)
{
    kmessage *msg = NULL;
          msg = dequeue(id_queue);
          if(msg !=NULL)
          {
             switch(port)
             {                     }
                     }

}int main (int argc,char *argv[])
{
   	if(argc  > 1)
    {
	    key_t key =   atoi(argv[1]);
	   // int port=   atoi(argv[2]);

	     bootstrap(key,-1);
        ctx->start= &start;
        ctx->stop = &stop;
        ctx->update   = &update;
        ctx->dispatch = &dispatch;
	    ctx->start();
    pause();
     }
}