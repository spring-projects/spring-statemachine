module("Stomp Connection");

test("Connect to an invalid Stomp server", function() {
  
  var client = Stomp.client(TEST.badUrl);
  client.connect("foo", "bar", 
    function() {},
    function() {
      start();
    });
  stop(TEST.timeout);
});

test("Connect to a valid Stomp server", function() {
  
  var client = Stomp.client(TEST.url);
  client.connect(TEST.login, TEST.password, 
    function() {
      start();
    });
  stop(TEST.timeout);
});

test("Disconnect", function() {
  
  var client = Stomp.client(TEST.url);
  client.connect(TEST.login, TEST.password, 
    function() {
      // once connected, we disconnect
      client.disconnect(function() {
        start();
      });
    });
  stop(TEST.timeout);
});