/*
Copyright (c) 2009, Mark Pilgrim, All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted provided that the following conditions are met:

* Redistributions of source code must retain the above copyright notice,
  this list of conditions and the following disclaimer.
* Redistributions in binary form must reproduce the above copyright notice,
  this list of conditions and the following disclaimer in the documentation
  and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS 'AS IS'
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.
*/

$(document).ready(function() {
	hideTOC();
	$("#live-web-sockets").html(supports("WebSocket" in window, "WebSockets"));
}); /* document.ready */

function hideTOC() {
    var toc = '<a href="javascript:showTOC()">Show Table of Contents</a>';
    $("#toc").html(toc);
}

function showTOC() {
    var toc = '';
    var old_level = 1;
    $('h2,h3').each(function(i, h) {
	    level = parseInt(h.tagName.substring(1));
	    if (level < old_level) {
		toc += '</ol>';
	    } else if (level > old_level) {
		toc += '<ol>';
	    }
	    toc += '<li><a href=#' + h.id + '>' + h.innerHTML + '</a>';
	    old_level = level;
	});
    while (level > 1) {
	toc += '</ol>';
	level -= 1;
    }
    toc = '<a href="javascript:hideTOC()">Hide Table of Contents</a><ol>' + toc.substring(4);
    $("#toc").html(toc);
}

function supports(bool, suffix) {
  var s = "Your browser ";
  if (bool) {
    s += "supports " + suffix + ".";
  } else {
    s += "does not support " + suffix + ". :(";
  }
  return s;
}
