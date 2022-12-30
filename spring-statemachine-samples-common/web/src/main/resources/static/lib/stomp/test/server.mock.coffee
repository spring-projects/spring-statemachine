WebSocketMock = require('./websocket.mock.js').WebSocketMock
Stomp = require('../../lib/stomp.js').Stomp

console = require 'console'

class StompServerMock extends WebSocketMock
  # WebSocketMock handlers
  
  handle_send: (msg) =>
    @stomp_dispatch(Stomp.Frame.unmarshall(msg).frames[0])
  
  handle_close: =>
    @_shutdown()
  
  handle_open: =>
    @stomp_init()
    @_accept()
  
  # Stomp server implementation
  
  stomp_init: ->
    @transactions = {}
    @subscriptions = {}
    @messages = []
  
  stomp_send: (command, headers, body=null) ->
    @_respond(Stomp.Frame.marshall(command, headers, body))
    
  stomp_send_receipt: (frame) ->
    if frame.headers.message?
      @stomp_send("ERROR", {'receipt-id': frame.headers['receipt-id'], 'message': frame.headers.message})
    else
      @stomp_send("RECEIPT", {'receipt-id': frame.headers['receipt-id']})
    
  stomp_send_message: (destination, subscription, message_id, body) ->
    @stomp_send("MESSAGE", {
      'destination': destination, 
      'message-id': message_id,
      'subscription': subscription}, body)

  stomp_dispatch: (frame) ->
    handler = "stomp_handle_#{frame.command.toLowerCase()}"
    if this[handler]?
      this[handler](frame)
      if frame.receipt
        @stomp_send_receipt(frame)
    else
      console.log "StompServerMock: Unknown command: #{frame.command}"

  stomp_handle_connect: (frame) ->
    @session_id = Math.random()
    @stomp_send("CONNECTED", {'session': @session_id})
    
  stomp_handle_begin: (frame) ->
    @transactions[frame.headers.transaction] = []
    
  stomp_handle_commit: (frame) ->
    transaction = @transactions[frame.headers.transaction]
    for frame in transaction
      @messages.push(frame.body)
    delete @transactions[frame.headers.transaction]

  stomp_handle_abort: (frame) ->
    delete @transactions[frame.headers.transaction]

  stomp_handle_send: (frame) ->
    if frame.headers.transaction
      @transactions[frame.headers.transaction].push(frame)
    else
      @messages.push(frame)

  stomp_handle_subscribe: (frame) ->
    sub_id = frame.headers.id or Math.random()
    cb = (id, body) => @stomp_send_message(frame.headers.destination, sub_id, id, body)
    @subscriptions[sub_id] = [frame.headers.destination, cb]

  stomp_handle_unsubscribe: (frame) ->
    if frame.headers.id in Object.keys(@subscriptions)
      delete @subscriptions[frame.headers.id]
    else
      frame.headers.message = "Subscription does not exist"
        
  stomp_handle_disconnect: (frame) ->
    @_shutdown()
  
  # Test helpers
  
  test_send: (sub_id, message) ->
    msgid = 'msg-' + Math.random()
    @subscriptions[sub_id][1](msgid, message)
  

exports.StompServerMock = StompServerMock