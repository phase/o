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
The top of the stack is now `25`.
