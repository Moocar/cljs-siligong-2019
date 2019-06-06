## Clojurescript for Javascript ppl

Code for the presentation I gave at [Wollongong's Siligong Valley JS meetup](https://www.meetup.com/SiligongValley/events/260356994/) on June 6, 2019.

## Technologies used:

- [jobim](https://github.com/MysteryMachine/jobim) for the clojurescript presentation library
- [reagent](https://reagent-project.github.io/) for custom slides
- [figwheel](https://figwheel.org/) for interactive cljs development

## To Run (on Mac)

First, clone this repo and cd into it. Now

### Leiningen

If this is your first time running clojure code, you'll first need to install Clojure's most popular package manager, [leiningen](https://leiningen.org/).

```
brew install leiningen
```

### Download dependencies

In the repo directory, run

```
lein install
```

This will download the internet

### Start figwheel

Figwheel is the hot reloading framework for clojurescript. Start it by running

```
lein figwheel
```

This will compile your clojurescript, and create a repl. When it's ready (you might see a `dev:cljs.user=> ` prompt), then open your browser to [localhost:3449](http://localhost:3449)

If all went according to plan, you should see the presentation. You can navigate forward and backward using the left and right arrows.

### The REPL

Your terminal now has a full clojurescript repl. Try typing some things:

```
(+ 1 2)
```

You should see the result `3`!

```
(js/alert "Boom")
```

### Live reloading

The entire code for the talk is in `src/cljs_siligong_2019/show.cljs`. Look through it and try changing the text of some slides and notice that you don't have to reload to see the changes, and that the state of the application is kept. This is the magic of figwheel.
