ComPrintDemo 串口打印DEMO (包含文档、Apk、Demo、jar包)
UsbPrintDemo USB口打印DEMO (包含文档、Apk、Demo、jar包)
BtPrintDemo  蓝牙打印DEMO (包含文档、Apk、Demo、jar包)

一、printsdk.jar调用接口版本更新说明：

1.【20161207】2.1版本，增加新功能：专用二维码打印(T500II板+MT532II);接口函数名PrintQrCodeT500II(int arg0,String data)

2.【20161215】2.2版本，增加新功能：支持图片内容为黑白两色的 BMP/JPG/PNG格式的文件;接口函数名PrintDiskImagefile(int[] datas, int arg0, int arg1)

3.【20161216】2.2版本，SDK接口调用文档更新中英文版本

   美松科技打印机产品开发包参考手册(Android)-V2.2.pdf （中文）

   Masung Printers SDK Manual(Android)-V2.2.pdf（英文）


二、usbprintsdk.jar更新说明

1.【20161215】2.1版本，只支持连接单台打印机设备

2.【20170113】2.10支持多台usb连接打印，且兼容之前的版本,具体参考调用文档中usb连接对应的调用接口

3.【20170518】2.10版本，usbprintsdk-2.10.jar优化连接打开设备异常问题；

三、功能实例更新
【20170519】2.2版本，UsbPrintDemo优化功能实例：

   （1）NV图多个路径选择，位图下载案例

   （2）打印Img路径选择，图片打印案例

   （3）打印水平制表案例
【20171110】2.2版本，UsbPrintDemo优化功能实例：
   （1）增加“自检页”打印，“打印机信息”获取按钮
   
   （2）打印水平制表多行打印案例
   
   （3）单色位图转换打印案例


