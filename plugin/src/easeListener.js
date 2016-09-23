var isHere = $('#ease_extension').get();
if (!isHere[0]) {
    $('body').prepend('<div id="ease_extension" style="dislay:none;">');
}

document.addEventListener("isConnected", function(event){
    if(event.detail==true){
        extension.storage.set("isConnected","true", function(){});
    } else {
        extension.storage.set("isConnected","false", function(){});
    }
}, false);

document.addEventListener("Logout", function(event){

    extension.runtime.sendMessage("Logout", null, function(response) {});

}, false);

document.addEventListener("NewConnection", function(event){
    extension.runtime.sendMessage("NewConnection", {"highlight":true, "detail":event.detail}, function(response) {});

}, false);

