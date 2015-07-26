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
	$('#stripped').html(
			'Stripped code: <code>' + stripped + '</code> Byte count: '
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
		$('#explanation').html(
				original
						+ (string ? "" : spaces)
						+ c
						+ (string ? "" : (c == '"' ? " " : es) + explanations[c]
								+ "\r\n"));
	}

}

function updateUtils() {
	updateByteCount();
	getStrippedCode();
	getExplanantion();
}

updateUtils();

$(document).ready(
		function() {
			$("#permalink").click(
					function() {
						var code = $.param({
							code : $('#code').val(),
							input : $('#input').val()
						});
						prompt("Permalink:", "http://"
								+ window.location.hostname + "/link/" + code);
						window.location.pathname = "/link/" + code;
					});
			$('#code').on('input propertychange paste', function() {
				updateUtils();
			});
		});