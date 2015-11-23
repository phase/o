**This document is just for reference when checking if a character is in use (because I forget a lot :P)
For real documentation, please see [o.readthedocs.org](http://o.readthedocs.org)**

##Comands
In the format `HEX (char): use`. Things in bold need to be checked in the interpreter because they either aren't implemented or are implemented improperly.

* _21_ (`!`): 
* _22_ (`"`): String parsing
* _23_ (`#`): String to number
* _24_ (`$`): Take object from lower stack up
* _25_ (`%`): Modulous
* _26_ (`&`): **something with maps?**
* _27_ (`'`): Character literal
* _28_ (`(`): Decrement
* _29_ (`)`): Increment
* _2a_ (`*`): Multiplication
* _2b_ (`+`): Addition
* _2c_ (`,`): Range
* _2d_ (`-`): Subtraction
* _2e_ (`.`): Clone the top of the stack
* _2f_ (`/`): Division
* _30_ (`0`): Push 0
* _31_ (`1`): Push 1
* _32_ (`2`): Push 2
* _33_ (`3`): Push 3
* _34_ (`4`): Push 4
* _35_ (`5`): Push 5
* _36_ (`6`): Push 6
* _37_ (`7`): Push 7
* _38_ (`8`): Push 8
* _39_ (`9`): Push 9
* _3a_ (`:`): Assign to variable
* _3b_ (`;`): Pop top value of stack
* _3c_ (`<`): Less than
* _3d_ (`=`): Equal to
* _3e_ (`>`): Greater than
* _3f_ (`?`): If????
* _40_ (`@`): Rotate top three items on stack
* _41_ (`A`): Push 10
* _42_ (`B`): Push 11
* _43_ (`C`): Push 12
* _44_ (`D`): Push 13
* _45_ (`E`): Push 14
* _46_ (`F`): Push 15
* _47_ (`G`): Push alphabet
* _48_ (`H`): Macro for `[Q`
* _49_ (`I`): Macro for `[i`
* _4a_ (`J`): Magic var
* _4b_ (`K`): Magic var
* _4c_ (`L`): Lambda (Push next character as a standalone CodeBlock: `Lo` -> `{o}`)
* _4d_ (`M`): Macro for `[i~`
* _4e_ (`N`): Push blank CodeBlock
* _4f_ (`O`): 
* _50_ (`P`): 
* _51_ (`Q`): Input var
* _52_ (`R`): 
* _53_ (`S`): Blank String
* _54_ (`T`): String with space `" "`
* _55_ (`U`): Newline string `"\n"`
* _56_ (`V`): _Commonly used for variables_
* _57_ (`W`): Push 32
* _58_ (`X`): Push 33
* _59_ (`Y`): Push 34
* _5a_ (`Z`): Push 35
* _5b_ (`[`): Start array
* _5c_ (`\`): Swap two objects on stack
* _5d_ (`]`): End array
* _5e_ (`^`): **Power?**
* _5f_ (`_`): Negate
* _60_ (`` ` ``): Reverse String
* _61_ (`a`): 
* _62_ (`b`): 
* _63_ (`c`): 
* _64_ (`d`): For loop
* _65_ (`e`): Is even? / String length
* _66_ (`f`): 
* _67_ (`g`): 
* _68_ (`h`): 
* _69_ (`i`): String input
* _6a_ (`j`): Number input
* _6b_ (`k`): 
* _6c_ (`l`): Push length of stack
* _6d_ (`m`): Math functions
* _6e_ (`n`): Used in for loops
* _6f_ (`o`): Print object
* _70_ (`p`): Print object with new line
* _71_ (`q`): Push input as string/number
* _72_ (`r`): 
* _73_ (`s`): Split string into char array
* _74_ (`t`): 
* _75_ (`u`): 
* _76_ (`v`): 
* _77_ (`w`): While Loop
* _78_ (`x`): 
* _79_ (`y`): 
* _7a_ (`z`): 
* _7b_ (`{`): Start CodeBlock
* _7c_ (`|`): 
* _7d_ (`}`): End CodeBlock
* _7e_ (`~`): Eval