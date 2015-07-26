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

var explanations = {
	'0' : 'Push 0 to the stack',
	'1' : 'Push 1 to the stack',
	'2' : 'Push 2 to the stack',
	'3' : 'Push 3 to the stack',
	'4' : 'Push 4 to the stack',
	'5' : 'Push 5 to the stack',
	'6' : 'Push 6 to the stack',
	'7' : 'Push 7 to the stack',
	'8' : 'Push 8 to the stack',
	'9' : 'Push 9 to the stack',
	'A' : 'Push 10 to the stack',
	'B' : 'Push 11 to the stack',
	'C' : 'Push 12 to the stack',
	'D' : 'Push 13 to the stack',
	'E' : 'Push 14 to the stack',
	'F' : 'Push 15 to the stack',
	'[' : 'Start an array',
	']' : 'Push the array to the stack',
	'o' : 'Pop the stack and print it',
	'p' : 'Pop the stack and print it with a new line',
	';' : 'Pop the top of the stack off',
	'+' : 'Add the top two objects on the stack',
	'-' : 'Subtract the top two objects on the stack',
	'*' : 'Multiply the top two objects on the stack',
	'/' : 'Divide the top two objects on the stack'

};

function getExplanantion() {
	$('#explanation').html('');
	var code = $('#code').val().replace(/\s/g, '');
	var bracketIndent = 0;
	var exSpaces = 1;
	for (var x = 0, c = ''; c = code.charAt(x); x++) {
		if (c == '{' || c == '[' || c == 'H' || c == 'I' || c == 'M') {
			exSpaces++;
		}
	}

	for (var x = 0, c = ''; c = code.charAt(x); x++) {
		if ((c == '}' || c == ']' || c == 'S') && bracketIndent > 0) {
			bracketIndent--;
		}
		var original = $('#explanation').html();
		var spaces = "";
		var es = "";
		for (var d = 0; d < bracketIndent; d++) {
			spaces += " ";
		}
		for (var d = 0; d < exSpaces - bracketIndent; d++) {
			es += " ";
		}
		if (c == '{' || c == '[' || c == 'H' || c == 'I' || c == 'M') {
			bracketIndent++;
		}
		$('#explanation').html(
				original + spaces + c + es + explanations[c] + "\r\n");
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