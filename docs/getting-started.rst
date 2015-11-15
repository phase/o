1. Getting Started
==================
Getting started in O is easy! Just follow the steps below and you'll be ready to go!

1.1 Getting the interpreter
---------------------------
You can download latest release from https://github.com/phase/o/releases or clone the repository and compile it manually::

    git clone https://github.com/phase/o && cd o
    gcc o.c -o o
    ./o

Running the executable without any arguments will open the REPL, which you can type lines of O code into to have them interpreted.

You can also go to http://o-lang.herokuapp.com and use the always-updated-interpreter so you won't have to recompile every update.

1.2 Let's test it out!
----------------------
Whether you're on the online IDE or using the REPL, this *Hello World* program will run on both::

    "Hello, World!"o

This is the simplest *Hello World* program in O. It pushes the string ``Hello, World!`` to the stack and outputs it with ``o``.