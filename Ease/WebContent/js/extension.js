var waitForExtension = true;
$(document).ready(function(){
	setTimeout(function(){
		waitForExtension = false;
		showExtensionPopup();
		if (!showExtensionPopup())
			$("#tutorial").addClass("myshow");
	},800);
	
	
});

function showExtensionPopup(){
	if (!($('#ease_extension').length)) {
        if(!waitForExtension){
        	$('#extension').addClass("myshow");
            return true;
        } else {
        	setTimeout(function(){
        		return showExtensionPopup();
        	},200);
        }
    } else {
    	if(getUserNavigator() == "Safari"){
        	if(!$('#ease_extension').attr("safariversion") || $('#ease_extension').attr("safariversion") !="2.2.2"){
        		$('#extension .title p').text("Update your extension");
        		$('#extension #download #line1').text("A new version of the extension is now available.");
        		$('#extension #download #line2').text("We added new features and made it faster !");
        		$('#extension #download button').text("Update Ease Extension");
        		$('#extension').addClass("myshow");
        		return true;
        	}
        }
    	return false;
    }
}

function sendEvent(obj) {
	if(testApp){
		if (!($(obj).hasClass('waitingLinkImage'))) {
	        var appId = $(obj).closest('.siteLinkBox').attr('id');
	        var link = $(obj).closest('.siteLinkBox').attr('link');
	        var logoImage = $(obj).find('.linkImage');
	        var json = new Object();
	        var event;
	        $(obj).addClass('waitingLinkImage');
	        $(obj).addClass('scaleinAnimation');
	        setTimeout(function() {
	            $(obj).removeClass("waitingLinkImage");
	            $(obj).removeClass('scaleinAnimation');
	        }, 1000);
	        if (typeof link !== typeof undefined && link !== false) {
	        } else {
	        	postHandler.post("AskInfo", {
	        		appId : appId
	        	}, function() {
	        	}, function(retMsg) {
	        		json.detail = JSON.parse(retMsg);
	        		event = new CustomEvent("Test", json);
	        		document.dispatchEvent(event);
	        	}, function(retMsg) {
	        		showAlertPopup(retMsg, true);
	        	}, 'text');
	        }
	    }
	} else {
    if (!($(obj).hasClass('waitingLinkImage'))) {
        var appId = $(obj).closest('.siteLinkBox').attr('id');
        var link = $(obj).closest('.siteLinkBox').attr('link');
        var logoImage = $(obj).find('.linkImage');
        var json = new Object();
        var event;
        
        //easeTracker.trackEvent("App clicks");

        if(showExtensionPopup())
        	return;
        else {
        $(obj).addClass('waitingLinkImage');
        $(obj).addClass('scaleinAnimation');
        setTimeout(function() {
            $(obj).removeClass("waitingLinkImage");
            $(obj).removeClass('scaleinAnimation');
        }, 1000);
        	postHandler.post("AskInfo", {
        		appId : appId,
        	}, function() {
        	}, function(retMsg) {
        		json.detail = JSON.parse(retMsg);
        		var message = "NewConnection";
        		//easeTracker.trackEvent("ClickOnApp");
        		if(json.detail[0] && json.detail[0].url){
        			json.detail = json.detail[0];
        			message = "NewLinkToOpen";
        			
        			easeTracker.trackEvent("ClickOnApp", {"type":"LinkApp", "appName":json.detail.app_name});
        		} else {
        			var jsonDetail = json.detail[json.detail.length - 1];
        			easeTracker.trackEvent("ClickOnApp", {"type":jsonDetail.type, "appName":jsonDetail.app_name, "websiteName": jsonDetail.wbesite_name});
        		}
        		var now = "" + new Date;
        		easeTracker.setOnce("TutoDateFirstClickOnApp", now);
                json.detail.highlight = !ctrlDown;
        		event = new CustomEvent(message, json);
        		document.dispatchEvent(event);
        	}, function(retMsg) {
        		//easeTracker.trackEvent("App fail clicks");
        		showAlertPopup(retMsg, true);
        	}, 'text');
        }
    	}
	}
}

$(document).ready(function() {
	 $('#homePageSwitch').change(function() {
		var homepageState = $(this).is(":checked");
		easeTracker.setHomepage(homepageState);
		easeTracker.trackEvent("HomepageSwitch");
		var stateString = homepageState.toString();
		postHandler.post("HomepageSwitch", {
			homepageState: stateString
		}, function() {
			
		}, function(data) {
			
		}, function(date) {
			
		});
	 });
	
    // init extension popup 
    $("#chrome button[type='submit'], #safari button[type='submit']").click(function(){
        window.location = "/";
    });
    $("#extension #download #showExtensionInfo").click(function(){
        $('#extension #step1').removeClass('show');
        $('#extension #extensionInfo').addClass('show');
    });
    $("#extension #extensionInfo button").click(function(){
        $('#extension #extensionInfo').removeClass('show');     
        $('#extension #step1').addClass('show');
    });
    $("#extension #download button[type='submit']").click(function(){
        $("#extension #step1 #download").removeClass('show');
        if (NavigatorName == "Chrome"){
            $("#extension #step1 #chrome").addClass('show');
            chrome.webstore.install(
                'https://chrome.google.com/webstore/detail/echjdhmhmgildgidlcdlepfkaledeokm',
                function() {
                    //do nothing
                },
                function() {
                    //do nothing
                });
        } else if (NavigatorName == "Safari"){
            $("#extension #step1 #safari").addClass('show');
            window.location.replace(location.protocol + '//' + location.hostname+"/safariExtension/EaseExtension.safariextz");
        } else {
            $("#extension #step1 #other").addClass('show');
        }
    });
});
