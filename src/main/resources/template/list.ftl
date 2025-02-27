<!DOCTYPE html>
<html lang="en">
<head>
    <title>预览文件记录</title>
    <meta name="renderer" content="webkit">
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="stylesheet" type="text/css" href="${contextPath}/easy/ai/static/layui/2.9.18/css/layui.css"/>
    <script type="text/javascript" src="${contextPath}/easy/ai/static/layui/2.9.18/layui.js"></script>
    <style>
        body {
            padding: 10px 20px 10px 20px;
        }
    </style>
</head>
<body>
<form class="layui-form layui-row layui-col-space16">
    <div class="layui-col-md4">
        <div class="layui-form-item">
            <label class="layui-form-label">文件名</label>
            <div class="layui-input-block">
                <input type="text" name="fileName" placeholder="请输入" class="layui-input" lay-affix="clear">
            </div>
        </div>
    </div>
    <div class="layui-col-md4">
        <div class="layui-form-item">
            <label class="layui-form-label">文件位置</label>
            <div class="layui-input-block">
                <input type="text" name="filePath" placeholder="请输入" class="layui-input" lay-affix="clear">
            </div>
        </div>
    </div>
    <div class="layui-col-md4">
        <div class="layui-form-item">
            <label class="layui-form-label">文件状态</label>
            <div class="layui-input-block">
                <input type="text" name="state" placeholder="请输入" class="layui-input" lay-affix="clear">
            </div>
        </div>
    </div>
    <div class="layui-btn-container layui-col-xs12">
        <button class="layui-btn" lay-submit lay-filter="table-search">查询</button>
        <button type="reset" class="layui-btn layui-btn-primary">重置</button>
    </div>
</form>
<!-- 拖拽上传 -->
<div class="layui-upload-drag" style="display: block;" id="ID-upload-demo-drag">
    <i class="layui-icon layui-icon-upload"></i>
    <div>点击上传，或将文件拖拽到此处</div>
    <div class="layui-hide" id="ID-upload-demo-preview">
        <hr>
        <img src="" alt="上传成功后渲染" style="max-width: 100%">
    </div>
</div>
<!-- 原始容器 -->
<table class="layui-hide" id="table"></table>
<!-- 操作列 -->
<script type="text/html" id="table-templet-operator">
    <div class="layui-clear-space">
        <a class="layui-btn layui-btn-xs" lay-event="delete">删除</a>
        <a class="layui-btn layui-btn-xs" lay-event="download">下载</a>
    </div>
</script>
<script>
    layui.use(['table', 'form', 'util'], function () {
        let table = layui.table, form = layui.form, layer = layui.layer, upload = layui.upload;

        // 搜索提交
        form.on('submit(table-search)', function (data) {
            let field = data.field; // 获得表单字段
            // 执行搜索重载
            table.reloadData('table', {
                where: field // 搜索的字段
            });
            return false; // 阻止默认 form 跳转
        });

        // 渲染
        upload.render({
            elem: '#ID-upload-demo-drag', // 绑定多个元素
            url: '${contextPath}/easy/ai/upload', // 此处配置你自己的上传接口即可
            accept: 'file', // 普通文件
            done: function (res) {
                if (res.code === 200)
                    table.reloadData('table', {});
                layer.msg(res.message);
            }
        });

        table.render({
            elem: '#table',
            cols: [[ //标题栏
                {type: 'numbers', fixed: 'left'},
                {field: 'id', title: 'ID', width: 150, fixed: 'left', hide: true},
                {field: 'fileName', title: '文件名', width: 300},
                {field: 'filePath', title: '文件位置', width: 350},
                {
                    field: 'state', title: '状态', width: 150, templet: function (d) {
                        // 00 上传 10 文档拆分中 20 文档拆分完 30 向量存储中 40 向量存储完
                        if (d.state === '00') {
                            return '上传';
                        } else if (d.state === '10') {
                            return '文档拆分中';
                        } else if (d.state === '20') {
                            return '文档拆分完';
                        } else if (d.state === '30') {
                            return '向量存储中';
                        } else if (d.state === '40') {
                            return '向量存储完';
                        } else {
                            return d.state;
                        }
                    }
                },
                {field: 'operator', title: '操作', width: 200, fixed: 'right', templet: '#table-templet-operator'},
            ]],
            url: '${contextPath}/easy/ai/list',
            method: 'post',
            contentType: 'application/json',
            parseData: function (res) { // res 即为原始返回的数据
                return {
                    "code": res.code === 200 ? 0 : res.code, // 解析接口状态
                    "msg": res.message, // 解析提示文本
                    "count": res.data.length, // 解析数据长度
                    "data": res.data // 解析数据列表
                };
            },
        });

        // 操作列事件
        table.on('tool(table)', function (obj) {
            let data = obj.data; // 获得当前行数据
            switch (obj.event) {
                case 'delete':
                    deleteRow(data.id)
                    break;
                case 'download':
                    downloadRow(data.id)
                    break;
            }
        })

        function deleteRow(id) {
            layer.confirm('确定要删除么？', {icon: 3}, function (index, layero, that) {
                fetch("${contextPath}/easy/ai/delete?id=" + id)
                    .then(response => response.json())
                    .then(res => {
                        if (res.code === 200)
                            table.reloadData('table', {});
                        layer.close(index);
                        layer.msg(res.message);
                    })
                    .catch(err => {
                        layer.msg(err)
                        layer.close(index);
                    })
            }, function (index, layero, that) {
            });
        }

        function downloadRow(id) {
            window.open("${contextPath}/easy/ai/download?id=" + id);
        }
    })
</script>
</body>
</html>