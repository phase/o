#File I/O

`f` puts the interpreter into file mode, meaning the next command does
something with files.

## Getting data from a file

Let's say you have a file named `stuff.txt` and you want to get the text
from that file. You can using `fi`

`stuff.txt` contains:
```
I like cheese
```

We'll use this to push the contents of the file to the stack (assuming
that `stuff.txt` is in the same directory):
```
"stuff.txt"fi
```

The stack now contains the string “I like cheese”.

## Putting data in a file

Using `fo`, we can overwrite whatever is in a file with what is on the
stack.
```
"I don't like cheese""stuff.txt"fo
```

Anything that was in `stuff.txt` is now gone, and it only contains “I
don't like cheese”. `fo` will only put the second item on the stack into
the file, no other elements will be affected.
