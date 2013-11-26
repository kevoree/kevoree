define(['jadeRuntime'], function(jade) {
return function anonymous(locals) {
var buf = [];
var locals_ = (locals || {}),libz = locals_.libz;// iterate libz
;(function(){
  var $$obj = libz;
  if ('number' == typeof $$obj.length) {

    for (var $index = 0, $$l = $$obj.length; $index < $$l; $index++) {
      var lib = $$obj[$index];

buf.push("<ul class=\"nav nav-list\"><li class=\"nav-header cursor-pointer lib-tree-library\"><i class=\"lib-subtree-icon icon-arrow-right icon-white\"></i>" + (jade.escape((jade.interp = lib.name) == null ? '' : jade.interp)) + "</li>");
// iterate lib.components
;(function(){
  var $$obj = lib.components;
  if ('number' == typeof $$obj.length) {

    for (var $index = 0, $$l = $$obj.length; $index < $$l; $index++) {
      var item = $$obj[$index];

if (item.type != 'UnknownType')
{
buf.push("<li" + (jade.attrs({ 'data-entity':(item.type), "class": [('lib-item')] }, {"data-entity":true})) + "><div class=\"lib-item-name\">" + (jade.escape((jade.interp = item.name) == null ? '' : jade.interp)) + "</div></li>");
}
    }

  } else {
    var $$l = 0;
    for (var $index in $$obj) {
      $$l++;      var item = $$obj[$index];

if (item.type != 'UnknownType')
{
buf.push("<li" + (jade.attrs({ 'data-entity':(item.type), "class": [('lib-item')] }, {"data-entity":true})) + "><div class=\"lib-item-name\">" + (jade.escape((jade.interp = item.name) == null ? '' : jade.interp)) + "</div></li>");
}
    }

  }
}).call(this);

buf.push("</ul>");
    }

  } else {
    var $$l = 0;
    for (var $index in $$obj) {
      $$l++;      var lib = $$obj[$index];

buf.push("<ul class=\"nav nav-list\"><li class=\"nav-header cursor-pointer lib-tree-library\"><i class=\"lib-subtree-icon icon-arrow-right icon-white\"></i>" + (jade.escape((jade.interp = lib.name) == null ? '' : jade.interp)) + "</li>");
// iterate lib.components
;(function(){
  var $$obj = lib.components;
  if ('number' == typeof $$obj.length) {

    for (var $index = 0, $$l = $$obj.length; $index < $$l; $index++) {
      var item = $$obj[$index];

if (item.type != 'UnknownType')
{
buf.push("<li" + (jade.attrs({ 'data-entity':(item.type), "class": [('lib-item')] }, {"data-entity":true})) + "><div class=\"lib-item-name\">" + (jade.escape((jade.interp = item.name) == null ? '' : jade.interp)) + "</div></li>");
}
    }

  } else {
    var $$l = 0;
    for (var $index in $$obj) {
      $$l++;      var item = $$obj[$index];

if (item.type != 'UnknownType')
{
buf.push("<li" + (jade.attrs({ 'data-entity':(item.type), "class": [('lib-item')] }, {"data-entity":true})) + "><div class=\"lib-item-name\">" + (jade.escape((jade.interp = item.name) == null ? '' : jade.interp)) + "</div></li>");
}
    }

  }
}).call(this);

buf.push("</ul>");
    }

  }
}).call(this);
;return buf.join("");
};
});
