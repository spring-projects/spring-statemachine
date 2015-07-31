
# **STOMP Over Web Socket** is a JavaScript STOMP Client using
# [HTML5 Web Sockets API](http://www.w3.org/TR/websockets).
#
# * Copyright (C) 2010-2012 [Jeff Mesnil](http://jmesnil.net/)
# * Copyright (C) 2012 [FuseSource, Inc.](http://fusesource.com)
#
# This library supports:
#
# * [STOMP 1.0](http://stomp.github.com/stomp-specification-1.0.html)
# * [STOMP 1.1](http://stomp.github.com/stomp-specification-1.1.html)
#
# The library is accessed through the `Stomp` object that is set on the `window`
# when running in a Web browser.

###
   Stomp Over WebSocket http://www.jmesnil.net/stomp-websocket/doc/ | Apache License V2.0

   Copyright (C) 2010-2013 [Jeff Mesnil](http://jmesnil.net/)
   Copyright (C) 2012 [FuseSource, Inc.](http://fusesource.com)
###

# Define constants for bytes used throughout the code.
Byte =
  # LINEFEED byte (octet 10)
  LF: '\x0A'
  # NULL byte (octet 0)
  NULL: '\x00'

# ##[STOMP Frame](http://stomp.github.com/stomp-specification-1.1.html#STOMP_Frames) Class
class Frame
  # Frame constructor
  constructor: (@command, @headers={}, @body='') ->

  # Provides a textual representation of the frame
  # suitable to be sent to the server
  toString: ->
    lines = [@command]
    skipContentLength = if (@headers['content-length'] == false) then true else false
    delete @headers['content-length'] if skipContentLength

    for own name, value of @headers
      lines.push("#{name}:#{value}")
    if @body && !skipContentLength
      lines.push("content-length:#{Frame.sizeOfUTF8(@body)}")
    lines.push(Byte.LF + @body)
    return lines.join(Byte.LF)

  # Compute the size of a UTF-8 string by counting its number of bytes
  # (and not the number of characters composing the string)
  @sizeOfUTF8: (s)->
    if s
      encodeURI(s).match(/%..|./g).length
    else
      0

  # Unmarshall a single STOMP frame from a `data` string
  unmarshallSingle= (data) ->
    # search for 2 consecutives LF byte to split the command
    # and headers from the body
    divider = data.search(///#{Byte.LF}#{Byte.LF}///)
    headerLines = data.substring(0, divider).split(Byte.LF)
    command = headerLines.shift()
    headers = {}
    # utility function to trim any whitespace before and after a string
    trim= (str) ->
      str.replace(/^\s+|\s+$/g,'')
    # Parse headers in reverse order so that for repeated headers, the 1st
    # value is used
    for line in headerLines.reverse()
      idx = line.indexOf(':')
      headers[trim(line.substring(0, idx))] = trim(line.substring(idx + 1))
    # Parse body
    # check for content-length or  topping at the first NULL byte found.
    body = ''
    # skip the 2 LF bytes that divides the headers from the body
    start = divider + 2
    if headers['content-length']
      len = parseInt headers['content-length']
      body = ('' + data).substring(start, start + len)
    else
      chr = null
      for i in [start...data.length]
        chr = data.charAt(i)
        break if chr is Byte.NULL
        body += chr
    return new Frame(command, headers, body)

  # Split the data before unmarshalling every single STOMP frame.
  # Web socket servers can send multiple frames in a single websocket message.
  # If the message size exceeds the websocket message size, then a single
  # frame can be fragmented across multiple messages.
  #
  # `datas` is a string.
  #
  # returns an *array* of Frame objects
  @unmarshall: (datas) ->
    # Ugly list comprehension to split and unmarshall *multiple STOMP frames*
    # contained in a *single WebSocket frame*.
    # The data is split when a NULL byte (followed by zero or many LF bytes) is
    # found
    frames = datas.split(///#{Byte.NULL}#{Byte.LF}*///)

    r =
      frames:  []
      partial: ''
    r.frames = (unmarshallSingle(frame) for frame in frames[0..-2])

    # If this contains a final full message or just a acknowledgement of a PING
    # without any other content, process this frame, otherwise return the
    # contents of the buffer to the caller.
    last_frame = frames[-1..][0]

    if last_frame is Byte.LF or (last_frame.search ///#{Byte.NULL}#{Byte.LF}*$///) isnt -1
      r.frames.push(unmarshallSingle(last_frame))
    else
      r.partial = last_frame
    return r

  # Marshall a Stomp frame
  @marshall: (command, headers, body) ->
    frame = new Frame(command, headers, body)
    return frame.toString() + Byte.NULL

# ##STOMP Client Class
#
# All STOMP protocol is exposed as methods of this class (`connect()`,
# `send()`, etc.)
class Client
  constructor: (@ws) ->
    @ws.binaryType = "arraybuffer"
    # used to index subscribers
    @counter = 0
    @connected = false
    # Heartbeat properties of the client
    @heartbeat = {
      # send heartbeat every 10s by default (value is in ms)
      outgoing: 10000
      # expect to receive server heartbeat at least every 10s by default
      # (value in ms)
      incoming: 10000
    }
    # maximum *WebSocket* frame size sent by the client. If the STOMP frame
    # is bigger than this value, the STOMP frame will be sent using multiple
    # WebSocket frames (default is 16KiB)
    @maxWebSocketFrameSize = 16*1024
    # subscription callbacks indexed by subscriber's ID
    @subscriptions = {}
    @partialData = ''

  # ### Debugging
  #
  # By default, debug messages are logged in the window's console if it is defined.
  # This method is called for every actual transmission of the STOMP frames over the
  # WebSocket.
  #
  # It is possible to set a `debug(message)` method
  # on a client instance to handle differently the debug messages:
  #
  #     client.debug = function(str) {
  #         // append the debug log to a #debug div
  #         $("#debug").append(str + "\n");
  #     };
  debug: (message) ->
    window?.console?.log message
      
  # Utility method to get the current timestamp (Date.now is not defined in IE8)
  now= ->
    if Date.now then Date.now() else new Date().valueOf
  
  # Base method to transmit any stomp frame
  _transmit: (command, headers, body) ->
    out = Frame.marshall(command, headers, body)
    @debug? ">>> " + out
    # if necessary, split the *STOMP* frame to send it on many smaller
    # *WebSocket* frames
    while(true)
      if out.length > @maxWebSocketFrameSize
        @ws.send(out.substring(0, @maxWebSocketFrameSize))
        out = out.substring(@maxWebSocketFrameSize)
        @debug? "remaining = " + out.length
      else
        return @ws.send(out)

  # Heart-beat negotiation
  _setupHeartbeat: (headers) ->
    return unless headers.version in [Stomp.VERSIONS.V1_1, Stomp.VERSIONS.V1_2]

    # heart-beat header received from the server looks like:
    #
    #     heart-beat: sx, sy
    [serverOutgoing, serverIncoming] = (parseInt(v) for v in headers['heart-beat'].split(","))

    unless @heartbeat.outgoing == 0 or serverIncoming == 0
      ttl = Math.max(@heartbeat.outgoing, serverIncoming)
      @debug? "send PING every #{ttl}ms"
      # The `Stomp.setInterval` is a wrapper to handle regular callback
      # that depends on the runtime environment (Web browser or node.js app)
      @pinger = Stomp.setInterval ttl, =>
        @ws.send Byte.LF
        @debug? ">>> PING"

    unless @heartbeat.incoming == 0 or serverOutgoing == 0
      ttl = Math.max(@heartbeat.incoming, serverOutgoing)
      @debug? "check PONG every #{ttl}ms"
      @ponger = Stomp.setInterval ttl, =>
        delta = now() - @serverActivity
        # We wait twice the TTL to be flexible on window's setInterval calls
        if delta > ttl * 2
          @debug? "did not receive server activity for the last #{delta}ms"
          @ws.close()

  # parse the arguments number and type to find the headers, connectCallback and
  # (eventually undefined) errorCallback
  _parseConnect: (args...) ->
    headers = {}
    switch args.length
      when 2
        [headers, connectCallback] = args
      when 3
        if args[1] instanceof Function
          [headers, connectCallback, errorCallback] = args
        else
          [headers.login, headers.passcode, connectCallback] = args
      when 4
        [headers.login, headers.passcode, connectCallback, errorCallback] = args
      else
        [headers.login, headers.passcode, connectCallback, errorCallback, headers.host] = args

    [headers, connectCallback, errorCallback]

  # [CONNECT Frame](http://stomp.github.com/stomp-specification-1.1.html#CONNECT_or_STOMP_Frame)
  #
  # The `connect` method accepts different number of arguments and types:
  #
  # * `connect(headers, connectCallback)`
  # * `connect(headers, connectCallback, errorCallback)`
  # * `connect(login, passcode, connectCallback)`
  # * `connect(login, passcode, connectCallback, errorCallback)`
  # * `connect(login, passcode, connectCallback, errorCallback, host)`
  #
  # The errorCallback is optional and the 2 first forms allow to pass other
  # headers in addition to `client`, `passcode` and `host`.
  connect: (args...) ->
    out = @_parseConnect(args...)
    [headers, @connectCallback, errorCallback] = out
    @debug? "Opening Web Socket..."
    @ws.onmessage = (evt) =>
      data = if typeof(ArrayBuffer) != 'undefined' and evt.data instanceof ArrayBuffer
        # the data is stored inside an ArrayBuffer, we decode it to get the
        # data as a String
        arr = new Uint8Array(evt.data)
        @debug? "--- got data length: #{arr.length}"
        # Return a string formed by all the char codes stored in the Uint8array
        (String.fromCharCode(c) for c in arr).join('')
      else
        # take the data directly from the WebSocket `data` field
        evt.data
      @serverActivity = now()
      if data == Byte.LF # heartbeat
        @debug? "<<< PONG"
        return
      @debug? "<<< #{data}"
      # Handle STOMP frames received from the server
      # The unmarshall function returns the frames parsed and any remaining
      # data from partial frames.
      unmarshalledData = Frame.unmarshall(@partialData + data)
      @partialData = unmarshalledData.partial
      for frame in unmarshalledData.frames
        switch frame.command
          # [CONNECTED Frame](http://stomp.github.com/stomp-specification-1.1.html#CONNECTED_Frame)
          when "CONNECTED"
            @debug? "connected to server #{frame.headers.server}"
            @connected = true
            @_setupHeartbeat(frame.headers)
            @connectCallback? frame
          # [MESSAGE Frame](http://stomp.github.com/stomp-specification-1.1.html#MESSAGE)
          when "MESSAGE"
            # the `onreceive` callback is registered when the client calls
            # `subscribe()`.
            # If there is registered subscription for the received message,
            # we used the default `onreceive` method that the client can set.
            # This is useful for subscriptions that are automatically created
            # on the browser side (e.g. [RabbitMQ's temporary
            # queues](http://www.rabbitmq.com/stomp.html)).
            subscription = frame.headers.subscription
            onreceive = @subscriptions[subscription] or @onreceive
            if onreceive
              client = this
              messageID = frame.headers["message-id"]
              # add `ack()` and `nack()` methods directly to the returned frame
              # so that a simple call to `message.ack()` can acknowledge the message.
              frame.ack = (headers = {}) =>
                client .ack messageID , subscription, headers
              frame.nack = (headers = {}) =>
                client .nack messageID, subscription, headers
              onreceive frame
            else
              @debug? "Unhandled received MESSAGE: #{frame}"
          # [RECEIPT Frame](http://stomp.github.com/stomp-specification-1.1.html#RECEIPT)
          #
          # The client instance can set its `onreceipt` field to a function taking
          # a frame argument that will be called when a receipt is received from
          # the server:
          #
          #     client.onreceipt = function(frame) {
          #       receiptID = frame.headers['receipt-id'];
          #       ...
          #     }
          when "RECEIPT"
            @onreceipt?(frame)
          # [ERROR Frame](http://stomp.github.com/stomp-specification-1.1.html#ERROR)
          when "ERROR"
            errorCallback?(frame)
          else
            @debug? "Unhandled frame: #{frame}"
    @ws.onclose   = =>
      msg = "Whoops! Lost connection to #{@ws.url}"
      @debug?(msg)
      @_cleanUp()
      errorCallback?(msg)
    @ws.onopen    = =>
      @debug?('Web Socket Opened...')
      headers["accept-version"] = Stomp.VERSIONS.supportedVersions()
      headers["heart-beat"] = [@heartbeat.outgoing, @heartbeat.incoming].join(',')
      @_transmit "CONNECT", headers

  # [DISCONNECT Frame](http://stomp.github.com/stomp-specification-1.1.html#DISCONNECT)
  disconnect: (disconnectCallback, headers={}) ->
    @_transmit "DISCONNECT", headers
    # Discard the onclose callback to avoid calling the errorCallback when
    # the client is properly disconnected.
    @ws.onclose = null
    @ws.close()
    @_cleanUp()
    disconnectCallback?()

  # Clean up client resources when it is disconnected or the server did not
  # send heart beats in a timely fashion
  _cleanUp: () ->
    @connected = false
    Stomp.clearInterval @pinger if @pinger
    Stomp.clearInterval @ponger if @ponger

  # [SEND Frame](http://stomp.github.com/stomp-specification-1.1.html#SEND)
  #
  # * `destination` is MANDATORY.
  send: (destination, headers={}, body='') ->
    headers.destination = destination
    @_transmit "SEND", headers, body

  # [SUBSCRIBE Frame](http://stomp.github.com/stomp-specification-1.1.html#SUBSCRIBE)
  subscribe: (destination, callback, headers={}) ->
    # for convenience if the `id` header is not set, we create a new one for this client
    # that will be returned to be able to unsubscribe this subscription
    unless headers.id
      headers.id = "sub-" + @counter++
    headers.destination = destination
    @subscriptions[headers.id] = callback
    @_transmit "SUBSCRIBE", headers
    client = this
    return {
      id: headers.id

      unsubscribe: ->
        client.unsubscribe headers.id
    }

  # [UNSUBSCRIBE Frame](http://stomp.github.com/stomp-specification-1.1.html#UNSUBSCRIBE)
  #
  # * `id` is MANDATORY.
  #
  # It is preferable to unsubscribe from a subscription by calling
  # `unsubscribe()` directly on the object returned by `client.subscribe()`:
  #
  #     var subscription = client.subscribe(destination, onmessage);
  #     ...
  #     subscription.unsubscribe();
  unsubscribe: (id) ->
    delete @subscriptions[id]
    @_transmit "UNSUBSCRIBE", {
      id: id
    }

  # [BEGIN Frame](http://stomp.github.com/stomp-specification-1.1.html#BEGIN)
  #
  # If no transaction ID is passed, one will be created automatically
  begin: (transaction) ->
    txid = transaction || "tx-" + @counter++
    @_transmit "BEGIN", {
      transaction: txid
    }
    client = this
    return {
      id: txid
      commit: ->
        client.commit txid
      abort: ->
        client.abort txid
    }
  
  # [COMMIT Frame](http://stomp.github.com/stomp-specification-1.1.html#COMMIT)
  #
  # * `transaction` is MANDATORY.
  #
  # It is preferable to commit a transaction by calling `commit()` directly on
  # the object returned by `client.begin()`:
  #
  #     var tx = client.begin(txid);
  #     ...
  #     tx.commit();
  commit: (transaction) ->
    @_transmit "COMMIT", {
      transaction: transaction
    }
  
  # [ABORT Frame](http://stomp.github.com/stomp-specification-1.1.html#ABORT)
  #
  # * `transaction` is MANDATORY.
  #
  # It is preferable to abort a transaction by calling `abort()` directly on
  # the object returned by `client.begin()`:
  #
  #     var tx = client.begin(txid);
  #     ...
  #     tx.abort();
  abort: (transaction) ->
    @_transmit "ABORT", {
      transaction: transaction
    }
  
  # [ACK Frame](http://stomp.github.com/stomp-specification-1.1.html#ACK)
  #
  # * `messageID` & `subscription` are MANDATORY.
  #
  # It is preferable to acknowledge a message by calling `ack()` directly
  # on the message handled by a subscription callback:
  #
  #     client.subscribe(destination,
  #       function(message) {
  #         // process the message
  #         // acknowledge it
  #         message.ack();
  #       },
  #       {'ack': 'client'}
  #     );
  ack: (messageID, subscription, headers = {}) ->
    headers["message-id"] = messageID
    headers.subscription = subscription
    @_transmit "ACK", headers

  # [NACK Frame](http://stomp.github.com/stomp-specification-1.1.html#NACK)
  #
  # * `messageID` & `subscription` are MANDATORY.
  #
  # It is preferable to nack a message by calling `nack()` directly on the
  # message handled by a subscription callback:
  #
  #     client.subscribe(destination,
  #       function(message) {
  #         // process the message
  #         // an error occurs, nack it
  #         message.nack();
  #       },
  #       {'ack': 'client'}
  #     );
  nack: (messageID, subscription, headers = {}) ->
    headers["message-id"] = messageID
    headers.subscription = subscription
    @_transmit "NACK", headers

# ##The `Stomp` Object
Stomp =
  VERSIONS:
    V1_0: '1.0'
    V1_1: '1.1'
    V1_2: '1.2'

    # Versions of STOMP specifications supported
    supportedVersions: ->
      '1.1,1.0'

  # This method creates a WebSocket client that is connected to
  # the STOMP server located at the url.
  client: (url, protocols = ['v10.stomp', 'v11.stomp']) ->
    # This is a hack to allow another implementation than the standard
    # HTML5 WebSocket class.
    #
    # It is possible to use another class by calling
    #
    #     Stomp.WebSocketClass = MozWebSocket
    #
    # *prior* to call `Stomp.client()`.
    #
    # This hack is deprecated and  `Stomp.over()` method should be used
    # instead.
    klass = Stomp.WebSocketClass || WebSocket
    ws = new klass(url, protocols)
    new Client ws

  # This method is an alternative to `Stomp.client()` to let the user
  # specify the WebSocket to use (either a standard HTML5 WebSocket or
  # a similar object).
  over: (ws) ->
    new Client ws

  # For testing purpose, expose the Frame class inside Stomp to be able to
  # marshall/unmarshall frames
  Frame: Frame

# # `Stomp` object exportation

# export as CommonJS module
if exports?
  exports.Stomp = Stomp

# export in the Web Browser
if window?
  # in the Web browser, rely on `window.setInterval` to handle heart-beats
  Stomp.setInterval= (interval, f) ->
    window.setInterval f, interval
  Stomp.clearInterval= (id) ->
    window.clearInterval id
  window.Stomp = Stomp
# or in the current object (e.g. a WebWorker)
else if !exports
  self.Stomp = Stomp
