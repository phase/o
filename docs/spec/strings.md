#Strings
Strings can be modified in a load of different ways.

##+
You can concatenate Strings together using `+`.
```
"Hi" " bob" +o
```
*Outputs `Hi bob`*

If one of the objects is a number, it will be treated as a string.
```
9 "Hi" + o
```
*Outputs `9Hi`*

##-
Every instance of the second String in the first String will be removed.
```
"Hello&Aunt&Sue&And&Joe" "&" - o
```
*Outputs `HelloAuntSueAndJoe`*

Also works with numbers.
```
"10011010101" 0-o
```
*Outputs `111111`*

##*
The String will be multiplied that many times.
```
"Ha"5*o
```
*Outputs `HaHaHaHaHa`*

##/
This splits the String into multiple String objects.
```
"A_B_C_D_E_F" '_ /
```

Stack contents:

* F
* E
* D
* C
* B
* A

##e
Pushes the length of the String, without popping off the String.
```
"hi"e oo
```
*Outputs `2hi`*

##`#`
Casts the String to a base 10 number.
```
"50"#o
```
*Outputs `50`*

If it fails, it pushes the String's hashcode.
```
'a#o
```
*Outputs `97`*

##`
Reverses the String.
```
"This is going to be reversed"`o
```
*Outputs `desrever eb ot gniog si sihT`*

##=
Checks if to strings are equal.
```
"This" "This" =o
```
*Outputs `1`*

```
"This" "isnt" =o
```
*Outputs `0`*

##>
Checks if string `a` is in string `b`.
```
"%" "this%" >o
```
*Outputs `1`*

```

##<
Checks if string `b` is in string `a`.
```
"this%" "%" <o
```
*Outputs `1`*

```