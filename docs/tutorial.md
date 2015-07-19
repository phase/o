#Tutorial
This tutorial on O was written by [Phase](https://github.com/o).

##1 The Stack

###1.1 Outputting
You can output the top of the stack to stdout by using `o`.
```
"Hello, World!"o
```
*Pops "Hello, World!" off the stack and outputs it*

###1.2 Pushing
There are many types of objects we can push to the stack, but the ones you'll use the most are *Strings and Doubles*.

###1.2.1 Strings
You can push strings to the stack by using `""`.
```
"Hello World!"
```
*The stack now contains "Hello World!"*

###1.2.2 Numbers
Numbers are stored as doubles on the stack. You can just put the number you want to push it to the stack.
```
5
```
*5 is pushed to the stack*

You can't do double-digit numbers, though!
```
55
```
*Pushes two 5s to the stack*

You can use `A-Z` as numbers, since numbers are parsed in Base 36.
```
5Z8
```
Stack Contents:

* 8
* 35
* 5

#1.3 Stack manipulation
You can manipulate the stack in a variety of ways. Here's a list of the most common operators:

* `;`: pop the top of the stack and discard it
* `\`: swap the top two values
* '@': put the third value of the stack on top
* `r`: reverse the stack
* `l`: push the length of the stack
* `o`: pop the top of the stack and output it
* `i`: get input and push it as a string
* `j`: get input and push it as a base 10 number
