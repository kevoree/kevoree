// ===================================
// `tail -f` in Node.js and WebSockets
// ===================================
var http = require('http'),
    io = require('socket.io'),
    spawn = require('child_process').spawn,
    fs = require('fs');

var backlog_size = 1000000;

// initialization parameters
var port = 8000;
var log_dir = "/Users/duke/Desktop/node0/";
//var filter = undefined;
var nodeList = ["node0", "node1"];
for (var i = 0; i < process.argv.length; i++) {
    if (process.argv[i] != '') {
        if (process.argv[i].indexOf("=") != -1) {
            if (process.argv[i].substring(0, process.argv[i].indexOf("=")) == "port") {
                port = process.argv[i].substring(process.argv[i].indexOf("=") + 1, process.argv[i].length);
            } else if (process.argv[i].substring(0, process.argv[i].indexOf("=")) == "logDir") {
                log_dir = process.argv[i].substring(process.argv[i].indexOf("=") + 1, process.argv[i].length);
            }
        }
    }
}


// -- Node.js HTTP Server ----------------------------------------------------------
server = http.createServer(function (req, res) {
    if (req.url == "/") {
        res.writeHead(200, {'Content-Type':'text/html'});
        fs.readFile(__dirname + '/index.html',
            function (err, data) {
                res.write(data, 'utf8');
                res.end();
            });
    } else if (req.url == "/bootstrap.min.css") {
        res.writeHead(200, {'Content-Type':'text/css'});
        fs.readFile(__dirname + '/bootstrap.min.css',
            function (err, data) {
                res.write(data, 'utf8');
                res.end();
            });
    } else if (req.url == "/bootstrap-responsive.min.css") {
        res.writeHead(200, {'Content-Type':'text/css'});
        fs.readFile(__dirname + '/bootstrap-responsive.min.css',
            function (err, data) {
                res.write(data, 'utf8');
                res.end();
            });
    } else if (req.url == "/jquery-1.8.0.min.js") {
        res.writeHead(200, {'Content-Type':'application/js'});
        fs.readFile(__dirname + '/jquery-1.8.0.min.js',
            function (err, data) {
                res.write(data, 'utf8');
                res.end();
            });
    } else if (req.url == "/scaled500.png") {
        res.writeHead(200, {'Content-Type':'text/html'});
        fs.readFile(__dirname + '/scaled500.png',
            function (err, data) {
                res.write(data, 'utf8');
                res.end();
            });
    } else if (req.url.slice(0, "/nodes/".length) == "/nodes/") {
        res.writeHead(200, {'Content-Type':'text/html'});
        fs.readFile(__dirname + '/log.html',
            function (err, data) {
                res.write(data, 'utf8');
                res.end();
            });
    }

});
server.listen(port);

// -- Setup Socket.IO ---------------------------------------------------------
var io = io.listen(server);
io.set('log level', 2);

io.sockets.on('connection', function (client) {
    var filename;
    var tail;
//    client.json.send({ logs:logs });
    client.on("message", function (message) {
        if (message.nodeList) {
            client.json.send({nodeList:nodeList});
        }
        if (message.logs) {
            // look up the dir for logs
            fs.readdir(log_dir, function (err, files) {
                var logs = [];
                if (err) throw err;
                files = Array.prototype.sort.apply(files, []);
                for (var file in files) {
                    try {
                        if (fs.statSync(log_dir + files[file]).isFile() && files[file].slice(0, message.logs[0].length) == message.logs[0]) {
                            logs.push(files[file]);
                        }
                    } catch(e){
                        console.error(e)
                    }
                }
                console.log(logs);
                client.json.send({logs:logs});
            });
        }
        if (message.log) {
            // Stop watching the last file and send the new one

            if (tail) tail.kill();

            fs.unwatchFile(filename);
            filename = log_dir + message.log;
            client.json.send({filename:filename});

            client.json.send({clear:true});
            // send some back log
            fs.stat(filename, function (err, stats) {
                if (err) throw err;
                if (stats.size == 0) {
                    client.json.send({clear:true});
                    return;
                }
                var start = (stats.size > backlog_size) ? (stats.size - backlog_size) : 0;
                var stream = fs.createReadStream(filename, {start:start, end:stats.size});
                stream.addListener("data", function (lines) {
                    console.log("Lines=="+lines);
                    lines = lines.toString('utf-8');
                    lines = lines.slice(lines.indexOf("\n") + 1).split("\n");
                    client.json.send({ tail:lines});
                });
            });
            // watch the file now
            tail = spawn('tail', ['--follow=name', filename]);
            tail.stdout.on('data', function (lines) {
                console.log(lines);
                client.json.send({ tail:lines.toString('utf-8').split("\n") });
            });

            // stop watching the file
            client.on("disconnect", function () {
                tail.kill()
            });
        }
    });
});

console.log('Log Server running now at http://[HOSTNAME]:' + port + '/ in your browser');
