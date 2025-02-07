<%@ page language="java" contentType="text/html; charset=UTF-8"
pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="language" value="${not empty param.language ? param.language : not empty language ? language : pageContext.request.locale}" scope="session" />
<c:if test="${language ne 'en' and language ne 'fr_FR'}">
	<c:set var="language" value="en" scope="session"/>
</c:if>
<fmt:setLocale value="${language}" />
<fmt:setBundle basename="com.Ease.Languages.text" />
<html lang="${language}">
<head>
	<title> Ease.space | Le meilleur moyen de se connecter à ses sites préférés.</title>
	<!-- Description shown in Google -->
	<meta name="description" content="Ease est la homepage qui vous débarrasse des mots de passe. En 1 clic, soyez connecté à vos sites web automatiquement." />
	<!-- Facebook metadata -->
	<meta property="og:url" content="https://ease.space/" />
	<meta property="og:title" content="Ease.space | Le meilleur moyen de se connecter à ses sites préférés." />
	<meta property="og:description" content="Ease est la homepage qui vous débarrasse des mots de passe. En 1 clic, soyez connecté à vos sites web automatiquement." />
	<meta property="og:image" content="https://ease.space/resources/images/fbmeta-fr.png" />
	<meta property="og:type" content="website" />
	<!-- Twitter metadata -->
	<meta name="twitter:card" content="summary_large_image" />
	<meta name="twitter:site" content="@Ease_app" />
	<meta name="twitter:creator" content="@Ease_app" />
	<meta name="twitter:title" content="Ease.space | Le meilleur moyen de se connecter à ses sites préférés." />
	<meta name="twitter:description" content="Ease est la homepage qui vous débarrasse des mots de passe. En 1 clic, soyez connecté à vos sites web automatiquement." />
	<meta name="twitter:image" content="https://ease.space/resources/images/fbmeta-en.png" />
	<meta http-equiv="Content-Type" contentType="text/html; charset=UTF-8" />
	<meta name="viewport" content="initial-scale=1, maximum-scale=1" />
	<meta property="og:image"
	content="https://ease.space/resources/other/fb_letsgo_icon.jpg" />
	<link rel="icon" type="image/png" href="resources/icons/APPEASE.png" />
	<script
	src="https://ajax.googleapis.com/ajax/libs/jquery/1.12.4/jquery.min.js"></script>
	<script src="https://code.jquery.com/ui/1.12.0/jquery-ui.js"></script>
	<link rel="stylesheet" href="css/default_style.css" />
	<link rel="stylesheet" type="text/css" href="//fonts.googleapis.com/css?family=Droid+Serif" />
	<link href='https://fonts.googleapis.comcss?family=Source+Sans+Pro' rel='stylesheet' type='textcss' />
	<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/css/bootstrap.min.css" />
	<link rel="stylesheet" type="text/css" href="//fonts.googleapis.com/css?family=Raleway" />



	<link rel="manifest" href="manifest.json">

	<script src="js/basic-utils.js"></script>
	<script src="js/postHandler.js"></script>
	<script src="js/websocket.js"></script>
	<script src="js/checkForInvitation.js"></script>
	<script
	src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/js/bootstrap.min.js">
</script>
<link rel="stylesheet" href="css/default_style.css" />
<link rel="stylesheet" href="css/landingPage.css" />
<link rel="stylesheet" type="text/css" href="css/lib/fonts/font-awesome-4.2.0/css/font-awesome.min.css" />
<link rel="stylesheet" type="text/css" href="css/lib/dropDownMenu/dropdown.css" />
<script src="js/selectFx.js"></script>
<script src="js/jquery.complexify.min.js"></script>
<script type="text/javascript">$crisp=[];CRISP_WEBSITE_ID="6e9fe14b-66f7-487c-8ac9-5912461be78a";(function(){d=document;s=d.createElement("script");s.src="https://client.crisp.im/l.js";s.async=1;d.getElementsByTagName("head")[0].appendChild(s);})();</script>
<script src="js/tracker.js"></script>
<script type="text/javascript">
	(function(e,t){var n=e.amplitude||{_q:[],_iq:{}};var r=t.createElement("script");r.type="text/javascript";
		r.async=true;r.src="https://d24n15hnbwhuhn.cloudfront.net/libs/amplitude-3.0.1-min.gz.js";
		r.onload=function(){e.amplitude.runQueuedFunctions()};var i=t.getElementsByTagName("script")[0];
		i.parentNode.insertBefore(r,i);function s(e,t){e.prototype[t]=function(){this._q.push([t].concat(Array.prototype.slice.call(arguments,0)));
			return this}}var o=function(){this._q=[];return this};var a=["add","append","clearAll","prepend","set","setOnce","unset"];
			for(var u=0;u<a.length;u++){s(o,a[u])}n.Identify=o;var c=function(){this._q=[];return this;
			};var p=["setProductId","setQuantity","setPrice","setRevenueType","setEventProperties"];
			for(var l=0;l<p.length;l++){s(c,p[l])}n.Revenue=c;var d=["init","logEvent","logRevenue","setUserId","setUserProperties","setOptOut","setVersionName","setDomain","setDeviceId","setGlobalUserProperties","identify","clearUserProperties","setGroup","logRevenueV2","regenerateDeviceId"];
				function v(e){function t(t){e[t]=function(){e._q.push([t].concat(Array.prototype.slice.call(arguments,0)));
				}}for(var n=0;n<d.length;n++){t(d[n])}}v(n);n.getInstance=function(e){e=(!e||e.length===0?"$default_instance":e).toLowerCase();
				if(!n._iq.hasOwnProperty(e)){n._iq[e]={_q:[]};v(n._iq[e])}return n._iq[e]};e.amplitude=n;
			})(window,document);

			/* Prod */
	//amplitude.getInstance().init("74f6ebfba0c7743a0c63012dc3a9fef0");

	/* Test */
	amplitude.getInstance().init("73264447f97c4623fb38d92b9e7eaeea");
	easeTracker.trackEvent("HomepageVisit");
</script>
</head>

<body id="landingBody">
	<%@ include file="templates/landingPage/landingHeader.jsp"%>

</body>
</html>