/**
 * Created with IntelliJ IDEA.
 * User: Erwan.Daubert
 * Date: 18/10/12
 * Time: 10:00
 */

var json = null;
function updateForm () {
    var nodeType = jQuery("#nodeType").get(0);
    var index = nodeType.selectedIndex;
    var value = nodeType.options[index].value;

    cleanForm();

    if (value == "JavaSENode") {
        JavaSENode();
    } else {
        buildForm(value);
    }
    submitButton();
}

function cleanOption () {
    var nodeType = jQuery("#nodeType").get(0);
    var childNodes = jQuery("#nodeType > *");

    childNodes.each(function (index, c) {
        if (c != undefined && c.value != "JavaSENode") {
            nodeType.removeChild(c);
        }
    });
}

function addOption (id) {
    var nodeType = jQuery("#nodeType").get(0);
    var option = document.createElement('option');
    option.value = id;
    option.innerHTML = id;
    nodeType.appendChild(option);

}

function cleanForm () {
    var form = jQuery("#formNodeType").get(0);
    jQuery("#formNodeType > *").each(function (index, e) {
        if (e.id != "nodeTypeList") {
            form.removeChild(e);
        }
    });
}

function submitButton () {
    var divcontrolgroup = document.createElement('div');
    divcontrolgroup.className = "control-group";

    var label = document.createElement('label');
    label.className = "control-label";
    divcontrolgroup.appendChild(label);

    var divcontrols = document.createElement('div');
    divcontrols.className = "controls";
    divcontrolgroup.appendChild(divcontrols);

    var button = document.createElement('input');
    button.id = "submit";
    button.className = "btn";
    button.type = "submit";
    button.value = "Submit";
    divcontrols.appendChild(button);

    jQuery("#formNodeType").get(0).appendChild(divcontrolgroup);
}

function createControlGroup (id) {
    var controlgroup = document.createElement('div');
    controlgroup.className = "control-group";
    controlgroup.id = id + "ControlGroup";
    return controlgroup;
}

function createControlLabel (id, mandatory) {
    var label = document.createElement('label');

    if (mandatory) {
        label.className = "control-label mandatory";
    } else {
        label.className = "control-label";
    }
    label.id = id + "Label";
    label.innerHTML = id;
    label.for = id + "Input";
    return label;
}

function createControls (id) {
    var controls = document.createElement('div');
    controls.className = "controls";
    controls.id = id + "Controls";
    return controls;
}

function createSelectElement (id, optionList, defaultValue) {
    var select = document.createElement('select');
    select.id = id + "Input";
    select.value = defaultValue;

    for (var i = 0; i < optionList.length; i++) {
        var option = document.createElement('option');
        option.value = optionList[i];
        option.innerHTML = optionList[i];
        select.appendChild(option);
        if (optionList[i] === defaultValue) {
            select.selectedIndex = i;
        }
    }
    return select;
}

function createInputElement (id, defaultValue) {
    var input = document.createElement('input');
    input.id = id + "Input";

    if (defaultValue != undefined) {
        input.value = defaultValue;
        var default_value = defaultValue;
        $(input).focus(function () {
            if (input.value == default_value) {
                input.value = '';
            }
        });
        $(input).blur(function () {
            if (input.value == '') {
                input.value = default_value;
            }
        });
    }
    return input;

}

function buildForm (value) {
    var form = jQuery("#formNodeType").get(0);
    for (var i = 0, ii = json[value].length; i < ii; i++) {
        var attribute = JSON.parse(json[value][i]);
        var controlGroup = createControlGroup(attribute.name);
        form.appendChild(controlGroup);

        var mandatory = !attribute.optional;
        if (mandatory == undefined) {
            mandatory = true;
        }

        var label = createControlLabel(attribute.name, mandatory);
        controlGroup.appendChild(label);

        var controls = createControls(attribute.name);
        controlGroup.appendChild(controls);

        var input = null;
        if (attribute.values != undefined) {
            input = createSelectElement(attribute.name, attribute.values, attribute.defaultValue);
        } else {
            input = createInputElement(attribute.name, attribute.defaultValue);
        }
        controls.appendChild(input);
    }
}

function JavaSENode () {
    var form = jQuery("#formNodeType").get(0);

    var nameControlGroup = createControlGroup("name");
    form.appendChild(nameControlGroup);

    var name = createControlLabel("name", true);
    nameControlGroup.appendChild(name);

    var nameControls = createControls("name");
    nameControlGroup.appendChild(nameControls);

    var nameInput = createInputElement("name", "");
    nameControls.appendChild(nameInput);

    var logLevelControlGroup = createControlGroup("logLevel");
    form.appendChild(logLevelControlGroup);

    var logLevel = createControlLabel("logLevel");
    logLevelControlGroup.appendChild(logLevel);

    var logLevelControls = createControls("logLevel");
    logLevelControlGroup.appendChild(logLevelControls);

    var logLevelSelect = createSelectElement("logLevel", ["WARN", "INFO", "DEBUG", "ERROR", "ALL"], "INFO");
    logLevelControls.appendChild(logLevelSelect);

    var coreLogLevelControlGroup = createControlGroup("coreLogLevel");
    form.appendChild(coreLogLevelControlGroup);

    var coreLogLevel = createControlLabel("coreLogLevel");
    coreLogLevelControlGroup.appendChild(coreLogLevel);

    var coreLogLevelControls = createControls("coreLogLevel");
    coreLogLevelControlGroup.appendChild(coreLogLevelControls);

    var coreLogLevelSelect = createSelectElement("coreLogLevel", ["WARN", "INFO", "DEBUG", "ERROR", "ALL"], "WARN");
    coreLogLevelControls.appendChild(coreLogLevelSelect);
}

function addError (controls) {
    var error = document.createElement("span");
    error.className = "mandatory";
    error.innerHTML = "Please set this attribute";
    controls.appendChild(error);
}

function cleanError (controls) {
    var toRemove = jQuery("#" + controls.id + " > span");
    if (toRemove.length > 0) {
        controls.removeChild(toRemove.get(0));
    }
}

function checker (id) {
    var label = jQuery("#" + id + "Label").get(0);
    if (label.className.indexOf("mandatory") != -1) {
        var controls = jQuery("#" + id + "Controls").get(0);
        var value = jQuery("#" + id + "Input").get(0).value;
        cleanError(controls);
        if (value != undefined && value != null && value != "") {
            return true;
        } else {
            addError(controls);
            return false;
        }
    } else {
        return true;
    }

}

function checkForm () {
    var check = true;

    jQuery(".control-group").each(function (index, e) {
        if (e.id != "" && e.id != "nodeTypeList") {
            var id = jQuery("#" + e.id + "> .controls").get(0).id;
            check = check && checker(id.substring(0, id.length - "Controls".length));
        }
    });

    return check;
}

function getAttributes () {
    var nodeType = jQuery("#nodeType").get(0);
    var index = nodeType.selectedIndex;
    var type = nodeType.options[index].value;

    var json = {"request":"add", "type":type};
    jQuery(".control-group").each(function (index, e) {
        if (e.id != "" && e.id != "nodeTypeList") {
            var id = e.id.substring(0, e.id.length - "ControlGroup".length);
            var valueInput = jQuery("#" + id + "Input").get(0);
            if (valueInput.tagName == "INPUT") {
                var value = valueInput.value;
            } else if (valueInput.tagName == "SELECT") {
                var nodeType = jQuery("#" + id + "Input").get(0);
                var selectedIndex = nodeType.selectedIndex;
                value = nodeType.options[selectedIndex].value;
            }
            json[id] = value;
        }
    });
    return json;
}

jQuery(document).ready(function () {
    updateForm();
    jQuery.ajax({
        url:"{pattern}AddChild",
        type:"post",
        data:{"request":"list"},
        dataType:'json',
        success:function (response) {
            if (response.request == "list") {
                cleanOption();
                for (var i = 0; i < response.types.length; i++) {
                    addOption(response.types[i]);
                }
                json = response;
            } else {
                alert("Unable to use this message " + response);
            }
        }
    });

    jQuery('#nodeType').change(function () {
        updateForm();
    });

    jQuery('#formNodeType').submit(function () {
        if (checkForm()) {
            var jsonRequest = getAttributes();
            jQuery.ajax({
                url:"{pattern}AddChild",
                type:"post",
                data:jsonRequest,
                dataType:'json',
                timeout: 5000,
                success:function (response) {
                    if (response.code != "0") {
                        alert(response.message);
                    } else {
                        window.location = "{pattern}";
                    }
                },
                error:function (response) {
                    alert("Unable to Add the node" + response);
                }
            });
        }
        return false; // unable the browser to submit the form
    });
});

