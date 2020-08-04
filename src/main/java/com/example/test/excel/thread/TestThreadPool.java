package com.example.test.excel.thread;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class TestThreadPool {


    public static void main(String[] args) throws Exception {
        ExecutorService executorService1 = Executors.newFixedThreadPool(5);
        ExecutorService executorService2 = new ThreadPoolExecutor(5, 10, 1000L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
    }
}
