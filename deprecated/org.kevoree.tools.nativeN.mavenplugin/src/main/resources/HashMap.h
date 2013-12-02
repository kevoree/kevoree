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

#ifndef HASH_MAP__
#define HASH_MAP__
#include <stdarg.h>
#include <stdlib.h>
#include <string.h>
#include <stdio.h>

 typedef struct _Entry {
    const char *key;
    const char *value;
  } Entry;


struct HashMap {
  void (*destructor)(void *arg, char *key, char *value);
  void *arg;
  struct _Entry **entries;
  int  mapSize;
  int  numEntries;
};

struct HashMap *newHashMap();
void initHashMap(struct HashMap *hashmap);
void destroyHashMap(struct HashMap *hashmap);

void deleteHashMap(struct HashMap *hashmap);
const void *addToHashMap(struct HashMap *hashmap, const char *key,
                         const char *value);
void deleteFromHashMap(struct HashMap *hashmap, const char *key);
char **getRefFromHashMap(const struct HashMap *hashmap, const char *key);
const char *getFromHashMap(const struct HashMap *hashmap, const char *key);
void iterateOverHashMap(struct HashMap *hashmap,
                        int (*fnc)(void *arg, const char *key, char **value),
                        void *arg);
int getHashmapSize(const struct HashMap *hashmap);



void fatal(const char *fmt, ...) {
  va_list ap;
  va_start(ap, fmt);
  vfprintf(stderr, fmt, ap);
  fputs("\n", stderr);
  va_end(ap);
#ifdef CORE
  abort();
#else
  exit(1);
#endif
}

// Output a error message and exit program with a non-zero return code.
void fatal(const char *fmt, ...) __attribute__((format(printf, 1, 2),
                                                noreturn));
#ifndef NDEBUG
#define check_HASHMAP(x) do {                                                         \
                   if (!(x))                                                  \
                     fatal("Check failed at "__FILE__":%d in %s(): %s",       \
                            __LINE__, __func__, #x);                          \
                 } while (0)
#else
#define check_HASHMAP(x) do {                                                         \
                   if (!(x))                                                  \
                     fatal("Fatal error!\n");                                 \
                 } while (0)
#endif



void destroy(void *arg, char *key, char *value)
{
   // todo
}

struct HashMap *newHashMap() {
  struct HashMap *hashmap;
  check_HASHMAP(hashmap =( struct HashMap *) malloc(sizeof(struct HashMap)));
  initHashMap(hashmap);
  return hashmap;
}

void initHashMap(struct HashMap *hashmap) {
  hashmap->destructor  = &destroy;
  hashmap->arg         = NULL;
  hashmap->entries     = NULL;
  hashmap->mapSize     = 0;
  hashmap->numEntries  = 0;
}

void destroyHashMap(struct HashMap *hashmap)
{
int i,j;
  if (hashmap) {
    for (i = 0; i < hashmap->mapSize; i++) {
      if (hashmap->entries[i]) {
        for (j = 0; hashmap->entries[i][j].key; j++) {
          if (hashmap->destructor) {
            hashmap->destructor(hashmap->arg,
                                (char *)hashmap->entries[i][j].key,
                                (char *)hashmap->entries[i][j].value);
          }
        }
        free(hashmap->entries[i]);
      }
    }
    free(hashmap->entries);
  }
}

void deleteHashMap(struct HashMap *hashmap) {
  destroyHashMap(hashmap);
  free(hashmap);
}

static unsigned int stringHashFunc(const char *s) {
  unsigned int h = 0;
  while (*s) {
    h = 31*h + *(unsigned char *)s++;
  }
  return h;
}

const void *addToHashMap(struct HashMap *hashmap, const char *key,
                         const char *value) {
                         int i,j;
  if (hashmap->numEntries + 1 > (hashmap->mapSize * 8)/10) {
    struct HashMap newMap;
    newMap.numEntries            = hashmap->numEntries;
    if (hashmap->mapSize == 0) {
      newMap.mapSize             = 32;
    } else if (hashmap->mapSize < 1024) {
      newMap.mapSize             = 2*hashmap->mapSize;
    } else {
      newMap.mapSize             = hashmap->mapSize + 1024;
    }
    check_HASHMAP(newMap.entries   = (Entry**)calloc(sizeof(void *), newMap.mapSize));
    for (i = 0; i < hashmap->mapSize; i++) {
      if (!hashmap->entries[i]) {
        continue;
      }
      for (j = 0; hashmap->entries[i][j].key; j++) {
        addToHashMap(&newMap, hashmap->entries[i][j].key,
                     hashmap->entries[i][j].value);
      }
      free(hashmap->entries[i]);
    }
    free(hashmap->entries);
    hashmap->entries             = newMap.entries;
    hashmap->mapSize             = newMap.mapSize;
    hashmap->numEntries          = newMap.numEntries;
  }
  unsigned hash                  = stringHashFunc(key);
  int idx                        = hash % hashmap->mapSize;
  i                          = 0;
  if (hashmap->entries[idx]) {
    for (i = 0; hashmap->entries[idx][i].key; i++) {
      if (!strcmp(hashmap->entries[idx][i].key, key)) {
        if (hashmap->destructor) {
          hashmap->destructor(hashmap->arg,
                              (char *)hashmap->entries[idx][i].key,
                              (char *)hashmap->entries[idx][i].value);
        }
        hashmap->entries[idx][i].key   = key;
        hashmap->entries[idx][i].value = value;
        return value;
      }
    }
  }
  check_HASHMAP(hashmap->entries[idx]    = ((Entry*)realloc(hashmap->entries[idx],
                                        (i+2)*sizeof(*hashmap->entries[idx]))));
  hashmap->entries[idx][i].key   = key;
  hashmap->entries[idx][i].value = value;
  memset(&hashmap->entries[idx][i+1], 0, sizeof(*hashmap->entries[idx]));
  hashmap->numEntries++;
  return value;
}

void deleteFromHashMap(struct HashMap *hashmap, const char *key) {
  if (hashmap->mapSize == 0) {
    return;
  }
  int i,j;
  unsigned hash = stringHashFunc(key);
  int idx       = hash % hashmap->mapSize;
  if (!hashmap->entries[idx]) {
    return;
  }
  for ( i = 0; hashmap->entries[idx][i].key; i++) {
    if (!strcmp(hashmap->entries[idx][i].key, key)) {
      j     = i + 1;
      while (hashmap->entries[idx][j].key) {
        j++;
      }
      if (hashmap->destructor) {
        hashmap->destructor(hashmap->arg,
                            (char *)hashmap->entries[idx][i].key,
                            (char *)hashmap->entries[idx][i].value);
      }
      if (i != j-1) {
        memcpy(&hashmap->entries[idx][i], &hashmap->entries[idx][j-1],
               sizeof(*hashmap->entries[idx]));
      }
      memset(&hashmap->entries[idx][j-1], 0, sizeof(*hashmap->entries[idx]));
      check_HASHMAP(--hashmap->numEntries >= 0);
    }
  }
}

char **getRefFromHashMap(const struct HashMap *hashmap, const char *key) {
  if (hashmap->mapSize == 0) {
    return NULL;
  }
  int i;
  unsigned hash = stringHashFunc(key);
  int idx       = hash % hashmap->mapSize;
  if (!hashmap->entries[idx]) {
    return NULL;
  }
  for (i = 0; hashmap->entries[idx][i].key; i++) {
    if (!strcmp(hashmap->entries[idx][i].key, key)) {
      return (char **)&hashmap->entries[idx][i].value;
    }
  }
  return NULL;
}

const char *getFromHashMap(const struct HashMap *hashmap, const char *key) {
  char **ref = getRefFromHashMap(hashmap, key);
  return ref ? *ref : NULL;
}

void iterateOverHashMap(struct HashMap *hashmap,
                        int (*fnc)(void *arg, const char *key, char **value),
                        void *arg) {
                        int i,j;
  for (i = 0; i < hashmap->mapSize; i++) {
    if (hashmap->entries[i]) {
      int count = 0;
      while (hashmap->entries[i][count].key) {
        count++;
      }
      for (j = 0; j < count; j++) {
        if (!fnc(arg, hashmap->entries[i][j].key,
                 (char **)&hashmap->entries[i][j].value)) {
          if (hashmap->destructor) {
            hashmap->destructor(hashmap->arg,
                                (char *)hashmap->entries[i][j].key,
                                (char *)hashmap->entries[i][j].value);
          }
          if (j != count-1) {
            memcpy(&hashmap->entries[i][j], &hashmap->entries[i][count-1],
                   sizeof(*hashmap->entries[i]));
          }
          memset(&hashmap->entries[i][count-1], 0,
                 sizeof(*hashmap->entries[i]));
          count--;
          j--;
        }
      }
    }
  }
}

int getHashmapSize(const struct HashMap *hashmap) {
  return hashmap->numEntries;
}

#endif  // HASH_MAP__