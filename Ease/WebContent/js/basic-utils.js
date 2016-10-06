function sendEvent(email, password, userSelector, passSelector, buttonSelector, urlLogin, urlHome){ //dispatch an Event to warn the plugin it has to connect to the websit\e
    var event = new CustomEvent("NewConnection",
       {'detail':{"user": email,
       "pass":password,
       "userField":userSelector,
       "passField":passSelector,
       "button": buttonSelector,
       "urlLogin": urlLogin,
       "urlHome": urlHome}});
    document.dispatchEvent(event);
    console.log("Ease : go to new page");
    console.log(event.detail);
}

function setCookie(cname, cvalue, exdays) {
    var d = new Date();
    d.setTime(d.getTime() + (exdays*24*60*60*1000));
    var expires = "expires="+d.toUTCString();
    document.cookie = cname + "=" + cvalue + "; " + expires;
}

function getCookie(cname) {
    var name = cname + "=";
    var ca = document.cookie.split(';');
    for(var i = 0; i < ca.length; i++) {
        var c = ca[i];
        while (c.charAt(0) == ' ') {
            c = c.substring(1);
        }
        if (c.indexOf(name) == 0) {
            return c.substring(name.length, c.length);
        }
    }
    return "";
}

function pad(num, totalChars) {
    var pad = '0';
    num = num + '';
    while (num.length < totalChars) {
        num = pad + num;
    }
    return num;
}

// Ratio is between 0 and 1
function changeColor(color, ratio, darker) {
    // Trim trailing/leading whitespace
    color = color.replace(/^\s*|\s*$/, '');

    // Expand three-digit hex
    color = color.replace(
        /^#?([a-f0-9])([a-f0-9])([a-f0-9])$/i,
        '#$1$1$2$2$3$3'
        );

    // Calculate ratio
    var difference = Math.round(ratio * 256) * (darker ? -1 : 1),
        // Determine if input is RGB(A)
        rgb = color.match(new RegExp('^rgba?\\(\\s*' +
            '(\\d|[1-9]\\d|1\\d{2}|2[0-4][0-9]|25[0-5])' +
            '\\s*,\\s*' +
            '(\\d|[1-9]\\d|1\\d{2}|2[0-4][0-9]|25[0-5])' +
            '\\s*,\\s*' +
            '(\\d|[1-9]\\d|1\\d{2}|2[0-4][0-9]|25[0-5])' +
            '(?:\\s*,\\s*' +
            '(0|1|0?\\.\\d+))?' +
            '\\s*\\)$'
            , 'i')),
        alpha = !!rgb && rgb[4] != null ? rgb[4] : null,

        // Convert hex to decimal
        decimal = !!rgb? [rgb[1], rgb[2], rgb[3]] : color.replace(
            /^#?([a-f0-9][a-f0-9])([a-f0-9][a-f0-9])([a-f0-9][a-f0-9])/i,
            function() {
                return parseInt(arguments[1], 16) + ',' +
                parseInt(arguments[2], 16) + ',' +
                parseInt(arguments[3], 16);
            }
            ).split(/,/),
        returnValue;

    // Return RGB(A)
    return !!rgb ?
    'rgb' + (alpha !== null ? 'a' : '') + '(' +
    Math[darker ? 'max' : 'min'](
        parseInt(decimal[0], 10) + difference, darker ? 0 : 255
        ) + ', ' +
    Math[darker ? 'max' : 'min'](
        parseInt(decimal[1], 10) + difference, darker ? 0 : 255
        ) + ', ' +
    Math[darker ? 'max' : 'min'](
        parseInt(decimal[2], 10) + difference, darker ? 0 : 255
        ) +
    (alpha !== null ? ', ' + alpha : '') +
    ')' :
        // Return hex
        [
        '#',
        pad(Math[darker ? 'max' : 'min'](
            parseInt(decimal[0], 10) + difference, darker ? 0 : 255
            ).toString(16), 2),
        pad(Math[darker ? 'max' : 'min'](
            parseInt(decimal[1], 10) + difference, darker ? 0 : 255
            ).toString(16), 2),
        pad(Math[darker ? 'max' : 'min'](
            parseInt(decimal[2], 10) + difference, darker ? 0 : 255
            ).toString(16), 2)
        ].join('');
    }

    function lighterColor(color, ratio) {
        return changeColor(color, ratio, false);
    }


    function darkerColor(color, ratio) {
        return changeColor(color, ratio, true);
    }

    function disableAutocompele(){
     if (document.getElementsByTagName) {
        var inputElements = document.getElementsByTagName("input");
        for (i=0; inputElements[i]; i++) {
            inputElements[i].setAttribute("autocomplete","off");
        }
    }
}

function setupOwlCarousel(){
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
        touchDrag : false,
        mouseDrag : false,
        afterInit: function () {
            $('.owl-carousel').find('.owl-wrapper').each(function () {
                var w = $(this).width() / 2;
                $(this).width(w);
                $(this).css('margin', '0 auto');
            });
        },
        afterUpdate: function () {
            $('.owl-carousel').find('.owl-wrapper').each(function () {
                var w = $(this).width() / 2;
                $(this).width(w);
                $(this).css('margin', '0 auto');
            });
        }       
    });
    $('.owl-wrapper').sortable({
        animation: 300,
        group:"profiles",
        handle: ".ProfileName",
        forceFallback: true,
        onStart: function(evt){
            var item = $(evt.item);
            $('body').css('cursor', 'move');
            item.css({
                'pointer-events': 'none',
                'opacity': '0'
            });
        },
        onEnd: function(evt){
            var item = $(evt.item);
            $('body').css('cursor', '');
            item.css({
                'pointer-events': '',
                'opacity': ''
            });
            if (evt.oldIndex != evt.newIndex){
                $.post(
                    "moveProfile",
                    {
                        profileId: item.find('.item').attr('id'),
                        index: item.index()
                    },
                    function (data){
                    },
                    'text'
                    );
            }
        }
    });
}

