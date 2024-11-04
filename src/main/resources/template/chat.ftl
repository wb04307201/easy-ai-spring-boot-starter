<!DOCTYPE html>
<html lang="en">
<head>
    <title>聊天</title>
    <meta name="renderer" content="webkit">
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="stylesheet" type="text/css" href="${contextPath}/easy/ai/static/css/chat.css"/>
</head>
<body>
<div class="container">
    <div class="content">
        <div class="item item-center"><span>你现在可以开始聊天了</span></div>
    </div>
    <div class="input-area">
        <textarea name="text" id="textarea"></textarea>
        <div class="button-area">
            <button id="send-btn" onclick="send()">发 送</button>
        </div>
    </div>
</div>
<script>
    const conversationId = Math.random().toString(12);
    let timer;

    document.onkeyup = function (e) {
        if (window.event)//如果window.event对象存在，就以此事件对象为准
            e = window.event;
        let code = e.charCode || e.keyCode;
        if (code === 13) {
            //此处编写用户敲回车后的代码
            send()
        }
    }

    function send() {
        let textarea = document.getElementById('textarea');
        let sendBtn = document.getElementById("send-btn")
        let text = textarea.value;
        if (!text) {
            alert('请输入内容');
            return;
        }

        // user
        let userItem = document.createElement('div');
        userItem.className = 'item item-right';
        userItem.innerHTML = `<div class="bubble bubble-left">` + text + `</div><div class="avatar"><img src="${contextPath}/easy/ai/static/assets/user.png"/></div>`;
        document.querySelector('.content').appendChild(userItem);

        //滚动条置底
        document.querySelector(".content").scrollTop = document.querySelector('.content').scrollHeight;

        //清空并禁止输入
        textarea.value = '';
        textarea.disabled = true;
        sendBtn.disabled = true;

        sendBtn.textContent = '正在发送...';
        /*timer = setInterval(() => {

        },1000)*/

        fetch('${contextPath}/easy/ai/chat/stream', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({message: text, conversationId: conversationId}),
        })
            .then(response => {
                if (!response.ok) {
                    throw new Error('Network response was not ok');
                }
                return response.body;
            })
            .then(body => {
                const reader = body.getReader();
                const id = Date.now();

                let assistantItem = document.createElement('div');
                assistantItem.className = 'item item-left';
                assistantItem.innerHTML = `<div class="avatar"><img src="${contextPath}/easy/ai/static/assets/ai.png"/></div><div class="bubble bubble-left" id="` + id + `"></div>`;
                document.querySelector('.content').appendChild(assistantItem);

                let answerTextContent = document.getElementById(id)

                // 读取数据流
                function read() {
                    return reader.read().then(({done, value}) => {
                        // 检查是否读取完毕
                        if (done) {
                            // console.log('已传输完毕');
                            textarea.focus();
                            textarea.disabled = false;
                            sendBtn.disabled = false;
                            sendBtn.textContent = '发 送';
                            return;
                        }
                        // 处理每个数据块
                        let context = new TextDecoder('utf-8').decode(value);
                        // console.log('收到的数据:', context);

                        answerTextContent.textContent = answerTextContent.textContent + context;

                        // 继续读取下一个数据块
                        read();
                    });
                }

                // 开始读取数据流
                read();
            })
            .catch(error => {
                textarea.disabled = false;
                sendBtn.disabled = false;
                sendBtn.textContent = '发 送';
                console.error('There has been a problem with your fetch operation:', error);
            });
    }
</script>
</body>
</html>