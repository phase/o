operators = {
    'o': "Pop the stack and output",
    'p': "Pop the stack and print with new line"
}

ObjType = {
    Number: 0,
    String: 1,
    CodeBlock: 2,
    Array: 3
}

var Obj = function(){
    this.type = ObjType.Number;
    this.string = "";
    this.num = ""; //maybe strictly integer?
    this.array = [];
};

function newString(string){
    var o = new Obj();
    o.type = ObjType.String;
    o.string = string;
    return o;
}

function newNumber(n){
    var o = new Obj();
    o.type = ObjType.Number;
    o.num = n;
    return o;
}

function newCodeBlock(string){
    var o = new Obj();
    o.type = ObjType.CodeBlock;
    o.string = string;
    return o;
}

EventType = {
    ObjectEvent: 0,
    StringEvent: 1
}
var Event = function(){}

function eventString(c, s){
    var e = new Event();
    e.string = s;
    e.c = c;
    e.type = EventType.StringEvent;
    return e;
}

function eventObj(o){
    var e = new Event();
    e.object = o;
    e.type = EventType.ObjectEvent;
    return e;
}

var events = [];

//flags
var fcb=false,fs=false;

var buffer = "";

/**
 * Parses O code into AST
 */
function parse(code){
    events = [];
    fcb=false,fs=false;
    buffer = "";
    for(var i = 0; i < code.length; i++){
        var c = code.charAt(i);
        if(fcb && !fs){
            if(c == "}"){
                fcb = false;
                events.push(eventObj(newCodeBlock(buffer)));
                buffer = "";
                continue;
            }
            buffer += c;
        }
        if(fs && !fcb) {
            if(c == "\""){
                fs = false;
                events.push(eventObj(newString(buffer)));
                buffer = "";
                continue;
            }
            buffer += c;
        }
        else if(c.match(/[1-9A-F]/)){
            events.push(eventObj(newNumber(c)));
        }
        else if(c == "{"){
            fcb = true;
        }
        else if(c == "\""){
            fs = true;
        }
        else if(c == "J" || c == "K"){
            events.push(eventString(c, "Assign to variable " + c));
        }
        //normal operators
        else if(c in operators){
            events.push(eventString(c, operators[c]));
        }
    }
    var ex = explain(events);
    return ex;
}

var maxWidth = 0, spaces = 0;

function getMaxWidth() {
    return maxWidth;
}

function getSpaces(i) {
    var s = "", j = 0;
    while(j<i){s+= " ";j++;}
    return s;
}

function explain(events) {
    maxWidth = 0;
    spaces = 1;
    e = "";
    for(var i = 0; i < events.length; i++){
        var event = events[i];
        if(event.type == EventType.ObjectEvent){
            var o = event.object;
            if(o.type == ObjType.Number){
                var g = o.num.length;
                var s = o.num + getSpaces(spaces-g) +  " Push " + o.num + " to the stack\n";
                if(maxWidth < s.length) maxWidth = s.length;
                e += s;
            }
            if(o.type == ObjType.String){
                var g = o.string.length + 2/*quotes*/;
                if(spaces < g) spaces = g;
                var s = "\"" + o.string + "\"" + getSpaces(spaces-g) +  " Push string to the stack\n";
                if(maxWidth < s.length) maxWidth = s.length;
                e += s;
            }
            if(o.type == ObjType.CodeBlock){
                var g = o.string.length + 2/*brackets*/;
                var s = "{" + o.string + "}" + getSpaces(spaces-g) +  " Push codeblock to the stack\n";
                if(maxWidth < s.length) maxWidth = s.length;
                e += s;
            }
        }
        else if(event.type == EventType.StringEvent){
            var s = event.string;
            var g = s.length;
            if(maxWidth < g) maxWidth = g;
            e += event.c + getSpaces(spaces) + s + "\n";
        }
    }
    return e;
}