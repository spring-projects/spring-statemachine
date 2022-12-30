class WebSocketMock
  constructor: (@url) ->
    @onclose = ->
    @onopen = ->
    @onerror = ->
    @onmessage = ->
    @readyState = 0
    @bufferedAmount = 0
    @extensions = ''
    @protocol = ''
    setTimeout(@handle_open, 0)
  
  # WebSocket API
  
  close: ->
    @handle_close()
    @readyState = 2
   
  send: (msg) ->
    if @readyState isnt 1 then return false
    @handle_send(msg)
    return true
  
  # Helpers
  
  _accept: ->
    @readyState = 1
    @onopen({'type': 'open'})
  
  _shutdown: ->
    @readyState = 3
    @onclose({'type': 'close'})
  
  _error: ->
    @readyState = 3
    @onerror({'type': 'error'})
  
  _respond: (data) ->
    @onmessage({'type': 'message', 'data': data})
    
  # Handlers
  
  handle_send: (msg) ->
    # implement me
  
  handle_close: ->
    # implement me
  
  handle_open: ->
    # implement me

exports.WebSocketMock = WebSocketMock