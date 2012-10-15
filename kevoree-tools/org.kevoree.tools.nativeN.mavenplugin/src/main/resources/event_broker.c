
/**
 * Created by jed
 * User: jedartois@gmail.com
 * Date: 03/10/12
 * Time: 11:47
 */

#include "events_udp.h"
#include "events_tcp.h"


int count=0;

void notify_tcp(Events ev)
{
   printf("notify tcp %d \n",count);
   count++;
}


void notify_udp(Events ev)
{
   printf("notify udp %d \n",count);
   count++;
}
int main(void)
{

     EventBroker ev_tcp;
     ev_tcp.port = 8084;
     ev_tcp.dispatch = &notify_tcp;


          EventBroker ev_udp;
          ev_udp.port = 8085;
          ev_udp.dispatch = &notify_udp;

      //  createEventBroker_udp (&ev_udp);

     createEventBroker_tcp (&ev_tcp);


}
