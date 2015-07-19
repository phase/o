#Numbers
Numbers can be modified in a load of different ways.

##+
Two numbers will be popped off the stack and added together.
```
56+ o
```
*Outputs `11`*

##-
Normal subtraction. **Does NOT make number negative!**
```
54-o
```
*Outputs `1`*

##_
Makes the top of the stack negative.
```
5_o
```
*Outputs `-5`*

##*
Multiplication:
```
54*o
```
*Outputs `20`*

##/
Division:
```
93/o
```
*Outputs 3*

##e
Push `1` if the number is even, push `0` otherwise.
```
2eo 1eo
```
*Outputs `10`*

##`
Treats the Number as a String and reverses it.
```
Z`o
```
*Outputs `53`*