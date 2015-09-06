# Contributing to O

The first thing you want to do is learn where the main files are.
```
src/
    O.java      -> Main Parser & REPL
    WebIDE.java -> Web IDE App
res/            -> HTML Pages w/ JS
docs/           -> ReadTheDocs Pages
```

When you create a PR, the build will be run through [Travis](https://travis-ci.org/phase/o) and [Heroku](http://o-lang.herokuapp.com/). Your PR's app will be in the format `o-lang-pr-#.herokuapp.com`, where you can test out the IDE.

## New Features
When adding a new feature, please thoroughly explain what it does and how it can be used effectively with examples.

Ex:

_My pull request adds a `+` operator that will add two numbers together._

```
>>> 1 2 + p
3
>>> 5 2 6 + + p
13
```

_I believe by addition to O will be good for golfing because adding two numbers together is usually pretty useful when golfing._
