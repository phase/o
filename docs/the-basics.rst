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

2.2. Number Literals are pushes individually
--------------------------------------------

``1234`` doesn't push the number *one thousand twenty-four*, it pushes ``1``, then ``2``, ``3``, and finally ``4``. Each individual digit is pushed to the stack.