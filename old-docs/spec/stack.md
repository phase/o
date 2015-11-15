#The Stack

##Outputting
You can output the top of the stack to stdout by using `o`.
```
"Hello, World!"o
```
*Pops "Hello, World!" off the stack and outputs it*

##Strings
You can push strings to the stack by using `""`.
```
"Hello World!"
```
*The stack now contains "Hello World!"*

##Characters
Characters are really stored as Strings on the stack, but they are a quick way to save a byte when [golfing](http://codegolf.stackexchange.com).
```
'io
```
*Outputs `i`"

##Numbers
Numbers are stored as doubles on the stack. You can just put the number you want to push it to the stack.
```
5
```
*5 is pushed to the stack*

You can't do double-digit numbers, though!
```
55
```
*Pushes two `5`s to the stack*

You can use `A-Z` as numbers, since numbers are parsed in Base 36.
```
5Z8
```
Stack Contents:

* 8
* 35
* 5

##Stack manipulation
You can manipulate the stack in a variety of ways. Here's a list of the most common operators:

* `;`: pop the top of the stack and discard it
* `\`: swap the top two values
* `@`: put the third value of the stack on top
* `r`: reverse the stack
* `l`: push the length of the stack
* `o`: pop the top of the stack and output it
* `i`: get input and push it as a string
* `j`: get input and push it as a base 10 number
