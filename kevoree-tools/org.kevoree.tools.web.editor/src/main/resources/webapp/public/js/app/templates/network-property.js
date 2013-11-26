define(['jadeRuntime'], function(jade) {
return function anonymous(locals) {
var buf = [];
var locals_ = (locals || {}),id = locals_.id,link = locals_.link,key = locals_.key,value = locals_.value;buf.push("<div" + (jade.attrs({ 'data-net-prop-id':(id), "class": [('row-fluid'),('network-property-row')] }, {"data-net-prop-id":true})) + "><div class=\"row-fluid\"><div class=\"control-group span3\"><input" + (jade.attrs({ 'id':('network-property-key-'+link.id+'-'+id), 'type':('text'), 'placeholder':('IP'), 'value':(key) }, {"id":true,"type":true,"placeholder":true,"value":true})) + "/></div><div class=\"control-group span7 offset2\"><input" + (jade.attrs({ 'id':('network-property-val-'+link.id+'-'+id), 'type':('text'), 'placeholder':('192.168.0.1'), 'value':(value) }, {"id":true,"type":true,"placeholder":true,"value":true})) + "/></div></div><div" + (jade.attrs({ 'id':('network-property-err-'+link.id+'-'+id), "class": [('row-fluid'),('hide')] }, {"id":true})) + "><small class=\"text-error\"></small></div></div>");;return buf.join("");
};
});
