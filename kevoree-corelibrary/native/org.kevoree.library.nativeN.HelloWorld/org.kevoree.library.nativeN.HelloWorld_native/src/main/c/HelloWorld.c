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
