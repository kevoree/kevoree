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
 #ifndef EVENTS_COMMON_H
 #define EVENTS_COMMON_H

#include "settings.h"

 typedef enum
   {
 	EV_STOP,
    EV_UPDATE,
 	EV_PORT_INPUT,
 	EV_PORT_OUTPUT,
 	EV_DICO_SET
   } Type;

 typedef struct Events
 {
     Type ev_type;
     int id_port;
     char key[SIZE_FIFO_KEY];
     char value[SIZE_FIFO_VALUE];
 } Events;

 typedef struct _EventBroker {
 	char name_pipe[SIZE_FIFO_NAME];	/* named pipe */
 	/* DISPACHER */
 	void (*dispatch)(Events ev);
 } EventBroker;


 typedef struct _Publisher {
   char name_pipe[SIZE_FIFO_NAME];   /* named pipe */
 } Publisher;


 #endif