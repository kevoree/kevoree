#include "Pwm.h"
#include "softPwm.h"
#include <unistd.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>

#define RANGE		100

int fd=0;
int i, j ;
char buf [512] ;

/* @Port(name = "in_pwm") */
void in_pwm(void *input) {
// USE INPUT
    if(fd >0){
        sprintf(buf,"%s",(char*)input);
        write( fd  , buf , sizeof(buf) );
    }
}

/*@Start*/
int start()
{
	fprintf(stderr,"Component starting \n");


	if (wiringPiSetup () == -1)
    {
       fprintf (stderr, "oops: %s\n", strerror (errno)) ;
       return 1 ;
    }
    fd =open( "/dev/servojed" , O_RDWR );

	if ( fd < 0 )
	{
		/* opening error */
      fprintf(stderr,"ERROR:  could not open /dev/servojed!\n");
    }
return 0;
}

/*@Stop */
int stop()
{
    close(fd);
    fprintf(stderr,"Component stoping \n");
return 0;
}

/*@Update */
int update()
{
    fprintf(stderr,"Component updating \n");
 return 0;
}
