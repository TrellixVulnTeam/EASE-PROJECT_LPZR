function getSID() {
    var jsId = document.cookie.match(/sId=[^;]+/);
    if (jsId != null) {
        if (jsId instanceof Array)
            jsId = jsId[0].substring(4);
        else
            jsId = jsId.substring(4);
    }
    return jsId;
}


extension.storage.get("sessionId", function (oldSessionId) {
    var newSessionId = "";
    var cookies = document.cookie.split(';');
    /*for (var i = 0; i < cookies.length; i++) {
        if (cookies[i][0] = " ") {
            cookies[i] = cookies[i].substring(1, cookies[i].length - 1);
        }
        if (cookies[i].indexOf("sId") == 0) {
            newSessionId = cookies[i].substring(cookies[i].indexOf("=") + 1, cookies[i].length - 1);
	    console.log("new session id: " + newSessionId);
            break;
        }
	}*/
    newSessionId = getSID();
    extension.storage.set("sessionId", newSessionId, function () {});
    if (newSessionId != "" && newSessionId == oldSessionId) {
        extension.storage.get("storedUpdates", function (storedUpdates) {
            if (storedUpdates != undefined && storedUpdates.length > 0) {
                extension.storage.get("extensionId", function (eId) {
                    $.post("http://localhost:8080/CreateUpdates", {
                        "sessionId": newSessionId,
                        "updates": JSON.stringify(storedUpdates),
                        "extensionId": eId
                    }, function (resp) {
                        var res = resp.split(" ");
                        if (res[0] == "200") {
                            var indices = res;
                            indices.splice(0, 1);
                            var toStore = [];
                            for (var i = 0; i < storedUpdates; i++) {
                                if (!indices.includes(i))
                                    toStore.push(storedUpdates[i]);
                            }
                            extension.storage.set("storedUpdates", toStore, function () {});
                        }
                    });
                });
            }
        });
    }
});



document.addEventListener("askForExtensionId", function (event) {
    extension.storage.get("extensionId", function (extensionId) {
        document.dispatchEvent(new CustomEvent("extensionId", {
            id: extensionId
        }));
    });
});