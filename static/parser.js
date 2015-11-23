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
                events.push(newCodeBlock(buffer));
                buffer = "";
                continue;
            }
            buffer += c;
        }
        if(fs && !fcb) {
            if(c == "\""){
                fs = false;
                events.push(newString(buffer));
                buffer = "";
                continue;
            }
            buffer += c;
        }
        else if(c.match(/[1-9A-F]/)){
            console.log(c);
            events.push(newNumber(c));
        }
        else if(c == "{"){
            fcb = true;
        }
        else if(c == "\""){
            fs = true;
        }
    }
    var ex = explain(events);
    return ex;
}

var maxWidth = 0;

function getMaxWidth() {
    return maxWidth;
}

function explain(events) {
    maxWidth = 0;
    e = "";
    //console.log(events)
    for(var i = 0; i < events.length; i++){
        var o = events[i];
        if(o instanceof Obj){
            if(o.type == ObjType.Number){
                var s = o.num + " Push " + o.num + " to the stack\n";
                if(maxWidth < s.length) maxWidth = s.length;
                e += s;
            }
            if(o.type == ObjType.String){
                var s = "\"" + o.string + "\" Push string to the stack\n";
                if(maxWidth < s.length) maxWidth = s.length;
                e += s;
            }
            if(o.type == ObjType.CodeBlock){
                var s = "{" + o.string + "} Push string to the stack\n";
                if(maxWidth < s.length) maxWidth = s.length;
                e += s;
            }
        }
    }
    return e;
}