;; Just to boot the server. I need figure out how to do that in a better way
;; Seems that `java -cp clojure.jar clojure.main src/qdis/core.clj -m qdis.core`
;; doesn't work.
(ns qdis.boot
  (:use qdis.core))

(apply -main *command-line-args*)
