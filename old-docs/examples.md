#Examples
Here are a bunch of examples of O. You can find some more advanced examples on [Code Golf](http://codegolf.stackexchange.com/users/41711/phase?tab=answers&sort=newest). You can click the code see to it run [online](http://o-lang.herokuapp.com).

##Hello World
[```
"Hello, World!"o
```](http://o-lang.herokuapp.com/link/code=%22Hello%2C+World!%22o&input=)

##Factorial
[```
H,;]*o
```](http://o-lang.herokuapp.com/link/code=H%2C%3B%5D*o&input=5)

##Cat
[```
io
```](http://o-lang.herokuapp.com/link/code=io&input=This+is+a+Cat+program+in+O!)

##Convert to negabinary
[```
j){n2_bo' o}d
```](http://o-lang.herokuapp.com/link/code=j\)%7Bn2_bo'+o%7Dd&input=7)
This takes an input and prints from 1 to that number in negabinary.

##The sum of the cubes of a list from 1 to n
[```
H,]3^+o
```](http://o-lang.herokuapp.com/link/code=H%2C%5D3%5E%2Bo&input=4)
This takes a list from `0` to `n` (`n` being the input) and cubes each value, then adds them together.

##[Count the number of ones in a number's binary form](http://codegolf.stackexchange.com/questions/47870/count-the-number-of-ones-in-unsigned-16-bit-integer/53462#53462)
[```
H2b~]+o
```](http://o-lang.herokuapp.com/link/code=H2b~%5D%2Bo&input=19)

##String into Number array
[```
M]o
```](http://o-lang.herokuapp.com/link/code=M%5Do&input=123456789)

##[Backwards Addition](http://codegolf.stackexchange.com/questions/53216/backwards-long-addition/53279#53279)
[```
ii`\`e@e@\-{0+}d#\#+`o
```](http://o-lang.herokuapp.com/link/code=ii%60%5C%60e%40e%40%5C-%7B0%2B%7Dd%23%5C%23%2B%60o&input=145%0A98)
