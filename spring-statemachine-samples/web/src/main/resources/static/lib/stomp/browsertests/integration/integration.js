$(document).ready(function(){

  var url = "ws://localhost:61623";
  var login = "admin";
  var passcode = "password"
  var destination = "/topic/chat.general";
  
  $("#server_url").text(url);
  $("#queue_name").text(destination);
  
  var client = Stomp.client(url);

  debug = function(str) {
    $("#debug").append(str + "\n");
  };
  client.debug = debug;
  // the client is notified when it is connected to the server.
  var onconnect = function(frame) {
      debug("connected to Stomp");
      client.subscribe(destination,
        function(frame) {
          client.unsubscribe(destination);
          client.disconnect();
        },
        { receipt: 1234 });
  };
  client.onerror = function(frame) {
      debug("connected to Stomp");
  };
  client.ondisconnect = function() {
    debug("disconnected from Stomp");
  };
  client.onreceipt = function() {
    debug("receipt from Stomp");
    client.send(destination, {foo: 1}, "test")
  };

  client.connect(login, passcode, onconnect);

  return false;
});
