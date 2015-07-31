module("Stomp Acknowledgement");

test("Subscribe using client ack mode, send a message and ack it", function() {
  
  var body = Math.random();
  
  var client = Stomp.client(TEST.url);
  
  client.debug = TEST.debug;
  client.connect(TEST.login, TEST.password, function() {
    var onmessage = function(message) {
      start();        
      // we should receive the 2nd message outside the transaction
      equals(message.body, body);
      var receipt = Math.random();
      client.onreceipt = function(frame) {
        equals(receipt, frame.headers['receipt-id'])
        client.disconnect();
      }
      message.ack({'receipt': receipt});
    }
    var sub = client.subscribe(TEST.destination, onmessage, {'ack': 'client'});      
    client.send(TEST.destination, {}, body);
  });
  stop(TEST.timeout);
});

test("Subscribe using client ack mode, send a message and nack it", function() {
  
  var body = Math.random();
  
  var client = Stomp.client(TEST.url);
  
  client.debug = TEST.debug;
  client.connect(TEST.login, TEST.password, function() {
    var onmessage = function(message) {
      start();        
      equals(message.body, body);
      var receipt = Math.random();
      client.onreceipt = function(frame) {
        equals(receipt, frame.headers['receipt-id'])
        client.disconnect();
      }
      message.nack({'receipt': receipt});
    }
    var sub = client.subscribe(TEST.destination, onmessage, {'ack': 'client'});      
    client.send(TEST.destination, {}, body);
  });
  stop(TEST.timeout);
});
