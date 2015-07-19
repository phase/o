#Arrays
Arrays are very powerful in O. With an array open, any time you modify the stack, it actually modifies the array. Here's an example:
```
[123r]o
```
*Outputs `[3.0, 2.0, 1.0]`*

Explanation:
```
[       Start making an array
 123    Push 1, 2, & 3 to the array
    r   Instead of reversing the stack, reverse the array
     ]o Output the array
```

##*
You can multiply all the contents of an Array together with `*`.
```
[123]*
```
This pops the stack off and pushes `6`

##+
Same as `*`, except addition.
```
[123]+o
```
*Outputs `6`*
##-
Same as `*`, except subtraction.
```
[531]-o
```
*Outputs `1`*
##/
Same as `*`, except division.
```
[Z57]/o
```
*Outputs `1`*
