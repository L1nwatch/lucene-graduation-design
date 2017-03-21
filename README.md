# lucene_graduation_design

毕设工程实现，主要是基于 Lucene 平台研究 HITS 算法

## 环境说明

* macOSX 10.10.5
* IDEA 2016.2
* JDK 1.8
* Lucene-4.3

## 进度说明

### 2017.03.01

开始准备 Lucene 平台的搭建, 第一步是把该平台的索引以及查找运行起来, 利用 Lucene-6.4.1 提供的 DEMO 直接跑的, 已经成功在 IDEA 上运行了。

### 2017.03.03

fork 一下别人的平台, 基于这个平台进行修改好了, 省得重复造轮子

### 2017.03.13

前端页面基本更改完毕, 包括搜索首页、搜索不到结果、搜索结果只有一页、搜索结果有多页的显示

以下是首页和搜索界面截图:

![首页截图](https://github.com/L1nwatch/lucene-graduation-design/blob/master/%E6%90%9C%E7%B4%A2%E9%A6%96%E9%A1%B5-min.jpg?raw=true)

![搜索界面截图](https://github.com/L1nwatch/lucene-graduation-design/blob/master/%E6%90%9C%E7%B4%A2%E7%BB%93%E6%9E%9C-min.jpg?raw=true)

### 2017.03.14

参考网上的 [HITS 算法实现](http://blog.csdn.net/androidlushangderen/article/details/43311943#), 把 DEMO 以及测试数据 copy 了过来, 打算先研究它的思路然后再自己重新优化一遍

### 2017.03.16

更新相关代码, 现在可以利用搜狗实验室的数据建立索引以及搜索了, 但是目前只用了 DEMO 数据, 完整版数据太大, 需要一段时间跑下来

### 2017.03.17

[参考](http://git.oschina.net/zhzhenqin/paoding-analysis), 把 paoding 分词跑起来了

成功在 DEMO 上使用了 paoding 中文分词技术, 包括高亮、划分段等效果

### 2017.03.18

修正了整个工程的命名

修正页数计算错误的 BUG

### 2017.03.19

将 HITS 结合到 Lucene 平台之中了, 不过目前链接关系是伪造数据, 所以导致算法结果不够明显

### 2017.03.20

实现扩展根集成基本集的算法, 中间需要使用到数据库, 于是用 Python 脚本来创建数据库数据, 用 Java 来查询对应的数据库。已经把对应的脚本放到 PythonScript 目录下了

用 Python 脚本为数据链接库提供了 4 个表格,  成功实现了扩展根集为基本集的方法, 但是目前效率极低, 需要优化一下