define(['jadeRuntime'], function(jade) {
return function anonymous(locals) {
var buf = [];
var locals_ = (locals || {}),name = locals_.name,attrsHTML = locals_.attrsHTML,nodes = locals_.nodes;buf.push("<div class=\"row-fluid\"><div class=\"row-fluid\"><div class=\"span4\">Instance name</div><input" + (jade.attrs({ 'id':('instance-attr-name'), 'type':('text'), 'placeholder':('Name'), 'value':(name), "class": [('span8')] }, {"type":true,"placeholder":true,"value":true})) + "/></div>" + (null == (jade.interp = attrsHTML) ? "" : jade.interp) + "</div>");
if ( nodes.length > 0)
{
buf.push("<div class=\"row-fluid\"><div class=\"well\"><ul class=\"nav nav-tabs\">");
// iterate nodes
;(function(){
  var $$obj = nodes;
  if ('number' == typeof $$obj.length) {

    for (var i = 0, $$l = $$obj.length; i < $$l; i++) {
      var node = $$obj[i];

buf.push("<li" + (jade.attrs({ "class": [((i==0)?'active':null)] }, {"class":true})) + "><a" + (jade.attrs({ 'href':('#node-props-'+node.name), 'data-toggle':('tab') }, {"href":true,"data-toggle":true})) + ">" + (jade.escape((jade.interp = node.name) == null ? '' : jade.interp)) + "</a></li>");
    }

  } else {
    var $$l = 0;
    for (var i in $$obj) {
      $$l++;      var node = $$obj[i];

buf.push("<li" + (jade.attrs({ "class": [((i==0)?'active':null)] }, {"class":true})) + "><a" + (jade.attrs({ 'href':('#node-props-'+node.name), 'data-toggle':('tab') }, {"href":true,"data-toggle":true})) + ">" + (jade.escape((jade.interp = node.name) == null ? '' : jade.interp)) + "</a></li>");
    }

  }
}).call(this);

buf.push("</ul><div class=\"tab-content\">");
// iterate nodes
;(function(){
  var $$obj = nodes;
  if ('number' == typeof $$obj.length) {

    for (var i = 0, $$l = $$obj.length; i < $$l; i++) {
      var node = $$obj[i];

buf.push("<div" + (jade.attrs({ 'id':('node-props-'+node.name), "class": [('tab-pane'),((i==0)?'active':null)] }, {"class":true,"id":true})) + ">" + (null == (jade.interp = node.fragDepAttrsHTML) ? "" : jade.interp) + "</div>");
    }

  } else {
    var $$l = 0;
    for (var i in $$obj) {
      $$l++;      var node = $$obj[i];

buf.push("<div" + (jade.attrs({ 'id':('node-props-'+node.name), "class": [('tab-pane'),((i==0)?'active':null)] }, {"class":true,"id":true})) + ">" + (null == (jade.interp = node.fragDepAttrsHTML) ? "" : jade.interp) + "</div>");
    }

  }
}).call(this);

buf.push("</div></div></div>");
};return buf.join("");
};
});
