$.fn.selectRange = function(start, end) {
    if(typeof end === 'undefined') {
        end = start;
    }
    return this.each(function() {
        if('selectionStart' in this) {
            this.selectionStart = start;
            this.selectionEnd = end;
        } else if(this.setSelectionRange) {
            this.setSelectionRange(start, end);
        } else if(this.createTextRange) {
            var range = this.createTextRange();
            range.collapse(true);
            range.moveEnd('character', end);
            range.moveStart('character', start);
            range.select();
        }
    });
};

$.fn.getCursorPosition = function () {
    var pos = 0;
    var el = $(this).get(0);
    // IE Support
    if (document.selection) {
        el.focus();
        var Sel = document.selection.createRange();
        var SelLength = document.selection.createRange().text.length;
        Sel.moveStart('character', -el.value.length);
        pos = Sel.text.length - SelLength;
    }
    // Firefox support
    else if (el.selectionStart || el.selectionStart == '0')
        pos = el.selectionStart;
    return pos;
};

String.prototype.replaceAt = function(index, character) {
    return this.substr(0, index) + character + this.substr(index+character.length);
}

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

function t(s){for(var i=0;i<s.length;i++){console.log(s.charCodeAt(i));}}

function updateByteCount() {
    var c = $('#code').val();
    var byteCount = getByteCount(c);
    var charCount = c.length;
    var s = byteCount + " bytes and " + charCount + " chars long.";
    $('#byteCount').html(s);
}

function getStrippedCode() {
    var stripped = $('#code').val().replace(/\s/g, '');
    $('#stripped').html(
            'Stripped code: <code>' + stripped + '</code> &nbsp; Byte count: '
                    + getByteCount(stripped));
}

var string = false;
var codeBlock = false;
var math = false;
var file = false;

function getExplanantion() {
    string = false;
    codeBlock = false;
    math = false;
    file = false;
    $('#explanation').html('');
    var code = $('#code').val().replace(/\s/g, '');
    var bracketIndent = 0;
    var exSpaces = 1;
    var stringSize = 0;
    for (var x = 0, c = ''; c = code.charAt(x); x++) {
        if (c == '"') {
            string = !string;
        } else if (string) {
            stringSize++;
        } else if ((c == '{' || c == '[' || c == 'H' || c == 'I' || c == 'M')
                && !string) {
            exSpaces++;
        }
    }
    var maxLength = 0;
    
    string = false;
    codeBlock = false;
    math = false;
    file = false;
    for (var x = 0, c = ''; c = code.charAt(x); x++) {
        if ((c == '}' || c == ']' || c == 'S') && bracketIndent > 0 && !string) {
            bracketIndent--;
        } else if (c == '"') {
            string = !string;
        }
        var original = $('#explanation').html();
        var spaces = "";
        var es = "";
        for (var d = 0; d < bracketIndent; d++) {
            spaces += " ";
        }
        for (var d = 0; d < exSpaces - bracketIndent + stringSize + 1; d++) {
            es += " ";
        }
        if ((c == '{' || c == '[' || c == 'H' || c == 'I' || c == 'M')
                && !string) {
            bracketIndent++;
        }
        var ex = (string ? "" : spaces) + c + (string ? "" : (c == '"' ? " " : es) + explanations[c] + "\r\n");
        if(ex.length > maxLength) maxLength = ex.length;
        $('#explanation').html(original + ex);
    }
    var width = maxLength * 8;
    //console.log("New width: " + width);
    $("#explanation").width(width);
}

function updateUtils() {
    updateByteCount();
    getStrippedCode();
    getExplanantion();
}

updateUtils();

$(document).ready(function() {
    $('#code').on('keydown', function() {
        var code = $("#code").val();
        var key = event.keyCode || event.charCode;
        var c = String.fromCharCode(event.which);
        var pos = $("#code").getCursorPosition()-1;
        if( key == 8 || key == 46 ){ //delete text
            console.log("Delete: " + c + " " + pos + " :: " + event.which);
            if(c == "(" && code.charAt(pos+1) == ")") code.replaceAt(pos+1, "");
            if(c == "{" && code.charAt(pos+1) == "}") code.replaceAt(pos+1, "");
        } else {
            //if()
        }
        $("#code").val(code);
    });
    $("#permalink").click(function() {
        /*var code = $.param({
            code : $('#code').val().replace(" ", "%20"),
            input : $('#input').val()
        });*/
        var code = "code=" + $('#code').val().replace(/ /g, "%20") 
                    + "&input=" + $('#input').val().replace(/ /g, "%20");
        prompt("Permalink:", "http://" + window.location.hostname + "/link/" + code);
        window.location.pathname = "/link/" + code;
    });
    $('#code').on('input propertychange paste', function() {
        updateUtils();
    });
});