2. The Basics
=============

O revolves around one stack in which you can push and pop values to and from. Here are some basic rules for writing O code.

2.1. I/O
--------

There are a couple ways to get a user's input. ``i`` will push the input to the stack as a string, while ``j`` will push the input to the stack as an integer. ``o`` pops the top value off the stack and outputs it. ``p`` outputs with a new line. Here's an example::

    >>> io
    test
    test

This is the smallest cat program possible. ``i`` gets the input and pushes it, ``o`` pops it and outputs it to stdout.

Another cool feature of O is that the stack contents will be outputted when the code finishes execution. Meaning ``1234`` will output ``1234``, while ``1234oooo`` will output ``4321``. This feature does not work in the REPL.

2.1.1. Input as a number
~~~~~~~~~~~~~~~~~~~~~~~~

``j`` will parse the input as a number, and throw an error if otherwise. ::

    >>> j5+o
    5
    10

``q`` will push the input as a number if it is one, and push it as a string if otherwise. ::

    >>> q5+o
    5
    10
    >>> q5+o
    hi
    hi5

``Q`` will do the same except assign it to a variable called *Q*. You will learn about variables more later.

2.2. Number Literals are pushes individually
--------------------------------------------

``1234`` doesn't push the number *one thousand two hundred thirty-four*, it pushes ``1``, then ``2``, ``3``, and finally ``4``. Each individual digit is pushed to the stack::

    >>> 1234oooo
    4321

Each ``o`` pops one number off and outputs it, meaning the number that was pushed last will pop off first.

2.2.1. Hexadecimal works too
~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Hexadecimal numbers work for capital notation. Lowercase notation is saved for other functions. ::

    >>1Aoo
    101

``1`` was pushed to the stack, then ``A`` pushed ``10`` to the stack, and then they were outputted.

2.3. Strings are enclosed in quotes
-----------------------------------

To make a string, you just need to enclose it within quotes. ::

    >>> "Hello, World!"o
    Hello, World!

What about printing something different? ::

    >>> "Hello'World!"oo
    World!Hello


2.3.1 Strings don't need quotes at all
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Remember when I said you need to put them in quotes? I lied. ``'`` has some interesting properties with strings. When used by itself, it will push the next character to the stack as a string.

    >>> 'ao
    a

If you are in the middle of making a string, it will push the current string buffer to the stack and start making a new string (like a macro for ``""``).

    >>> "a'a"oo
    aa
    >>> "hello'madam"oo
    madamhello

The next section will cover basic arithmetic.

2.3.2 Strings don't need quotes all the time
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

If a string is at the end of a file, you don't need to put a quote at the end. ::

    "Hello, World!

Put that in a file and it will print::

    Hello, World!
