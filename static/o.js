function genUni() {
    var code = prompt("Generate Unicode Character:");
    $('#code').val($('#code').val() + String.fromCharCode(parseInt(code)));
    updateByteCount();
};

function getByteCount(s) {
    var count = 0, stringLength = s.length;
    s = String(s || "");
    for (var i = 0; i < stringLength; i++) {
        var partCount = encodeURI(s[i]).split("%").length;
        count += partCount == 1 ? 1 : partCount - 1;
    }
    return count;
}

function updateByteCount() {
    var c = $('#code').val();
    var byteCount = getByteCount(c);
    var charCount = c.length;
    var s = byteCount + " bytes and " + charCount + " chars long.";
    $('#byteCount').html(s);
}

function getStrippedCode() {
    var stripped = $('#code').val().replace(/\s/g, '');
    $('#stripped').html('Stripped code: <code>' + stripped + '</code> &nbsp; Byte count: ' + getByteCount(stripped));
}

function getExplanantion() {
    $('#explanation').html('');
    var code = $('#code').val();
    var e = parse(code);
    $("#explanation").html(e);
    var width = getMaxWidth() * 8;
    $("#explanation").width(width > window.innerWidth * 0.25 ? window.innerWidth * 0.25 : width);
    if ($("#explanation").height() > window.innerHeight * 0.25) $("#explanation").height(window.innerHeight * 0.25);
}

function selectText(container) {
    if (document.selection) {
        var range = document.body.createTextRange();
        range.moveToElementText(document.getElementById(container));
    } else if (window.getSelection) {
        var range = document.createRange();
        range.selectNode(document.getElementById(container));
        window.getSelection().addRange(range);
    }
}

function updateUtils() {
    updateByteCount();
    getStrippedCode();
    getExplanantion();
}

updateUtils();

$(document).ready(function() {
    $("#permalink").click(function() {
        var code = window.btoa($('#code').val().replace(/ /g, "%20")) + "/" + window.btoa($('#input').val().replace(/ /g, "%20"));
        prompt("Permalink:", "http://" + window.location.hostname + "/link/" + code);
        window.location.pathname = "/link/" + code;
    });
    $('#code').on('input propertychange paste', function() {
        updateUtils();
    });
});
