var client = null;

module("Stomp Subscription", {
  setup: function() {
    client = Stomp.client(TEST.url);
    client.debug = TEST.debug;
  },

  teardown: function() {
    client.disconnect();
  }
});

test("Should receive messages sent to destination after subscribing", 1, function() {
  var msg = 'Is anybody out there?';

  client.connect(TEST.login, TEST.password, function() {
    client.subscribe(TEST.destination, function(frame) {
      start();
      equals(frame.body, msg);
    });

    client.send(TEST.destination, {}, msg);
  });

  stop(TEST.timeout);
});

test("Should no longer receive messages after unsubscribing to destination", 1, function() {
  var msg1 = 'Calling all cars!',
      subscription1 = null,
      subscription2 = null;

  client.connect(TEST.login, TEST.password, function() {
    subscription1 = client.subscribe(TEST.destination, function(frame) {
      start();
      ok(false, 'Should not have received message!');
    });

    subscription2 = client.subscribe(TEST.destination, function(frame) {
      start();
      equals(frame.body, msg1);
    });

    subscription1.unsubscribe();
    client.send(TEST.destination, {}, msg1);
  });

  stop(TEST.timeout);
});
