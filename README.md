# hybrid-demo
hybrid入门demo，实现简单的JSbridge，及开源库dsbridge使用示例

JSbridge-demo文件是关于JSbridge的两种实现：
	1.拦截URL schema
	2.注入JS api
	web端点击按钮，调用Native端弹窗弹出web端输入信息
	Native端点击按钮，调用web端弹窗弹出native端输入信息
还有通过回调函数进行通讯。一般格式为：调用能力，传递参数，监听回调；
	web端获取native内容显示，native端获取web内容显示
hybird-app文件是一个demo，实现通过web端发送原生http请求、原生端实现换肤功能。