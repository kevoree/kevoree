/**
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 3, 29 June 2007;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Created by jed
 * User: jedartois@gmail.com
 * Date: 03/10/12
 * Time: 11:47
 */

#include "events_udp.h"
#include "events_tcp.h"
#include "events_fifo.h"

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

     strcpy(ev_tcp.name_pipe,"/tmp/test");

          EventBroker ev_udp;
          ev_udp.port = 8085;
          ev_udp.dispatch = &notify_udp;

      //  createEventBroker_udp (&ev_udp);



     createEventBroker_fifo (&ev_tcp);


}
