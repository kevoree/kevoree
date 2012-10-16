#include "thirdparty/component.h" 


void input_port(void *input);
void output_port(void *input) {
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
             {			 case 0:
					 input_port(msg->value);
			 break;
                     }
                     }

    } while(msg != NULL);
}
int main (int argc,char *argv[])
{

    printf("argc= %d \n",argc);
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
     } else
     {


         return -1;

     }
     return 0;
}