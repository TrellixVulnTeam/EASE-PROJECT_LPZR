function refresh() {
	postHandler.post('checkConnection',
			{},
			function(){},
			function(retMsg) {}, 
			function(retMsg) {
				mixpanel.track("Session lost");
				window.location.replace("index.jsp");
			},
			'text'
		);
	setTimeout(refresh, 45*1000);
 /* $.ajax({
    url: 'index.jsp'
  }).success(function() {
    setTimeout(refresh, 60*60*1000);
  });*/
}

$(document).ready(function() {
  refresh();
})
