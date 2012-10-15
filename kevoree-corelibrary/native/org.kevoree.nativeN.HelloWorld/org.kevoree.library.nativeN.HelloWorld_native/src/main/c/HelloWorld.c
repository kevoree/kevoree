#include "HelloWorld.h"

extern void output_port(void *input);

/* @Port(name = "input_port") */
void input_port(void *input) {
// USE INPUT


}

/*@Start*/
int start()
{
	fprintf(stderr,"Component starting \n");

	while(1){

	     output_port((char*)"Hello World 2");
	usleep(1000*1000);
	}

return 0;
}

/*@Stop */
int stop()
{
    fprintf(stderr,"Component stoping \n");
return 0;
}

/*@Update */
int update()
{
    fprintf(stderr,"Component updating \n");
 return 0;
}
