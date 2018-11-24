![](https://clojars.org/perun-selmer/latest-version.svg)

# perun-selmer

[](dependency)
```clojure
[perun-selmer "0.1.0"] ;; latest release
```
[](/dependency)

A boot task that uses `io.perun` to process `selmer` templates.

## Usage

    (require '[perun-selmer.core :refer [selmer]])

and use the `(selmer)` task in your build pipeline
to process `.selmer` files into `.html` (with yaml front matter).

The `(selmer*)` task does not process yaml metadata.

Each take the following options (with these defaults):

      {:out-dir "public"
       :out-ext ".html"
       :extensions [".selmer"]
       :filterer identity
       :meta {:original true
              :include-rss true
              :include-atom true}}

## License

Copyright © 2018 Randy Stauner

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
