# qdis

Qdis is a WIP for a message-oriented middleware which provides a Web API and is back-ended by Redis. Of course, today it is nothing serious, it is just a playground to relax and try Clojure a little bit, which definetely is lot of fun.

## Install

    $ git clone https://leandrosilva@github.com/leandrosilva/qdis.git
    $ cd qdis
    $ lein deps

## Getting started

**1º Terminal)** Start a Redis server

**2º Terminal)** Start Qdis in development mode:

    $ ./bin/qdis

**3º Terminal)** Run one of the test scripts:

    $ ./test/script/enqueue-dequeue.sh

**4º Terminal)** Start a Redis client and see the generated log:

    redis 127.0.0.1:6379> keys *

## Configuration

If you want to try a different configuration based on a given runtime environment:

1) Edit one of the config files which are:

    development.clj
    test.clj
    integration.clj
    production.clj

2) Start Qdis pointing the given environment:

    $ ./bin/qdis --env development
    $ ./bin/qdis --env test
    $ ./bin/qdis --env integration
    $ ./bin/qdis --env production

## License

Copyright (C) 2010 FIXME

Distributed under the Eclipse Public License, the same as Clojure.
