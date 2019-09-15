package com;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.util.Date;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Sche extends Thread {

    //任务队列调度器
    public Scheduler scheduler;


    public void init() {
        try {
            this.scheduler = new StdSchedulerFactory().getScheduler();
            this.scheduler.start();

        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }


    private ConcurrentLinkedQueue<ScheduleTask> schedulerList = new ConcurrentLinkedQueue<>(); // 待处理的时间调度队列

    /**
     * 延迟delay毫秒之后执行任务
     *
     * @param task
     * @param delay
     */
    public void scheduleOnce(ScheduleTask task, long delay) {
        task.triggerAt = System.currentTimeMillis() + delay;

        // 定义trigger
        SimpleScheduleBuilder sche = SimpleScheduleBuilder.repeatSecondlyForever();
        // 循环次数 设置为0 代表不多余循环只执行一次
        sche.withRepeatCount(0);

        // 添加任务
        scheduleUtils(task, sche, delay);
    }

    /**
     * 时间调度任务
     *
     * @param task
     * @param scheduleBuilder
     * @param delay
     */
    private void scheduleUtils(ScheduleTask task,
                               ScheduleBuilder<?> scheduleBuilder, long delay) {
        try {
            // 开始执行时间
            Date startAt = new Date();
            if (delay > 0) {
                startAt = new Date(System.currentTimeMillis() + delay);
            }

            // 定义时间调度的job内容
            JobDetail jobDetail = JobBuilder.newJob(ScheduleJob.class).withIdentity(task.jobKey).build();
            jobDetail.getJobDataMap().put("task", task);
            jobDetail.getJobDataMap().put("scheduler", schedulerList);

            // 创建最终trigger
            Trigger trigger = TriggerBuilder.newTrigger().startAt(startAt).withSchedule(scheduleBuilder).build();

            // 绑定job和trigger
            scheduler.scheduleJob(jobDetail, trigger);

            // 设置任务信息
            task.state = ScheduleTask.STATE_WAITING;
            task.sched = scheduler;
            task.jobKey = jobDetail.getKey();
            task.trigger = trigger;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (true) {
            if (!schedulerList.isEmpty()) {
                ScheduleTask poll = schedulerList.poll();
                poll.execute();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
