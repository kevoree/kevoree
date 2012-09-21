var httpProxy = require('http-proxy');

var vars = [];
for(var i=0;i<process.argv.length;i++){
    if (process.argv[i] != ''){
        if(process.argv[i].indexOf("=") != -1){
            var argName = process.argv[i].substring(0,process.argv[i].indexOf("="));
            vars[argName] = process.argv[i].substring(process.argv[i].indexOf("=")+1,process.argv[i].length);
            console.log("Arg "+argName+"->"+vars[argName]);
        }
    }
}


var ip = vars["ip"];
var port = parseInt(vars["port"]);
var remotePort = parseInt(vars["remotePort"]);

console.log("Proxy "+port+"->http://"+ip+":"+remotePort);
httpProxy.createServer(remotePort, ip).listen(port);