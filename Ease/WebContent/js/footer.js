$(document).ready(function() {
  $("#buttonShow i").hover(function(e) {
	$(".footer").show("slide", { direction: "down" }, 500);
    $(".phrase").css({"-webkit-transform":"translateY(-25px)","transform":"translateY(-25px)"});
    $("#buttonShow").hide("slide", { direction: "down" }, 500);

  });
  $(".mediaTab").mouseleave(function() {
	$(".footer").hide("slide", { direction: "down" }, 500);
    $(".phrase").css({"-webkit-transform":"translateY(0px)","transform":"translateY(0px)"});
    $("#buttonShow").show("slide", { direction: "down" }, 500);
  })
});
