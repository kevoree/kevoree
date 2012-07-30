#include <QueueList.h>
#include <avr/pgmspace.h>
#include <avr/wdt.h>

#define kprint(x) KevSerialPrint_P(PSTR(x))
#define kprintln(x) KevSerialPrintln_P(PSTR(x))
#define KNOINLINE __attribute__((noinline))
#define BUFFERSIZE 100
#define MAX_UNTYPED_DICTIONARY 20

//transformation to modify the runtime    example : $4{0:T1:period=100}
const char UDI_C = '0';
const char AIN_C = '1';
const char RIN_C = '2';
const char ABI_C = '3';
const char RBI_C = '4';

int serialIndex = 0;
char inBytes[BUFFERSIZE];
const char startBAdminChar = '{';
const char endAdminChar = '}';
const char startAdminChar = '$';
const char sepAdminChar = '/';
const char delimitation = ':';

#define MSGMAXSIZE 15
#define MSGBUFFERSIZE 4
int currentInstanceID = -1;
char msgBytes[MSGBUFFERSIZE][MSGMAXSIZE];
int currentMsgBufIndex = -1;
int currentMsgIndex = -1;
boolean messageInProgress = false;
boolean instanceNameRead = false;
 //declare reset function @ address 0
void(* reset) (void) = 0;