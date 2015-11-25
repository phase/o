ObjType =
    Number: 0
    String: 1,
    CodeBlock: 2,
    Array: 3,
    DoLoop: 4,
    If: 5,
    WhileLoop: 6

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
        else if c is "d"
            if events[events.length-1].object.type is ObjType.CodeBlock
                events[events.length-1].object.type = ObjType.DoLoop
        else if c is "?"
            if events[events.length-1].object.type is ObjType.CodeBlock
                events[events.length-1].object.type = ObjType.If
        else if c is "w"
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
            if event.object.type is ObjType.Number
                maxSpaces += event.object.num.length
            else if event.object.type is ObjType.String
                maxSpaces += event.object.string.length + 2
            else if event.object.type is ObjType.CodeBlock
                maxSpaces += event.object.string.length + 2
            else if event.object.type is ObjType.DoLoop
                maxSpaces += event.object.string.length + 3
            else if event.object.type is ObjType.If
                maxSpaces += event.object.string.length + 3
            else if event.object.type is ObjType.WhileLoop
                maxSpaces += event.object.string.length + 3
        else if event.type is EventType.StringEvent
            maxSpaces += 1
    for event in events
        if event.type is EventType.ObjectEvent
            if event.object.type is ObjType.Number
                g = event.object.num.length
                s = getSpaces(beforeSpaces) + event.object.num + getSpaces(maxSpaces-beforeSpaces) + " Push " + event.object.num + " to the stack\n"
                if maxWidth < s.length
                    maxWidth = s.length
                beforeSpaces += g
                e += s
            else if event.object.type is ObjType.String
                g = event.object.string.length + 2
                s = getSpaces(beforeSpaces) + "\"" + event.object.string + "\"" + getSpaces(maxSpaces-beforeSpaces-g+1) + " Push string to the stack\n"
                if maxWidth < s.length
                    maxWidth = s.length
                beforeSpaces += g
                e += s
            else if event.object.type is ObjType.CodeBlock
                g = event.object.string.length + 2
                s = getSpaces(beforeSpaces) + "{" + event.object.string + "}" + getSpaces(maxSpaces-beforeSpaces-g+1) + " Push CodeBlock to the stack\n"
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
            s = getSpaces(beforeSpaces) + event.c + getSpaces(maxSpaces-beforeSpaces+1) + event.string
            g = s.length
            if maxWidth < g
                maxWidth = g
            beforeSpaces += 1
            e += s + "\n"
    e