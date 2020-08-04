package com.example.test.excel.thread;

import java.util.ArrayList;
import java.util.List;

/**
 * 多线程顺序输出1-100数组
 * threadA 输出1 3 5 7...
 * threadB 输出2 4 6 8...
 * 最终按顺序输出1 2 3 4 5 6 7 8...
 */
public class TestThread {


    public static void main(String[] args) throws Exception {
        List<Integer> list = new ArrayList<>();
        for (int i = 1; i <= 100; i++) {
            list.add(i);
        }

        Object lock = new Object();
        Thread threadA = new Thread(() -> {
            synchronized (lock) {
                for (int i = 0; i < list.size(); i++) {
                    if (i % 2 == 0) {
                        System.out.println(list.get(i));
                        lock.notify();
                        try {
                            lock.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
        threadA.start();

        Thread threadB = new Thread(() -> {
            synchronized (lock) {
                for (int i = 0; i < list.size(); i++) {
                    if (i % 2 == 1) {
                        System.out.println(list.get(i));
                        lock.notify();
                        try {
                            lock.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    }
                }
            }
        });
        threadB.start();

        Thread.currentThread().join();

    }
}
