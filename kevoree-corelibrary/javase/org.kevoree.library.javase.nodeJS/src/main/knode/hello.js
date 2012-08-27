var port = 8022
for(var i=0;i<process.argv.length;i++)
{
    if (process.argv[i] != ''){
        if(process.argv[i].indexOf("=") != -1){
            port = process.argv[i].substring(process.argv[i].indexOf("=")+1,process.argv[i].length);
        }
    }
}
console.log("NodeJS WebServer on "+port);

var http = require('http');
var server = http.createServer(function(req, res) {
    res.writeHead(200);
    res.end('Hello from NodeJS managed by Kevoree');
});
server.listen(port);