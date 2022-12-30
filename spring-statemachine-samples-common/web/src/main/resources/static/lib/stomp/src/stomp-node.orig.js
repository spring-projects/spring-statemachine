var Stomp = require("./stomp");
var net   = require('net');

var wrapTCP = function(port, host) {
  // node.js net.Socket;
  var socket;

  // Web Socket-like object
  var ws = {
    url: 'tcp:// ' + host + ':' + port,
    send: function(d) {
      socket.write(d);
    },
    close: function() {
      socket.end();
    }
  };

  socket = net.connect(port, host, function(e) {
    ws.onopen();
  });
  socket.on('error', function(e) {
    // handler can be null if the ws is properly closed
    if (ws.onclose) {
      ws.onclose(e);
    }
  });
  socket.on('close', function(e) {
    // handler can be null if the ws is properly closed
    if (ws.onclose) {
      ws.onclose();
    }
  });
  socket.on('data', function(data) {
    // wrap the data in an event object
    var event = {
      'data': data.toString()
    };
    ws.onmessage(event);
  });
  
  return ws;
};

var wrapWS = function(url) {
  var WebSocketClient = require('websocket').client;

  var connection;

  var ws = {
    url: url,
    send : function(d) {
      connection.sendUTF(d);
    },
    close : function() {
      connection.close();
    }
  };
  
  var socket = new WebSocketClient();
  socket.on('connect', function(conn) {
      connection = conn;
      ws.onopen();
      connection.on('error', function(error) {
        if (ws.onclose) {
          ws.onclose(error);
        }
      });
      connection.on('close', function() {
        if (ws.onclose) {
          ws.onclose();
        }
      });
      connection.on('message', function(message) {
          if (message.type === 'utf8') {
            // wrap the data in an event object
            var event = {
              'data': message.utf8Data
            };
            ws.onmessage(event);
          }
      });
  });

  socket.connect(url);
  return ws;
}

var overTCP = function(host, port) {
  var socket = wrapTCP(port, host);
  return Stomp.Stomp.over(socket);
}

var overWS= function(url) {
  var socket = wrapWS(url);
  return Stomp.Stomp.over(socket);
}

exports.overTCP = overTCP;
exports.overWS = overWS;
