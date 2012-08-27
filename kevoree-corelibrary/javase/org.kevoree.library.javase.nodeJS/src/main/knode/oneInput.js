

/* Look for port passed for Kevoree params */
var port = 8022
for(var i=0;i<process.argv.length;i++)
{
    if (process.argv[i] != ''){
        if(process.argv[i].indexOf("=") != -1){
            port = process.argv[i].substring(process.argv[i].indexOf("=")+1,process.argv[i].length);
        }
    }
}

var WebSocketServer = require('ws').Server
    , wss = new WebSocketServer({port: 99996});

wss.on('connection', function(ws) {
    //Noop
});

console.log("NodeJS WebServer on "+port);
var http = require('http');
var server = http.createServer(function(req, res) {
    if(req.url == "/"){
        if(gws){
            gws.onmessage = (function(rec){
                console.log("DaFuck"+rec);
                res.writeHead(200);
                res.end("Response accepted");
            });
            gws.send("hello");
        } else {
            res.writeHead(200);
            res.end("Sub Not connected !");
        }

    }
});
server.listen(port);