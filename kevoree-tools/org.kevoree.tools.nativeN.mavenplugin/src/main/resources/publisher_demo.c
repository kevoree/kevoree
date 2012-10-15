

/**
 * Created by jed
 * User: jedartois@gmail.com
 * Date: 03/10/12
 * Time: 11:47
 */

#include "events_udp.h"
#include "events_tcp.h"

#include <strings.h>

int main(void)
{

   int i=0;

    Events      t;
    t.ev_type = EV_UPDATE;



  Publisher publisher_udp;
  publisher_udp.port = 8085;
  strcpy (publisher_udp.hostname, "127.0.0.1");


       for(i=0;i<50;i++)
       {
                     send_event_udp(publisher_udp,t);
       }




         /*
 Publisher publisher_tcp;
 publisher_tcp.port = 8084;
 strcpy (publisher_tcp.hostname, "127.0.0.1");


       send_event_tcp(publisher_tcp,t);

          send_event_tcp(publisher_tcp,t);

   send_event_tcp(publisher_tcp,t);

          */



}