#Standard Input/Output
There are many ways to get inputs and produce outputs.

##Inputs
`i` gets input as a String.
```
i1+o
```
*Input: `h`, Output: `h1`*

`j` gets input as a Number.
```
j1+o
```
*Input: `5`, Output: `6`*

`Q` gets input as a Number if the input is a Number, or String if it is a String. It will also set the variable `Q` to what you inputted, just like Pyth's `Q`.
```
QQ+ o
```
*Input: `5`, Output: `10`*

`z` gets input as a String. It will also set the variable `z` to what you inputted, just like Pyth's `z`.
```
zz+ o
```
*Input: `h`, Output: `hh`*

`H` calls `[` and `Q`, so you can save a character when golfing.


##Outputs
`o` prints the top of the stack.
```
'h 'i oo
```
*Outputs `ih`*

`p` prints the top of the stack with a new line.
```
'h 'i pp
```
*Outputs 
```
h
i
```*
