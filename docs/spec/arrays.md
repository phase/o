#Arrays
Arrays are very powerful in O. With an array open, any time you modify the stack, it actually modifies the array. Here's an example:
```
[123r]o
```
*Outputs `[3, 2, 1]`*

Explanation:
```
[       Start making an array
 123    Push 1, 2, & 3 to the array
    r   Instead of reversing the stack, reverse the array
     ]o Output the array
```

##(
You can reopen an Array with `(`.
```
[12].p(3]p
```
*Outputs:*
```
[1, 2]
[1, 2, 3]
```

##*
You can multiply all the contents of an Array together with `*`. This will fold right.
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

##^
You can insert the contents of one string inbetween the characters of another string with `^`.
```
["abc" "d"]^o
```
*Outputs: `adbdcd`*

```
["abc" "def_"]^o
```
*Outputs: `adef_bdef_cdef_`*

Having more than one String will make your output more complex.

```
["abc" "2_" "3-"]^o
```
*Outputs: `a3-23-_3-b3-23-_3-c3-23-_3-`*
