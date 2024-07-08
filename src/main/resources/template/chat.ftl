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
            <div>
                <input type="checkbox" id="with-document" name="with-document" checked/>
                <label for="with-document">启用知识库</label>
            </div>
            <button id="send-btn" onclick="send()">发 送</button>
        </div>
    </div>
</div>
<script>
    const messages = []

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
        let withDocument = document.getElementById("with-document")
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
        let height = document.querySelector('.content').scrollHeight;
        document.querySelector(".content").scrollTop = height;

        //清空并禁止输入
        textarea.value = '';
        textarea.disabled = true;
        withDocument.disabled = true;
        sendBtn.disabled = true;

        const newMessages = JSON.parse(JSON.stringify(messages));
        newMessages.push({messageType: 'user', textContent: text});

        fetch(withDocument.checked ? '${contextPath}/easy/ai/chatWithDocument' : '${contextPath}/easy/ai/chat', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({messages: newMessages}),
        })
            .then(response => {
                if (!response.ok) {
                    throw new Error('Network response was not ok');
                }
                return response.json(); // 解析 JSON 数据
            })
            .then(data => {
                console.log(data); // 处理返回的数据
                if (data.code === 200) {
                    messages.push({messageType: 'user', textContent: text})
                    const assistantTextContent = data.data.result.output.content
                    messages.push({messageType: 'assistant', textContent: assistantTextContent})

                    // assistant
                    let assistantItem = document.createElement('div');
                    assistantItem.className = 'item item-left';
                    assistantItem.innerHTML = `<div class="avatar"><img src="${contextPath}/easy/ai/static/assets/ai.png"/></div><div class="bubble bubble-left">` + assistantTextContent + `</div>`;
                    document.querySelector('.content').appendChild(assistantItem);

                    //滚动条置底
                    let height = document.querySelector('.content').scrollHeight;
                    document.querySelector(".content").scrollTop = height;

                    //允许输入并指向
                    textarea.disabled = false;
                    withDocument.disabled = false;
                    sendBtn.disabled = false;
                    textarea.focus();
                } else {
                    textarea.disabled = false;
                    withDocument.disabled = false;
                    sendBtn.disabled = false;
                    console.error(data.message);
                }
            })
            .catch(error => {
                textarea.disabled = false;
                withDocument.disabled = false;
                sendBtn.disabled = false;
                console.error('There has been a problem with your fetch operation:', error);
            });
    }
</script>
</body>
</html>