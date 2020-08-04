#########一：thread 通信
    a.wait()   notify():
        1.以上两个方法需要配套使用
        2.必须在同步方法或同步代码块中调用
        3.wait()：挂起当前线程
        4.notify()：唤醒其他线程
    b.volatile关键字
        1.须了解JMM   使用volatile关键字，满足各cpu之间的线程可见性，各cpu的一级缓存会有一个队列，通知（将当前值同步到其他cpu的缓存队列）并阻断其他cpu的处理线程。

 #########二：Synchronized关键字：
    保证代码块的可见性，同步（一致）性
    锁定的是当前线程
    lock--->对象头(包含当前线程，锁定状态)
    
#########三：线程池    
    1.重要参数：
        核心线程数，最大线程数，
        阻塞队列，
        超时时间，单位，
        创建线程的工厂，
        拒绝策略
    2.创建线程池的几种方式：
        ExecutorService executorService1 = Executors.newFixedThreadPool(5);
        ExecutorService executorService2 = new ThreadPoolExecutor(5,10,0L,TimeUnit.MILLISECONDS,new LinkedBlockingQueue<Runnable>());
    3.线程池类型：
        a.newFixedThreadPool
        b.newCachedThreadPool
        c.newSingleThreadExecutor
        d.newScheduleThreadPool 