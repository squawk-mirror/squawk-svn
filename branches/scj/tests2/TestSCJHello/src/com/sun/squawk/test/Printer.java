package com.sun.squawk.test;

public class Printer implements Runnable {

    String msg;

    Printer(String msg) {
        this.msg = msg;
    }

    public void run() {
        System.out.println(msg);
    }
}
