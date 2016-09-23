<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ page import="com.Ease.session.User"%>
<%@ page import="com.Ease.session.Profile"%>
<%@ page import="com.Ease.context.Site"%>
<%@ page import="java.util.List"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<%
	int itr = 0;
%>
<script>
	$(document)
			.ready(
					function() {
						$('#PopupAddApp #password')
								.keyup(
										function(event) {
											if (event.keyCode == 13) {
												$(
														"#PopupAddApp .md-content .buttonSet #accept")
														.click();
											}
										});
						$('#PopupAddApp #login').keyup(function(event) {
							if (event.keyCode == 13) {
								$('#PopupAddApp #password').focus();
							}
						});
						$('.helpIntegrateApps #integrateAppForm #integrateApp').keyup(function(e){
						    if(e.keyCode == 13)
						    	$('.helpIntegrateApps #integrateAppForm #integrate').trigger("click");
						});
						$('.helpIntegrateApps #integrateAppForm #integrate').click(
										function() {
											var form = $(this).closest('#integrateAppForm');
											$.post(
												'askForNewApp',
												{
													ask : $(form).find('#integrateApp').val()
												},
												function(data) {
													$(form).find('.inputs input').val('');
													$(form).find('.inputs').hide();
													$(form).find('.confirmation').show().delay(1000).fadeOut(function() {
														$(form).find('.inputs').show();
													});
												},
												'text');
										});
					});

	function reinitCarousel() {
		var owl = $(".owl-carousel").data('owlCarousel');

		$(".ProfilesHandler").addClass('editMode');
		owl.reinit({
			items : 3,
			itemsCustom : false,
			itemsDesktop : [ 1199, 3 ],
			itemsDesktopSmall : [ 980, 3 ],
			itemsTablet : [ 768, 3 ],
			itemsTabletSmall : false,
			itemsMobile : [ 479, 1 ],
			singleItem : false,
			itemsScaleUp : false,
			pagination : false,
		    touchDrag: false,
		    mouseDrag: false
		});
	}

	function enterEditMode() {


		$('#dragAndDropHelper').css('display', 'block');
		$('#tutorialView').css('display', 'none');
		$(".ProfilesHandler").addClass('editMode');
		$('.CatalogViewTab').addClass('show');
		var addProfileHelper = $('#addProfileHelper').find('.item');
		var owl = $(".owl-carousel").data('owlCarousel');
		owl.destroy();
		var nbProfiles = $('.owl-carousel > *').length;

		if (nbProfiles < 3) {
			$('.owl-carousel').append($(addProfileHelper));
		}

		$('.owl-carousel').owlCarousel({
			items : 3,
			itemsCustom : false,
			itemsDesktop : [ 1199, 3 ],
			itemsDesktopSmall : [ 980, 3 ],
			itemsTablet : [ 768, 3 ],
			itemsTabletSmall : false,
			itemsMobile : [ 479, 1 ],
			singleItem : false,
			itemsScaleUp : false,
			pagination : false,
		    touchDrag: false,
		    mouseDrag: false
		});
	}

	function leaveEditMode() {
		console.log("quit");
		console.trace();
		$('#dragAndDropHelper').css('display', 'none');
		if ($('#tutorialView').length) {
			window.location.replace("index.jsp");
		}
		$(".ProfilesHandler").removeClass('editMode');
		$('.CatalogViewTab').removeClass('show');

		var owl = $(".owl-carousel").data('owlCarousel');
		owl.destroy();
		var addProfileHelper = $('.AddProfileView').closest('.item');

		$('#addProfileHelper').append($(addProfileHelper));
		$('.owl-carousel').owlCarousel({
			items : 3,
			itemsCustom : false,
			itemsDesktop : [ 1199, 3 ],
			itemsDesktopSmall : [ 980, 3 ],
			itemsTablet : [ 768, 3 ],
			itemsTabletSmall : false,
			itemsMobile : [ 479, 1 ],
			singleItem : false,
			itemsScaleUp : false,
			pagination : false,
		    touchDrag: false,
		    mouseDrag: false

		});
		$('.scaleOutAnimation').removeClass('scaleOutAnimation');
	}

	$(document).ready(function() {
		$('.CatalogViewTab #quit').click(function() {
			leaveEditMode();
			//		$('.CatalogViewTab').removeClass('show');
			//    	window.location.replace("index.jsp");
		});
	});

	$(document).ready(
			function() {
				$(document).click(
						function(event) {
							if ($(".CatalogViewTab").hasClass("show")) {
								if (!$(event.target).parents().hasClass(
										"ProfilesHandler editMode")) {
									if (!($(event.target).is(
											"#enterEditMode img")
											|| $(event.target).is(
													"#enterEditMode")
											|| $(event.target).is(
													$(".CatalogViewTab"))
											|| $(".CatalogViewTab").has(
													$(event.target)).length
											|| $(event.target).is(
													"#PopupAddApp")
											|| $("#PopupAddApp").has(
													$(event.target)).length
											|| $(event.target).is(
													"#PopupDeleteApp")
											|| $("#PopupDeleteApp").has(
													$(event.target)).length
											|| $(event.target).is(
													"#PopupModifyApp")
											|| $("#PopupModifyApp").has(
													$(event.target)).length
											|| $(event.target).is(
													".AddProfileView") || $(
											".AddProfileView").has(
												$(event.target)).length
											|| $(event.target).is(
													"#PopupDeleteProfile")
											|| $("#PopupDeleteProfile").has(
												$(event.target)).length

									)) 
										leaveEditMode();
								}
							}
						});
			});

	/*$(document).ready(function() {
		$(document).click(function(event) {
			if ($(".CatalogViewTab").hasClass("show")) {
				if (!$(event.target).is('.CatalogViewTab show'))
					//leaveEditMode();
			}
			if ($('.CatalogViewTab ').hasClass("show")) {
				leaveEditMode();
				if ($('.ProfilContent').contains($(this))) {
					console.log("Obvious bug");
					leaveEditMode();
				}
			}
		});
	});*/

	function showConfirmDeleteAppPopup(elem, event) {
		event.preventDefault();
		event.stopPropagation();
		var popup = $('#PopupDeleteApp');
		var app = $(elem).closest('.siteLinkBox');
		var image = $(app).find('.linkImage');

		popup.addClass('md-show');
		popup.find("#close").unbind('click');
		popup.find("#close").click(function() {
			popup.removeClass('md-show');
		});
		popup.find("#accept").unbind('click');
		popup.find("#accept").click(function() {
			popup.removeClass('md-show');
			image.addClass('easyScaling');
			$.post('deleteApp', {
				appId: $(app).attr('id')
			}, function(data) {
				if (data[0] == 's') {
					image.removeClass('easyScaling');
					image.addClass('deletingApp');
					setTimeout(function() {
						console.log(app);
						app.remove();
					}, 500);
				} else {
					if (data[0] != 'e') {
						document.location.reload(true);
					} else {
						showAlertPopup(null, true);
						image.removeClass('easyScaling');
					}

				}
			}, 'text');
		});
	}

	$(document)
			.ready(
					function() {
						$('.catalogApp')
								.draggable(
										{
											cursor : 'none',
											cursorAt : {
												left : 25,
												top : 25
											},
											helper : function(e, ui) {
												var ret;

												ret = $('<div class="dragHelperLogo" style="position: fixed;"/>');
												ret.attr("idx", $(this).attr(
														"idx"));
												ret.attr("name", $(this).find(
														'p').text());
												ret.attr("connect", $(this)
														.attr("connect"));
												ret.attr("data-login", $(this)
														.attr("data-login"));
												ret.attr("data-sso", $(this)
														.attr("data-sso"));
												ret.append($('<img />'));
												ret.find('img').attr(
														"src",
														$(this).find('img')
																.attr("src"));
												return ret; //Replaced $(ui) with $(this)
											}
										});
					});
</script>
