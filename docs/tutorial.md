#Tutorial
This tutorial on O was written by [Phase](https://github.com/o). You can view it on GitHub [here](https://github.com/phase/o/blob/master/docs/tutorial.md).

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
*Pushes two 5s to the stack*

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
* '@': put the third value of the stack on top
* `r`: reverse the stack
* `l`: push the length of the stack
* `o`: pop the top of the stack and output it
* `i`: get input and push it as a string
* `j`: get input and push it as a base 10 number

#Variables
Variables hold one value that can be pushed to the stack. To make a variable, you use `:`. This will take the value on the top of the stack and set the variable's value to it.
```
4:C
```
C is now set to 4. This will override any numbers, so using C won’t push 12 anymore, it will push 4. You can use any Unicode characters, but they will add to you byte count.

To push the value of a variable, you just have to use it as an operator.
```
4:k;kk+
```
The stack now holds 8:

* 4 is pushed
* the value of k is set to 4
* 4 is popped off
* k is pushed twice, putting two 4s on the stack
* the two 4s are added together, leaving 8

To change the value of a variable, you just have to reassign it to the top of the stack using `:`.
```
4:l;5:l;ll*
```
The top of the stack is now 25.


#CodeBlocks
CodeBlock are blocks of code than be assigned to variables and user in operators.
```
{.*}s; 5s o
```

* pushes a CodeBlock to the stack with the contents of `.*`
* the value of `s` is set to the CodeBlock
* the CodeBlock is popped off
* `5` is pushed to the stack
* `s` runs, squaring 5
* `25` is outputted
