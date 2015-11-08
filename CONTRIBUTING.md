# Contributing to O

The first thing you want to do is learn where the main files are.
```
o2.c   -> Main Parser & REPL
res/   -> HTML Pages w/ JS
docs/  -> ReadTheDocs Pages
```

When you create a PR, the build will be run through [Travis](https://travis-ci.org/phase/o) and [Heroku](http://o-lang.herokuapp.com/). Your PR's app will be in the format `o-lang-pr-#.herokuapp.com`, where you can test out the IDE.

## Bug Reports/Help
Please [create an issue](https://github.com/phase/o/issues/new) with a decent title and a description of your problem with steps to reproduce it and a link to the WebIDE.

## New Features
If you have an idea for a new feature, but have no idea how to Java, you can [create an issue](https://github.com/phase/o/issues/new) detailing how your feature would work with examples.

When adding a new feature, please thoroughly explain what it does and how it can be used effectively with examples from the current WebIDE or your PR's.

Ex:

_My pull request adds a `+` operator that will pop two numbers off the stack and push their sum. [Example](http://o-lang.herokuapp.com/link/code=12%2Bp&input=)_

```
>>> 1 2 + p
3
>>> 5 2 6 + + p
13
```

_I believe by addition to O will be good for golfing because adding two numbers together is usually pretty useful when golfing._
