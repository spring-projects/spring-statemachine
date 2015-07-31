#WebWorker Example Using STOMP Over WebSocket

This example uses a WebWorker to send and receive STOMP messages from the broker.

The main page creates a WebWorker and post all the configuration to connect to the STOMP broker and send a text message.
When the WebWorker will receive a message, it will inform the main page using its `postMessage` method. 

##Running the Example

run in the `example`parent directory:

    $ node server.js
  
and go to [/webworker/](http://localhost:8080/webworker/).
