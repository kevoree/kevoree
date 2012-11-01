function addError (controls, message) {
    var error = document.createElement("span");
    error.className = "mandatory";
    error.innerHTML = message;
    controls.appendChild(error);
}

function cleanError (controls) {
    var toRemove = jQuery("#" + controls.id + " > span");
    if (toRemove.length > 0) {
        controls.removeChild(toRemove.get(0));
    }
}

function checkForm () {
    var check = true;
// TODO
    var loginControls = jQuery(".controls:has(#loginInput)").get(0);
    var login = jQuery("#loginInput").get(0).value;
//    var passwordControls = jQuery(".controls:has(#loginInput)").get(0);
//    var password = jQuery("#passwordInput").get(0).value;
    var sshkeyControls = jQuery(".controls:has(#sshInput)").get(0);
    var sshKey = jQuery("#sshInput").get(0).value;

    cleanError(loginControls);
    if (login == "") {
        addError(loginControls, "Login must be set !");
        check = false;
    }
    /*cleanError(passwordControls);
    if (login == "") {

    }*/
    cleanError(sshkeyControls);
    if (sshKey.indexOf("\n") != -1 && sshKey.indexOf("\n") != sshKey.length -1) {
        addError(sshkeyControls, "multiple line is not allowed to define a public SSH key !");
        check = false;
    }

    return check;
}

function getAttributes () {
    var login = jQuery("#loginInput").get(0).value;
    var password = jQuery("#passwordInput").get(0).value;
    var sshKey = jQuery("#sshInput").get(0).value;

    if (sshKey.indexOf("\n") == sshKey.length -1) {
        sshKey = sshKey.substr(0, sshKey.length -1)
    }

    return {"request":"initialize", "login":login, "password":password, "sshKey":sshKey};
}

jQuery(document).ready(function () {
    jQuery('#formNodeType').submit(function () {
        if (checkForm()) {
            var jsonRequest = getAttributes();
            jQuery.ajax({
                url:"{pattern}InitializeUser",
                type:"post",
                data:jsonRequest,
                dataType:'json',
                success:function (response) {
                    if (response.code == "0") {
                        window.location = window.location + "/" + jQuery("#loginInput").get(0).value;
                    } else if (response.code == "1") {
                        alert("User configuration already exists!");
                        window.location = window.location + "/" + jQuery("#loginInput").get(0).value;
                    } else {
                        console.log(response);
                    }
                }
            });
        }
        return false; // unable the browser to submit the form
    });
});