ObjType =
    Number: 0
    String: 1,
    CodeBlock: 2,
    Array: 3

class Obj
    constructor: (@type,@string,@num,@array) ->
    @type: ObjType.Number
    @string: ""
    @num: ""
    @array: []

newString = (string) -> new Obj(ObjType.String, string, "", [])
newNumber = (n) -> new Obj(ObjType.Number, "", n, [])
newCodeBlock = (string) -> new Obj(ObjType.CodeBlock, string, "", [])

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

buffer = ""

resetParser = () ->
    events = []
    fcb = false
    fs = false
    buffer = ""

###
  Parses O code into AST
###
parse = (code) ->
    if code is ""
        return ""
    resetParser()
    for i in [0..(code.length-1)]
        c = code.charAt i
        if fcb and not fs
            if c is "}"
                fcb = false
                events.push eventObj newCodeBlock buffer
                buffer = ""
                continue
            buffer += c
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
        else if c is "J" or c is "K"
            events.push eventString c, "Assign to variable " + c
        #normal explanations
        else if explanations[c] != undefined
            events.push eventString c, explanations[c]
    explain events

maxWidth = 0
spaces = 1

getMaxWidth = () -> maxWidth

getSpaces = (i) ->
    s = ""
    j = 0;
    while j<i
        s+= " "
        j++
    s

explain = (events) ->
    maxWidth = 0
    spaces = 1
    e = ""
    for event in events
        if event.type is EventType.ObjectEvent
            if event.object.type is ObjType.Number
                g = event.object.num.length
                s = event.object.num + getSpaces(spaces-g) + " Push " + event.object.num + " to the stack\n"
                if maxWidth < s.length
                    maxWidth = s.length
                e += s
            else if event.object.type is ObjType.String
                g = event.object.string.length + 2
                if spaces < g
                    spaces = g
                s = "\"" + event.object.string + "\"" + getSpaces(spaces-g) + " Push string to the stack\n"
                if maxWidth < s.length
                    maxWidth = s.length
                e += s
            else if event.object.type is ObjType.CodeBlock
                g = event.object.string.length + 2
                if spaces < g
                    spaces = g
                s = "{" + event.object.string + "}" + getSpaces(spaces-g) + " Push CodeBlock to the stack\n"
                if maxWidth < s.length
                    maxWidth = s.length
                e += s
        else if event.type is EventType.StringEvent
            s = event.c + getSpaces(spaces) + event.string
            g = s.length
            if maxWidth < g
                maxWidth = g
            e += s + "\n"
    e