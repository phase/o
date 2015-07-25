function genUni(){
  var code = prompt("Generate Unicode Character:");
  $('#code').val($('#code').val() + String.fromCharCode(parseInt(code)));
  updateByteCount();
};

function getByteCount(s) {
  var count = 0, stringLength = s.length;
  s = String(s || "");
  for(var i = 0; i < stringLength; i++){
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

updateByteCount();
    
$(document).ready(function() {
  $("#permalink").click(function(){
    var code = $.param({code: $('#code').val(), input: $('#input').val()});
    prompt("Permalink:", "http://" + window.location.hostname + "/link/" + code);
    window.location.pathname = "/link/" + code;
  });
  $('#code').on('input propertychange paste', function() {
    updateByteCount();
  });
});