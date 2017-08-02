;(function($) {
    var websocketAha = function() {};
    websocketAha.prototype = {
        initWebsocket: function(ip, port, callback) {
            var socket;
            if(!window.WebSocket) {
                window.WebSocket = window.MozWebSocket;
            }

            if(window.WebSocket) {
                socket = new WebSocket("ws://" + ip + ":" + port);
                socket.onopen = callback.onopen;
                socket.onclose = callback.onclose;
                socket.onmessage = callback.onmessage;
            } else {
                if(console) {
                    console.log("webbrower is not surport the websocket...");
                }
            }
            
            return socket;
        }
    };
    
    $.fn.websocketAha = new websocketAha();
})(jQuery);