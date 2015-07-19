#Control Flow
Control Flow in **O** isn't the most intuitive, but it's simple to use.

##An object's truthness
You can use any type of object in control flow, so there's got to be a way to check if the object is truthful!

if the object is a String:

* `true` if the String isn't `""`
* `false` if the String *is* `""`

if the object is a Number:

* `true` if the Number is greater than `0`
* `false` if the Number is less than or equal to `0`

if the object is an Array:

* `true` if the Array's size is greater than `0`
* `false` if the Array's size is `0`

if the object is a Dictionary:

* `true` if the Dictionary's size is greater than `0`
* `false` if the Dictionary's size is `0`

if the object is a CodeBlock:

* run the CodeBlock, pop the top value, and return the truthness of that object

##if
If statements are done using `?`. It will pop 3 values off the stack: an object to check true, a true-CodeBlock, and a false-CodeBlock. If the object is true, it will run the true-CodeBlock; if it's false, it will run the false-CodeBlock.
```
1 {'ao} {'bo} ?
```
*Outputs `a`*


##for
For loops aren't called *for loops* in **O**, they're called **do loops**, as in: *do {} n times*.
```
5 {'ho} d
```
*Outputs `hhhhh`*

You can also get the variable inside the for loop using `n`.
```
5 {no} d
```
*Outputs `01234`*

##while
While loops are made using `w`. They will run the block, pop the stack, and repeat if the value is *true*.
```
9.{.o(.}w
```
*Outputs `987654321`.
