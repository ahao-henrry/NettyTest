$(function() {
    websocketObj.init();
});

var websocketObj = {
    socket: null,
    init: function() {
        this.socket = $().websocketAha.initWebsocket("10.0.33.44", 8081, this.callback);
        this.bindEvent();
    },
    callback: {
        onclose: function(event) {
            websocketObj.appendln("websocket disconnected...");
        },
        onopen: function(event) {
            websocketObj.appendln("websocket connected...");
        },
        onmessage: function(event) {
            websocketObj.appendln("receive：" + event.data);
        }
    },
    bindEvent: function() {
        $(".sendMsg").bind("click", this.send);
        $(".clearHistory").bind("click", this.clearHistory);
    },
    appendln: function(text) {
        var msgContent = document.getElementById("responseText");
        msgContent.value += text + "\r\n";
    },
    send: function() {
        var msg = $(".msg").val();
        if(websocketObj.socket.readyState == WebSocket.OPEN) {
            websocketObj.socket.send(msg);
            websocketObj.appendln("send：" + msg);
        } else {
            websocketObj.appendln("connect websocket fail....");
        }
    },
    clearHistory: function() {
        var msgContent = document.getElementById("responseText");
        msgContent.value = "";
    }
};