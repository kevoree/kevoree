var app = require('http').createServer(handler)
    , io = require('socket.io').listen(app)
    , fs = require('fs');
var stnode = require('node-static');
var file = new (stnode.Server)('static', {
    cache:600,
    headers:{ 'X-Powered-By':'node-static' }
});

app.listen(8080);

function handler(req, res) {

    if (req.url == "/") {
        fs.readFile(__dirname + '/static/smoothie.html',
            function (err, data) {
                if (err) {
                    res.writeHead(500);
                    return res.end('Error loading smoothie.html');
                }

                res.writeHead(200);
                res.end(data);
            });
    } else {
        file.serve(req, res, function (err, result) {
            if (err) {
                console.error('Error serving %s - %s', req.url, err.message);
                if (err.status === 404 || err.status === 500) {
                    //res.writeHead()
                    //file.serveFile(util.format('/%d.html', err.status), err.status, {}, req, res);
                    res.end();
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

io.sockets.on('connection', function (socket) {
    setInterval(function() {
    socket.emit('data',Math.random());
    },1000);
    setInterval(function() {
        socket.emit('data2',Math.random());
    },500);
    socket.on('my other event', function (data) {
        console.log(data);
    });
});