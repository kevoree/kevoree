#include "thirdparty/component.h" 


void in_pwm(void *input);
void dispatch(int port,int id_queue)
{
    kmessage *msg = NULL;
    do
    {
          msg = dequeue(id_queue);
          if(msg !=NULL)
          {
             switch(port)
             {			 case 0:
					 in_pwm(msg->value);
			 break;
                     }
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