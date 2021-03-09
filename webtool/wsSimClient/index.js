var wsUrl = "ws://glb-sit-sz.tvtcloud.com/wsproxy";
//var wsUrl = "wss://192.168.201.3:7701/wsproxy";

// 对Date的扩展，将 Date 转化为指定格式的String
// 月(M)、日(d)、小时(h)、分(m)、秒(s)、季度(q) 可以用 1-2 个占位符， 
// 年(y)可以用 1-4 个占位符，毫秒(S)只能用 1 个占位符(是 1-3 位的数字) 
// 例子： 
// (new Date()).Format("yyyy-MM-dd hh:mm:ss.S") ==> 2006-07-02 08:09:04.423 
// (new Date()).Format("yyyy-M-d h:m:s.S")      ==> 2006-7-2 8:9:4.18 
Date.prototype.Format = function (fmt) {
    var o = {
        "M+": this.getMonth() + 1, //月份 
        "d+": this.getDate(), //日 
        "H+": this.getHours(), //小时 
        "m+": this.getMinutes(), //分 
        "s+": this.getSeconds(), //秒 
        "q+": Math.floor((this.getMonth() + 3) / 3), //季度 
        "S": this.getMilliseconds() //毫秒 
    };
    if (/(y+)/.test(fmt)) fmt = fmt.replace(RegExp.$1, (this.getFullYear() + "").substr(4 - RegExp.$1.length));
    for (var k in o)
    if (new RegExp("(" + k + ")").test(fmt)) fmt = fmt.replace(RegExp.$1, (RegExp.$1.length == 1) ? (o[k]) : (("00" + o[k]).substr(("" + o[k]).length)));
    return fmt;
}

$(function () {
    var sendTemplate = `<div class="customer"><div class="name">Client</div><div class="content"></div></div>`
    var replyTemplate = `<div class="reply"><div class="name">Proxy</div><div class="content"></div></div>`
    // setWebsocket();

    $("#sendBtn").click(function () {
        var content = $("#inputArea").val()
        ws.send(content)
        appendSend(content)
        $("#inputArea").val('')
    })

    $("#clear").click(function () {
        $("#chartBox").empty()
    })

    $("#open").click(function () {
        setWebsocket()
    })

    $("#close").click(function () {
        ws.close()
    })

    $("#btnClearLog").click(function(){
        $("#chartBox").empty();
    })

    $("#chkIsAutoLogin").change(function(){
        if($("#chkIsAutoLogin").is(":checked")){
            $("#svcinfo").show();
        }
        else{
            $("#svcinfo").hide();
        }
    })

    function fitScroll() {
        var scrollHeight = $("#chartBox")[0].scrollHeight
        var height = $("#chartBox").outerHeight()
        if (scrollHeight > height)
            $("#chartBox").scrollTop(scrollHeight - height)
    }

    function appendSend(content) {
        var $send = $(sendTemplate)
        $(".content", $send).html(new Date().Format("yyyy-MM-dd HH:mm:ss.S") + " -- " + content)
        $("#chartBox").append($send)
        fitScroll()
    }

    function appendReply(content) {
        var $reply = $(replyTemplate)
        $(".content", $reply).html(new Date().Format("yyyy-MM-dd HH:mm:ss.S") + " -- " + content)
        $("#chartBox").append($reply)
        fitScroll()
    }

    function getNonce(){
        return Math.floor(Math.random() * 2147483647);
    }

    function getTime(){
        return Math.floor((new Date()).getTime() / 1000);
    }


    function doLogin(){    
        var svcId = $("#svcId").val();
        var svcPassword = $("#svcPassword").val();
        if(!svcId || !svcPassword){
            alert('id and password is required!');
            return;
        }
        var nonce = getNonce();
        var time = getTime();
        var sign = sha512(nonce + "#" + md5(svcPassword));
        var loginData = {
            "url": "/proxy/login",
            "basic": {
                "ver": "1.0",
                "id": 1,
                "time": time,
                "nonce": nonce,
                "sign": sign
            },
            "data": {
                "id": svcId.toString(),
                "serviceInfo": {
                    "applicationName": "test_1",
                    "versionInfo": "test",
                    "upgradeTime": "20200915081806",
                    "runTime": "0d 0h 0m 0s",
                    "pid": 2550,
                    "hostName": "test_1",
                    "hostIp": "192.168.201.7",
                    "startTime":getTime(),
                    "port": 0
                }
            }
        };
        var content = JSON.stringify(loginData);
        ws.send(content)
        appendSend(content);
    }

    function rspHeartbeat(jsonData){
        // {"basic":{"ver":"1.0","id":7194,"time":1604937644,"nonce":464201481},"url":"/proxy/heartbeat","data":{"runTime":"0d 0h 4m 14s"}}
        var data = {
            "basic":{
                "ver":"1.0",
                "time":getTime(),
                "nonce":getNonce(),
                "id":jsonData["basic"]["id"]
            },
            "data":{"startTime":getTime()},
            "url":"/proxy/heartbeat"
        }
        var content = JSON.stringify(jsonData);
        ws.send(content);
        appendSend(content);
    }

    function setWebsocket() {
        var param = location.href.split("?")[1] || ''
        var token = param.split("=")[1]
        window.ws = new WebSocket(wsUrl);
        ws.onopen = event => {
            console.log('已发起websocket连接请求');
            if($("#chkIsAutoLogin").is(":checked")){
                doLogin();
            }
        }

        /* 接收服务器推送消息 */
        ws.onmessage = event => {
            console.log(event.data, 'websocket推送的数据')
            appendReply(event.data);
            if($("#chkIsHeartbeat").is(":checked")){
                var jsonData = JSON.parse(event.data);
                if(jsonData["url"] == "/proxy/heartbeat"){
                    rspHeartbeat(jsonData);
                }
            }
        }

        /* 连接关闭 */
        ws.onclose = event => {
            console.log('websocket连接关闭')
            appendReply("<span style='color:red'>=================websocket连接关闭===============</span>");
        }

        /* 连接错误 */
        ws.onerror = (event, error) => {
            console.log('websocket连接错误：', error)
            appendReply("<span style='color:red'>=================websocket连接错误：" + error + "===============</span>");
        }
    }
})