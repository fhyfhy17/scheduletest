package com;

public class test {
    public static void main(String[] args) {

        Sche sche = new Sche();
        sche.init();
        sche.start();

        ScheduleTask task = new ScheduleTask() {
            @Override
            public void execute() {
                System.out.println(123);
            }
        };

        sche.scheduleOnce(task,3000);
    }


}
