 #ifndef EVENTS_COMMON_H
 #define EVENTS_COMMON_H

 typedef enum
   {
 	EV_STOP,
     EV_UPDATE,
 	EV_PORT_INPUT,
 	EV_PORT_OUTPUT
   } Type;

 typedef struct Events
 {
     Type ev_type;
     int id_port;
 } Events;

 typedef struct _EventBroker {
 	unsigned short  port; /* Port */
 	int sckServer; /* descripteur socket  Serveur*/
 	int sckClient; /* Descripteur du socket du client */
 	void (*dispatch)(Events ev);
 	struct sockaddr_in client;
 } EventBroker;


 typedef struct _Publisher {
 	int socket; /* point rdv */
 	unsigned short 	     port; /* Port */
 	struct hostent*      hp ;
 	struct sockaddr_in   adr ;
 	char hostname[512];
   	socklen_t            lgradr ;
 } Publisher;


 #endif