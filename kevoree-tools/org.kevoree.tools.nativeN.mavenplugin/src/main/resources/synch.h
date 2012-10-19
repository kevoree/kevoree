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
#ifndef __SYNCH_H__
#define __SYNCH_H__


#include <stdlib.h>
#include <semaphore.h>

typedef void * semaphore_t;

semaphore_t semaphore_create();
void semaphore_initialize(semaphore_t p_semaphore, int count);
void semaphore_P(semaphore_t p_semaphore);
void semaphore_V(semaphore_t p_semaphore);
void semaphore_destroy(semaphore_t p_semaphore);

#endif /* SYNCH_H_ */

struct semaphore {
sem_t mutex;
};

semaphore_t semaphore_create() {
struct semaphore * sem = (struct semaphore *) malloc(
sizeof(struct semaphore));
return sem;
}
void semaphore_initialize(semaphore_t p_semaphore, int count) {
struct semaphore * sem = (struct semaphore *) p_semaphore;
if (p_semaphore != NULL)
sem_init(&sem->mutex, 0, count);
}
void semaphore_P(semaphore_t p_semaphore) {
struct semaphore * sem = (struct semaphore *) p_semaphore;
if (p_semaphore != NULL) {
sem_wait(&sem->mutex);
}
}

void semaphore_V(semaphore_t p_semaphore) {
struct semaphore * sem = (struct semaphore *) p_semaphore;
if (p_semaphore != NULL) {
sem_post(&sem->mutex);
}
}

void semaphore_destroy(semaphore_t p_semaphore) {
struct semaphore * sem = (struct semaphore *) p_semaphore;
if (p_semaphore != NULL) {
sem_destroy(&sem->mutex);
}
}