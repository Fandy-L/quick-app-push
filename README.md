# quick-app-push
快应用消息推送

运行需要：
  1、安装依赖编译插件：lombok
  2、安装redis，在application.yml进行参数配置
  3、安装mysql，在application.yml进行参数配置，代码使用jpa自动建表
  
待优化的测试：
  1、单推、群推的各个厂商进行一次数据库查表，可以改成查找一次表到容器，各厂商取完数据后清空本次数据
  2、加mogonDB，对单推对于时间跨度问题，直接使用mongonDB存储记录统计数据。、

参考博客文章：https://blog.csdn.net/weixin_38772076/article/details/104046702
