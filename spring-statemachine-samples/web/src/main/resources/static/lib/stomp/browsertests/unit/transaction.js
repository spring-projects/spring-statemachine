module("Stomp Transaction");

test("Send a message in a transaction and abort", function() {
  
  var body = Math.random();
  var body2 = Math.random();
  
  var client = Stomp.client(TEST.url);
  
  client.debug = TEST.debug;
  client.connect(TEST.login, TEST.password,
    function() {
      client.subscribe(TEST.destination, function(message)
      {
        start();
        // we should receive the 2nd message outside the transaction
        equals(message.body, body2);
        client.disconnect();
      });
      
      var tx = client.begin("txid_" + Math.random());
      client.send(TEST.destination, {transaction: tx.id}, body);
      tx.abort();
      client.send(TEST.destination, {}, body2);
    });
    stop(TEST.timeout);
});

test("Send a message in a transaction and commit", function() {
  
  var body = Math.random();
  
  var client = Stomp.client(TEST.url);
  
  client.debug = TEST.debug;
  client.connect(TEST.login, TEST.password,
    function() {
      client.subscribe(TEST.destination, function(message)
      {
        start();
        equals(message.body, body);
        client.disconnect();
      });
      var tx = client.begin();
      client.send(TEST.destination, {transaction: tx.id}, body);
      tx.commit();
    });
    stop(TEST.timeout);
});

test("Send a message outside a transaction and abort", function() {

  var body = Math.random();

  var client = Stomp.client(TEST.url);

  client.debug = TEST.debug;
  client.connect(TEST.login, TEST.password,
    function() {
      client.subscribe(TEST.destination, function(message)
      {
        start();
        // we should receive the message since it was sent outside the transaction
        equals(message.body, body);
        client.disconnect();
      });

      var tx = client.begin();
      client.send(TEST.destination, {}, body);
      tx.abort();
    });
    stop(TEST.timeout);
});