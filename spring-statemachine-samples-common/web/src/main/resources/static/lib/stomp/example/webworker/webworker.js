importScripts ("/stomp.js");

// *WebWorker* onmessage implementation
onmessage = function (event) {
  var url = event.data.url;
  var login = event.data.login;
  var passcode = event.data.passcode;
  var destination = event.data.destination;
  var text = event.data.text;

  // create the Stomp client
  var client = Stomp.client(url);
  
  // connect to the server
  client.connect(login, passcode, function(frame) {
    // upon connection, subscribe to the destination
    var sub = client.subscribe(destination, function(message) {
      // when a message is received, post it to the current WebWorker
      postMessage("WebWorker: " + message.body);
      //... unsubscribe from the destination
      sub.unsubscribe();
      //... and disconnect from the server
      client.disconnect();
    });
    // send the text to the destination
    client.send(destination, {}, text);
  });
};
