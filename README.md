# baller

A 2D baller ball bouncing game written in [ClojureScript](https://github.com/clojure/clojurescript) using the [InfiniteLives](https://github.com/infinitelives) library for [Lisp Game Jam 2017](https://itch.io/jam/lisp-game-jam-2017-easy-mode)

[PLAY NOW!](https://prodge.github.io/baller/)

![Menu Screen](https://github.com/Prodge/baller/blob/master/screenshot.png?raw=true "Menu Screen")
![Playing Screen](https://github.com/Prodge/baller/blob/master/screenshot2.png?raw=true) "Playing Screen")

## Setup

To get an interactive development environment run:

    lein figwheel

and open your browser at [localhost:3449](http://localhost:3449/).
This will auto compile and send all changes to the browser without the
need to reload. After the compilation process is complete, you will
get a Browser Connected REPL. An easy way to try it is:

    (js/alert "Am I connected?")

and you should see an alert in the browser window.

To clean all compiled files:

    lein clean

To create a production build run:

    lein do clean, cljsbuild once min

And open your browser in `resources/public/index.html`. You will not
get live reloading, nor a REPL.

## License

Copyright © Prodge

Distributed under the Eclipse Public License either version 1.0 or (at your option) any later version.
