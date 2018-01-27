# Overview

This is a translation of the [scheme compiler](https://gist.github.com/nyuichi/1116686) by Marc Feeley (author of [gambit](http://gambitscheme.org/) into Clojure. More details about Marc's compiler can be found in this [presentation](http://churchturing.org/y/90-min-scc.pdf) and these videos [part1](https://www.youtube.com/watch?v=Bp89aBm9tGU) and [part2](https://www.youtube.com/watch?v=M4dwcdK5bxE). This is my attempt at trying to understand the implementation details. Perhaps the implementation being in Clojure would make it accessible to more people.


## Why would someone do this?

While Marc's "90 minutes scheme to C" is simply brilliant and it's under 800 lines of code (and mind you, all lines are less than 80 chars long :)) - I found it hard to "get it". For example, I was a little stumped by define-type to start with. The videos and the presentation helped me understand "CPS conversion" and "Closure convesion" but when it came to code generation, it was not so clear.

Another "problem" with Marc's implementation is that it is in scheme. What I mean is that, you need a scheme interpreter/compiler to run the compiler. If you are on Linux, this is not an issue but it become a problem if you are on other platforms. Since I have to spend a lot of my time on Windows, it becomes challenging to set up a scheme compiler that could compile Marc's implementation and also allow me to tweak it to gain understanding.


## Why Clojure?

Ofcourse, this would be a question only for those don't know [Clojure](http://www.clojure.org) :)
