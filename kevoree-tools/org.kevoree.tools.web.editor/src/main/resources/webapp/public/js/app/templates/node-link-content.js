define(['jadeRuntime'], function(jade) {
return function anonymous(locals) {
var buf = [];
var locals_ = (locals || {}),id = locals_.id,isActive = locals_.isActive,type = locals_.type,minRate = locals_.minRate,maxRate = locals_.maxRate,rate = locals_.rate,props = locals_.props;buf.push("<div" + (jade.attrs({ 'id':('node-link-'+id), 'data-node-link-id':(id), "class": [('tab-pane'),((isActive)?'active':null)] }, {"class":true,"id":true,"data-node-link-id":true})) + "><div class=\"row-fluid\"><div class=\"input-prepend span4\"><span class=\"add-on add-on-gray\">Type</span><input" + (jade.attrs({ 'id':('node-link-type-'+id), 'type':('text'), 'placeholder':('LAN, Wifi...'), 'value':(type), "class": [('input-medium')] }, {"id":true,"type":true,"placeholder":true,"value":true})) + "/></div><div class=\"input-prepend offset4 span3\"><span class=\"add-on add-on-gray\">Rate</span><input" + (jade.attrs({ 'id':('node-link-rate-'+id), 'type':('number'), 'min':(minRate), 'max':(maxRate), 'placeholder':('Trust 0~100'), 'value':(rate), "class": [('input-small')] }, {"id":true,"type":true,"min":true,"max":true,"placeholder":true,"value":true})) + "/></div></div><div class=\"row-fluid\"><div id=\"network-properties-container\" class=\"well\"><h5><span class=\"span4\">Network properties</span><div class=\"btn-group span2 offset6\"><button" + (jade.attrs({ 'id':('network-property-delete-'+id), "class": [('btn'),('btn-danger'),('btn-mini'),('disabled')] }, {"id":true})) + "><i class=\"icon-trash icon-white\"></i></button><button" + (jade.attrs({ 'id':('network-property-add-'+id), "class": [('btn'),('btn-info'),('btn-mini')] }, {"id":true})) + "><i class=\"icon-plus icon-white\"></i></button></div></h5><div" + (jade.attrs({ 'id':('network-property-list-'+id), "class": [('row-fluid')] }, {"id":true})) + ">");
// iterate props
;(function(){
  var $$obj = props;
  if ('number' == typeof $$obj.length) {

    for (var $index = 0, $$l = $$obj.length; $index < $$l; $index++) {
      var prop = $$obj[$index];

buf.push(null == (jade.interp = prop.HTML) ? "" : jade.interp);
    }

  } else {
    var $$l = 0;
    for (var $index in $$obj) {
      $$l++;      var prop = $$obj[$index];

buf.push(null == (jade.interp = prop.HTML) ? "" : jade.interp);
    }

  }
}).call(this);

buf.push("</div></div></div></div>");;return buf.join("");
};
});
