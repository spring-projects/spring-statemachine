(function() {
  module("Parse connect method arguments", {

    setup: function() {
      // prepare something for all following tests
      myConnectCallback = function() {
        // called back when the client is connected to STOMP broker
      };

      myErrorCallback = function() {
        // called back if the client can not connect to STOMP broker
      };

      client = Stomp.client(TEST.url);
      
      checkArgs = function(args, expectedHeaders, expectedConnectCallback, expectedErrorCallback) {
        var headers = args[0];
        var connectCallback = args[1];
        var errorCallback = args[2];

        deepEqual(headers, expectedHeaders);
        strictEqual(connectCallback, expectedConnectCallback);
        strictEqual(errorCallback, expectedErrorCallback);        
      }
    }
  });

  test("connect(login, passcode, connectCallback)", function() {
    checkArgs(
      client._parseConnect("jmesnil", "wombats", myConnectCallback),
      
      {login: 'jmesnil', passcode: 'wombats'},
      myConnectCallback,
      undefined);
  });

  test("connect(login, passcode, connectCallback, errorCallback)", function() {
    checkArgs(
      client._parseConnect("jmesnil", "wombats", myConnectCallback, myErrorCallback),
      
      {login: 'jmesnil', passcode: 'wombats'},
      myConnectCallback,
      myErrorCallback);
  });

  test("connect(login, passcode, connectCallback, errorCallback, vhost)", function() {
    checkArgs(
      client._parseConnect("jmesnil", "wombats", myConnectCallback, myErrorCallback, "myvhost"),
      
      {login: 'jmesnil', passcode: 'wombats', host: 'myvhost'},
      myConnectCallback,
      myErrorCallback);
  });

  test("connect(headers, connectCallback)", function() {
    var headers = {login: 'jmesnil', passcode: 'wombats', host: 'myvhost'};

    checkArgs(
      client._parseConnect(headers, myConnectCallback),
      
      headers,
      myConnectCallback,
      undefined);
  });

  test("connect(headers, connectCallback, errorCallback)", function() {
    var headers = {login: 'jmesnil', passcode: 'wombats', host: 'myvhost'};

    checkArgs(
      client._parseConnect(headers, myConnectCallback, myErrorCallback),
      
      headers,
      myConnectCallback,
      myErrorCallback);
  });
})();
