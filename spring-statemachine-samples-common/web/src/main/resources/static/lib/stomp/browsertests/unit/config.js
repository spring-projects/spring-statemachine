$(document).ready(function(){
  
  TEST = {
    destination : "/topic/chat.general",
    login : "admin",
    password : "password",
    url : "ws://localhost:61614",
    badUrl: "ws://localhost:61625",
    timeout: 2000,
    debug : function(str) {
      $("#debug").append(str + "\n");
    }
    
  };

  // fill server requirements:
  $("#test_url").text(TEST.url);
  $("#test_destination").text(TEST.destination);
  $("#test_login").text(TEST.login);
  $("#test_password").text(TEST.password);
  
});
