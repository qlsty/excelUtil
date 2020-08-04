thread 通信

　　a.wait()   notify():

　　　　1.以上两个方法需要配套使用
　　　　2.必须在同步方法或同步代码块中调用

　　   　3.wait()：挂起当前线程

　　   　4.notify()：唤醒其他线程

　　b.volatile关键字

　　　　1.须了解JMM   使用volatile关键字，满足各cpu之间的线程可见性，各cpu的一级缓存会有一个队列，通知（将当前值同步到其他cpu的缓存队列）并阻断其他cpu的处理线程。

 Synchronized关键字：

　　保证代码块的可见性，同步（一致）性

消息的逻辑位置