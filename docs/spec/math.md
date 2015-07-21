#Math Mode
Math Mode can be entered by using the `m` command.

##Square root
To find the square root of a number, you use `mq`.
```
4 mq o
```
*Outputs `2`*

##Ranges
Range is `mr`.
```
[95 mr]o
```
*Outputs `[9.0, 8.0, 7.0, 6.0, 5.0]`*

Also works the other way around.
```
[59 mr]o
```
*Outputs `[5.0, 6.0, 7.0, 8.0, 9.0]`*

##d
`d` is a little weird, it's like a lame Distance Formula. It pops two values off the stack, squares them, adds them, and finds the square root. It's implemented as such:
```
double y = Math.pow((double) stack.pop(), 2);
double x = Math.pow((double) stack.pop(), 2);
stack.push(Math.sqrt(x + y));
```

##Floor
Floor is `m[`.
```
72/ m[ o
```
*Outputs `3`*

##Ceil
Ceil is `m]`.
```
72/ m] o
```
*Outputs `4`*

##Sine
Sin is `ms`, Arcsin is `mS`.

##Cosine
Coin is `mc`, Arccosin is `mC`.

##Tangent
Tangent is `mt`, Arctangent is `mT`.
