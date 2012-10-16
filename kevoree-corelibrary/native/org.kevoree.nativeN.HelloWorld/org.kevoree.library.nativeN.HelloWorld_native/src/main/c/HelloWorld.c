#include "HelloWorld.h"

extern void output_port(void *input);

char input_msg[1024];
int counter = 0;

/* @Port(name = "input_port") */
void input_port(void *input) {
// USE INPUT
    sprintf(input_msg,"Receive from java '%s' counter = %d \n",(char *)input,counter);

   output_port((char*)input_msg);
   counter++;

}

/*@Start*/
int start()
{
	//fprintf(stderr,"Component starting \n");

	while(1)
	{
	     output_port((char*)"Hello World");

	     usleep(1000*1000);
	}

return 0;
}

/*@Stop */
int stop()
{
   // fprintf(stderr,"Component stoping \n");
return 0;
}

/*@Update */
int update()
{
   // fprintf(stderr,"Component updating \n");
 return 0;
}
