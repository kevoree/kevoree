var kevs = require('./../../../target/parsers/javascript/parser'),
    fs   = require('fs'),
    path = require('path');

fs.readFile(path.resolve(process.cwd(), process.argv[2]), 'utf8', function (err, data) {
  if (err) throw err;

  var parser = new kevs.Parser();
  var ast = parser.parse(data);
  console.log(ast.toString());
});