#include <stdio.h>
#include <pthread.h>

#include "wiringPi.h"
#include "softPwm.h"
#include <sched.h>
#include <string.h>
#define	MAX_PINS	64

// The PWM Frequency is derived from the "pulse time" below. Essentially,
//	the frequency is a function of the range and this pulse time.
//	The total period will be range * pulse time in uS, so a pulse time
//	of 100 and a range of 100 gives a period of 100 * 100 = 10,000 uS
//	which is a frequency of 100Hz.
//
//	It's possible to get a higher frequency by lowering the pulse time,
//	however CPU uage will skyrocket as wiringPi uses a hard-loop to time
//	periods under 100uS - this is because the Linux timer calls are just
//	accurate at all, and have an overhead.
//
//	Another way to increase the frequency is to reduce the range - however
//	that reduces the overall output accuracy...

#define	PULSE_TIME	100

static int marks [MAX_PINS] ;
static int range [MAX_PINS] ;

int newPin = -1 ;


/*
 * piHiPri:
 *	Attempt to set a high priority schedulling for the running program
 *********************************************************************************
 */

int piHiPri (int pri)
{
  struct sched_param sched ;

  memset (&sched, 0, sizeof(sched)) ;

  if (pri > sched_get_priority_max (SCHED_RR))
    pri = sched_get_priority_max (SCHED_RR) ;

  sched.sched_priority = pri ;
  return sched_setscheduler (0, SCHED_RR, &sched) ;
}


/*
 * softPwmThread:
 *	Thread to do the actual PWM output
 *********************************************************************************
 */

static PI_THREAD (softPwmThread)
{
  int pin, mark, space ;

  pin    = newPin ;
  newPin = -1 ;

  piHiPri (50) ;

  for (;;)
  {
    mark  = marks [pin] ;
    space = range [pin] - mark ;

    if (mark != 0)
      digitalWrite (pin, HIGH) ;
    delayMicroseconds (mark * 100) ;

    if (space != 0)
      digitalWrite (pin, LOW) ;
    delayMicroseconds (space * 100) ;
  }

  return NULL ;
}


/*
 * softPwmWrite:
 *	Write a PWM value to the given pin
 *********************************************************************************
 */

void softPwmWrite (int pin, int value)
{
  pin &= 63 ;

  /**/ if (value < 0)
    value = 0 ;
  else if (value > range [pin])
    value = range [pin] ;

  marks [pin] = value ;
}


/*
 * softPwmCreate:
 *	Create a new PWM thread.
 *********************************************************************************
 */

int softPwmCreate (int pin, int initialValue, int pwmRange)
{
  int res ;

  pinMode      (pin, OUTPUT) ;
  digitalWrite (pin, LOW) ;

  marks [pin] = initialValue ;
  range [pin] = pwmRange ;

  newPin = pin ;
  res = piThreadCreate (softPwmThread) ;

  while (newPin != -1)
    delay (1) ;

  return res ;
}