module("Stomp Frame");

test("marshall a CONNECT frame", function() {
  var out = Stomp.Frame.marshall("CONNECT", {login: 'jmesnil', passcode: 'wombats'});
  equals(out, "CONNECT\nlogin:jmesnil\npasscode:wombats\n\n\0");
});

test("marshall a SEND frame", function() {
  var out = Stomp.Frame.marshall("SEND", {destination: '/queue/test'}, "hello, world!");
  equals(out, "SEND\ndestination:/queue/test\ncontent-length:13\n\nhello, world!\0");
});

test("marshall a SEND frame without content-length", function() {
  var out = Stomp.Frame.marshall("SEND", {destination: '/queue/test', 'content-length': false}, "hello, world!");
  equals(out, "SEND\ndestination:/queue/test\n\nhello, world!\0");
});

test("unmarshall a CONNECTED frame", function() {
  var data = "CONNECTED\nsession-id: 1234\n\n\0";
  var frame = Stomp.Frame.unmarshall(data).frames[0];
  equals(frame.command, "CONNECTED");
  same(frame.headers, {'session-id': "1234"});
  equals(frame.body, '');
});

test("unmarshall a RECEIVE frame", function() {
  var data = "RECEIVE\nfoo: abc\nbar: 1234\n\nhello, world!\0";
  var frame = Stomp.Frame.unmarshall(data).frames[0];
  equals(frame.command, "RECEIVE");
  same(frame.headers, {foo: 'abc', bar: "1234"});
  equals(frame.body, "hello, world!");
});

test("unmarshall should not include the null byte in the body", function() {
  var body1 = 'Just the text please.',
      body2 = 'And the newline\n',
      msg = "MESSAGE\ndestination: /queue/test\nmessage-id: 123\n\n";

  equals(Stomp.Frame.unmarshall(msg + body1 + '\0').frames[0].body, body1);
  equals(Stomp.Frame.unmarshall(msg + body2 + '\0').frames[0].body, body2);
});

test("unmarshall should support colons (:) in header values", function() {
  var dest = 'foo:bar:baz',
      msg = "MESSAGE\ndestination: " + dest + "\nmessage-id: 456\n\n\0";

  equals(Stomp.Frame.unmarshall(msg).frames[0].headers.destination, dest);
});

test("only the 1st value of repeated headers is used", function() {
  var msg = "MESSAGE\ndestination: /queue/test\nfoo:World\nfoo:Hello\n\n\0";

  equals(Stomp.Frame.unmarshall(msg).frames[0].headers['foo'], 'World');
});

test("Content length of UTF-8 strings", function() {
  equals(0,  Stomp.Frame.sizeOfUTF8());
  equals(0,  Stomp.Frame.sizeOfUTF8(""));
  equals(1,  Stomp.Frame.sizeOfUTF8("a"));
  equals(2,  Stomp.Frame.sizeOfUTF8("ф"));
  equals(3,  Stomp.Frame.sizeOfUTF8("№"));
  equals(15, Stomp.Frame.sizeOfUTF8("1 a ф № @ ®"));
});