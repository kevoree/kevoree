var kevs = require('./../../../target/parsers/javascript/parser'),
    fs   = require('fs'),
    path = require('path');

fs.readFile(path.resolve(__dirname, '..', '..', 'examples', 'attrs.kevs'), 'utf8', function (err, data) {
  if (err) throw err;

  var parser = new kevs.Parser();
  var ast = parser.parse(data);
  console.log(ast.toString());
});
