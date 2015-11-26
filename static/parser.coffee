ObjType =
    Number: 0
    String: 1,
    Char: 2,
    Array: 3,
    CodeBlock: 4,
    DoLoop: 5,
    If: 6,
    WhileLoop: 7

class Explanation
    constructor: (@element, @explanation) ->

numberExplanation = (n) -> new Explanation(n, "Push " + n +  " to the stack\n")
stringExplanation = (string) -> new Explanation("\""+string+"\"", "Push string to the stack\n")
charExplanation = (c) -> new Explanation("'"+c, " Push " + c + " to the stack\n")

class Obj
    constructor: (@type,@string,@num,@array,@cbexs,@explanation) ->
    @type: ObjType.Number
    @string: ""
    @num: ""
    @array: []
    @explanation: new Explanation("","")
    @cbexs: []

newString = (string) -> new Obj(ObjType.String, string, "", [], [], stringExplanation(string))
newNumber = (n) -> new Obj(ObjType.Number, "", n, [], [], numberExplanation(n))
newChar = (c) -> new Obj(ObjType.Char, c, "", [], [], charExplanation(c))
newCodeBlock = (string) -> new Obj(ObjType.CodeBlock, string, "", [], [], new Explanation("{"+getSpaces(string.length)+"}", "Push CodeBlock to the stack\n"))

EventType =
    ObjectEvent: 0,
    StringEvent: 1

class Event
    constructor: (@type, @string, @c, @object) ->

eventString = (c, s) -> new Event(EventType.StringEvent, s, c, 0)
eventObj = (o) -> new Event(EventType.ObjectEvent, "", '', o)

events = []

#flags
fcb = false
fs = false
fc = false

buffer = ""

#CodeBlock Indent
cbi = 0
#CodeBlock Explanations
cbexs = []
newCodeBlockExplanation = (ex) -> cbexs.push(new Explanation(getSpaces(cbexs.length) + ex.element, ex.explanation))

resetParser = () ->
    events = []
    fcb = false
    fs = false
    fc = false
    buffer = ""
    cbecs = []
    cbi = 0

parse = (code) ->
    if code is ""
        return ""
    resetParser()
    for i in [0..(code.length-1)]
        c = code.charAt i
        if fc and not fs and not fcb
            fc = false
            events.push eventObj newChar c
        if fcb and not fs
            if c is "{"
                cbi++
            if c is "}" and cbi is 0
                fcb = false
                cb = newCodeBlock buffer
                cb.cbexs = cbexs
                events.push eventObj cb
                cbexs = []
                buffer = ""
                continue
            if c is "}"
                cbi--
            buffer += c
            newCodeBlockExplanation new Explanation(c, "Test");
        else if fs and not fcb
            if c is "\""
                fs = false
                events.push eventObj newString buffer
                buffer = ""
                continue
            buffer += c
        else if c.match /[1-9A-F]/
            events.push eventObj newNumber c
        else if c is "{"
            fcb = true
        else if c is "\""
            fs = true
        else if c is "'"
            fc = true
        else if c is "J" or c is "K"
            events.push eventString c, "Assign to variable " + c
        else if c is "d" and not fcb
            if events[events.length-1].object.type is ObjType.CodeBlock
                events[events.length-1].object.type = ObjType.DoLoop
        else if c is "?" and not fcb
            if events[events.length-1].object.type is ObjType.CodeBlock
                events[events.length-1].object.type = ObjType.If
        else if c is "w" and not fcb
            if events[events.length-1].object.type is ObjType.CodeBlock
                events[events.length-1].object.type = ObjType.WhileLoop
        #normal explanations
        else if explanations[c] != undefined
            events.push eventString c, explanations[c]
    explain events

maxWidth = 0
beforeSpaces = 0

getMaxWidth = () -> maxWidth

getSpaces = (i) ->
    if i < 0
        ""
    s = ""
    j = 0;
    while j<i
        s+= " "
        j++
    s

explain = (events) ->
    maxWidth = 0
    beforeSpaces = 0
    maxSpaces = 0
    e = ""
    for event in events
        if event.type is EventType.ObjectEvent
            maxSpaces += event.object.num.length if event.object.type is ObjType.Number
            maxSpaces += event.object.string.length + 2 if event.object.type is ObjType.String
            maxSpaces += event.object.string.length + 1 if event.object.type is ObjType.Char
            maxSpaces += event.object.string.length + 2 if event.object.type is ObjType.CodeBlock
            maxSpaces += event.object.string.length + 3 if event.object.type is ObjType.DoLoop
            maxSpaces += event.object.string.length + 3 if event.object.type is ObjType.If
            maxSpaces += event.object.string.length + 3 if event.object.type is ObjType.WhileLoop
        else if event.type is EventType.StringEvent
            maxSpaces += 1
    for event in events
        if event.type is EventType.ObjectEvent
            if event.object.type is ObjType.Number
                g = event.object.num.length
                s = getSpaces(beforeSpaces) + event.object.explanation.element + getSpaces(maxSpaces-beforeSpaces) + event.object.explanation.explanation
                if maxWidth < s.length
                    maxWidth = s.length
                beforeSpaces += g
                e += s
            else if event.object.type is ObjType.String
                g = event.object.string.length + 2
                s = getSpaces(beforeSpaces) + event.object.explanation.element + getSpaces(maxSpaces-beforeSpaces-g+1) + event.object.explanation.explanation
                if maxWidth < s.length
                    maxWidth = s.length
                beforeSpaces += g
                e += s
            else if event.object.type is ObjType.Char
                g = event.object.string.length + 1
                s = getSpaces(beforeSpaces) + "'" + event.object.explanation.element + getSpaces(maxSpaces-beforeSpaces-g+2) + event.object.explanation.explanation
                if maxWidth < s.length
                    maxWidth = s.length
                beforeSpaces += g
                e += s
            else if event.object.type is ObjType.CodeBlock
                g = event.object.string.length + 2
                s = getSpaces(beforeSpaces) + "{" + getSpaces(event.object.string.length) + "}" + getSpaces(maxSpaces-beforeSpaces-g+1) + "Push CodeBlock to the stack"
                cbei = 0
                for cbex in event.object.cbexs
                    s += "\n" + getSpaces(beforeSpaces+1) + cbex.element + getSpaces(event.object.string.length-cbei+1) + cbex.explanation
                    cbei++
                if maxWidth < s.length
                    maxWidth = s.length
                beforeSpaces += g
                e += s
            else if event.object.type is ObjType.DoLoop
                g = event.object.string.length + 3
                s = getSpaces(beforeSpaces) + "{" + event.object.string + "}d" + getSpaces(maxSpaces-beforeSpaces-g+1) + " Run block\n"
                if maxWidth < s.length
                    maxWidth = s.length
                beforeSpaces += g
                e += s
            else if event.object.type is ObjType.If
                g = event.object.string.length + 3
                s = getSpaces(beforeSpaces) + "{" + event.object.string + "}?" + getSpaces(maxSpaces-beforeSpaces-g+1) + " If block\n"
                if maxWidth < s.length
                    maxWidth = s.length
                beforeSpaces += g
                e += s
            else if event.object.type is ObjType.WhileLoop
                g = event.object.string.length + 3
                s = getSpaces(beforeSpaces) + "{" + event.object.string + "}w" + getSpaces(maxSpaces-beforeSpaces-g+1) + " While the top of the stack is true, run this block\n"
                if maxWidth < s.length
                    maxWidth = s.length
                beforeSpaces += g
                e += s
        else if event.type is EventType.StringEvent
            s = getSpaces(beforeSpaces) + event.c + getSpaces(maxSpaces-beforeSpaces) + event.string
            g = s.length
            if maxWidth < g
                maxWidth = g
            beforeSpaces += 1
            e += s + "\n"
    e