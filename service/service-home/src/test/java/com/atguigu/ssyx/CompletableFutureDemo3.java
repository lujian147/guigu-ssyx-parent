package com.atguigu.ssyx;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CompletableFutureDemo3 {
    public static void main(String[] args) {
        //创建一个线程池
        ExecutorService fixedThreadPool = Executors.newFixedThreadPool(3);
        //CompletableFuture创建异步对象
        System.out.println("main begin....");
        CompletableFuture<Void> completableFuture = CompletableFuture.runAsync(() -> {
            System.out.println("当前线程" + Thread.currentThread().getName());
            int result = 1024;
            System.out.println("result:" + result);
        }, fixedThreadPool).whenComplete((res,exception)->{
            System.out.println("whenComplete" + res);
            System.out.println("exception" + exception);
        });
        System.out.println("main end....");
    }
}
