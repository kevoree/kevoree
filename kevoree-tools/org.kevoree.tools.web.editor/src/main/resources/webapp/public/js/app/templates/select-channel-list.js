define(['jadeRuntime'], function(jade) {
return function anonymous(locals) {
var buf = [];
var locals_ = (locals || {}),channels = locals_.channels;// iterate channels
;(function(){
  var $$obj = channels;
  if ('number' == typeof $$obj.length) {

    for (var i = 0, $$l = $$obj.length; i < $$l; i++) {
      var channel = $$obj[i];

buf.push("<label class=\"radio\"><input" + (jade.attrs({ 'type':('radio'), 'name':('channel-type-radios'), 'data-channel-id':(i), "class": [('channel-type-item')] }, {"type":true,"name":true,"data-channel-id":true})) + "/>" + (jade.escape((jade.interp = channel) == null ? '' : jade.interp)) + "</label>");
    }

  } else {
    var $$l = 0;
    for (var i in $$obj) {
      $$l++;      var channel = $$obj[i];

buf.push("<label class=\"radio\"><input" + (jade.attrs({ 'type':('radio'), 'name':('channel-type-radios'), 'data-channel-id':(i), "class": [('channel-type-item')] }, {"type":true,"name":true,"data-channel-id":true})) + "/>" + (jade.escape((jade.interp = channel) == null ? '' : jade.interp)) + "</label>");
    }

  }
}).call(this);
;return buf.join("");
};
});
