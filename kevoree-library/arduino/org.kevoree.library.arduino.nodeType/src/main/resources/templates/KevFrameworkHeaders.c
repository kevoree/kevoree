
     #include <QueueList.h> 
     #include <avr/pgmspace.h> 
     #include <avr/wdt.h>
     #define UDI_C 0 
     #define AIN_C 1 
     #define RIN_C 2 
     #define ABI_C 3 
     #define RBI_C 4 
     #define kprint(x) KevSerialPrint_P(PSTR(x)) 
     #define kprintln(x) KevSerialPrintln_P(PSTR(x)) 
     #define KNOINLINE __attribute__((noinline))
     #define BUFFERSIZE 100
     int serialIndex = 0;
     char inBytes[BUFFERSIZE];
     const char startBAdminChar = '{';
     const char endAdminChar = '}';
     const char startAdminChar = '$';
     const char sepAdminChar = '/';


     #define MSGMAXSIZE 15
     #define MSGBUFFERSIZE 4
     int currentInstanceID = -1;
     char msgBytes[MSGBUFFERSIZE][MSGMAXSIZE];
     int currentMsgBufIndex = -1;
     int currentMsgIndex = -1;
     boolean messageInProgress = false;
     boolean instanceNameRead = false;


