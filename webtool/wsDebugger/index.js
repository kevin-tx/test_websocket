var wsUrl = "ws://glb-sit-sz.tvtcloud.com/wsproxy";

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
    var logTemplate = `<div class="log"><div class="content"></div></div>`
    setWebsocket();

    function fitScroll($container) {
        var scrollHeight = $container[0].scrollHeight
        var height = $container.outerHeight()
        if (scrollHeight > height)
            $container.scrollTop(scrollHeight - height)
    }

    function appendLog(content) {
        var $reply = $(logTemplate)
        $(".content", $reply).html(new Date().Format("yyyy-MM-dd HH:mm:ss.S") + " -- " + content)
        $("#log").append($reply)
        fitScroll($("#log"))
    }

    $("#btnClearLog").click(function(){
        $("#log").empty();
    })

    function doLogin() {
        var data = {
            "basic": {
                "nonce": "",
                "sign": ""
            },
            "data": {
                "id": "99999",
                "serviceInfo": {}
            },
            "url": "/proxy/login"
        }
        ws.send(JSON.stringify(data));
    }

    function setWebsocket() {
        var param = location.href.split("?")[1] || ''
        var token = param.split("=")[1]
        window.ws = new WebSocket(wsUrl)
        ws.onopen = event => {
            console.log('已发起websocket连接请求');
            doLogin();
        }

        /* 接收服务器推送消息 */
        ws.onmessage = event => {
            console.log(event.data, 'websocket推送的数据')
            if (event.data.indexOf("[status-unAuth]") == 0) {
                var $unAuthList = $("#unAuthList");
                $unAuthList.empty();
                var data = $.trim(event.data.substr("[status-unAuth]".length));
                if (data.length > 0) {
                    unauth = data.split(",");                    
                    for (item in unauth) {
                        var $reply = $(logTemplate)
                        $(".content", $reply).text(unauth[item])
                        $unAuthList.append($reply)
                        fitScroll($unAuthList);
                    }
                }
            }
            else if (event.data.indexOf("[status-auth]") == 0) {
                var $authMap = $("#authMap");
                $authMap.empty();
                var jsondata = $.trim(event.data.substr("[status-auth]".length));
                if (jsondata.length > 0) {
                    var auth = JSON.parse(jsondata);
                    for (item in auth) {
                        var $reply = $(logTemplate)
                        $(".content", $reply).text(item + " [" + auth[item] + "]")
                        $authMap.append($reply)
                        fitScroll($authMap);
                    }
                }
            }
            else {
                appendLog(event.data);
            }
        }

        /* 连接关闭 */
        ws.onclose = event => {
            console.log('websocket连接关闭')
            appendLog("<span style='color:red'>=================websocket连接关闭===============</span>");
        }

        /* 连接错误 */
        ws.onerror = (event, error) => {
            console.log('websocket连接错误：', error)
            appendLog("<span style='color:red'>=================websocket连接错误：" + error + "===============</span>");
        }
    }
});