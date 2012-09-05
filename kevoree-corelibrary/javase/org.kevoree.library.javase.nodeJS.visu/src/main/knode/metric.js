var http = require('http');
var sys = require('sys');
var fs = require('fs');
var stnode = require('node-static');
var file = new (stnode.Server)('static', {
    cache:600,
    headers:{ 'X-Powered-By':'node-static' }
});

http.createServer(function (req, res) {
    if (req.headers.accept && req.headers.accept == 'text/event-stream') {
        if (req.url == '/events') {
            sendSSE(req, res);
        } else {
            res.writeHead(404);
            res.end();
        }
    } else {

        if (req.url=="/") {
            res.writeHead(200, {'Content-Type':'text/html'});
            res.write(fs.readFileSync(__dirname + '/static/metric.html'));
            res.end();
        } else {
            file.serve(req, res, function (err, result) {
                if (err) {
                    console.error('Error serving %s - %s', req.url, err.message);
                    if (err.status === 404 || err.status === 500) {
                        file.serveFile(util.format('/%d.html', err.status), err.status, {}, req, res);
                    } else {
                        res.writeHead(err.status, err.headers);
                        res.end();
                    }
                } else {
                    console.log('%s - %s', req.url, res.message);
                }
            });
        }

    }

}).listen(8000);

function sendSSE(req, res) {
    res.writeHead(200, {
        'Content-Type':'text/event-stream',
        'Cache-Control':'no-cache',
        'Connection':'keep-alive'
    });

    var id = (new Date()).toLocaleTimeString();

    setInterval(function () {
        constructSSE(res, id, (new Date()).toLocaleTimeString());
    }, 5000);

    constructSSE(res, id, (new Date()).toLocaleTimeString());
    //res.end();
}


function constructSSE(res, id, data) {
    res.write('id: ' + id + '\n');
    res.write("data: " + data + '\n\n');
}
