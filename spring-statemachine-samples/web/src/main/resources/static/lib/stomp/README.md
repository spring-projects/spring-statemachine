# STOMP.js

This library provides a STOMP client for Web browser (using Web Sockets) or node.js applications (either using raw TCP sockets or Web Sockets).

## Web Browser support

The library file is located in `lib/stomp.js` (a minified version is available in `lib/stomp.min.js`).
It does not require any dependency (except WebSocket support from the browser or an alternative to WebSocket!)

Online [documentation][doc] describes the library API (including the [annotated source code][annotated]).

## node.js support

Install the 'stompjs' module

    $ npm install stompjs

In the node.js app, require the module with:

    var Stomp = require('stompjs');

To connect to a STOMP broker over a TCP socket, use the `Stomp.overTCP(host, port)` method:

    var client = Stomp.overTCP('localhost', 61613);

To connect to a STOMP broker over a WebSocket, use instead the `Stomp.overWS(url)` method:

    var client = Stomp.overWS('ws://localhost:61614');

## Development Requirements

For development (testing, building) the project requires node.js. This allows us to run tests without the browser continuously during development (see `cake watch`).

    $ npm install

## Building and Testing

[![Build Status](https://secure.travis-ci.org/jmesnil/stomp-websocket.png)](http://travis-ci.org/jmesnil/stomp-websocket)

To build JavaScript from the CoffeeScript source code:

    $ cake build

To run tests:

    $ cake test

To continuously run tests on file changes:

    $ cake watch


## Browser Tests

* Make sure you have a running STOMP broker which supports the WebSocket protocol
 (see the [documentation][doc])
* Open in your web browser the project's [test page](browsertests/index.html)
* Check all tests pass

## Use

The project contains examples for using stomp.js
to send and receive STOMP messages from a server directly in the Web Browser or in a WebWorker.

## Authors

 * [Jeff Mesnil](http://jmesnil.net/)
 * [Jeff Lindsay](http://github.com/progrium)

[doc]: http://jmesnil.net/stomp-websocket/doc/
[annotated]: http://jmesnil.net/stomp-websocket/doc/stomp.html
