package com.crutchbag.mks.amqp;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.crutchbag.mks.log.MksLogger;

@Component
public class MQSender {

    @Autowired
    private RabbitAdmin ra;
    @Autowired
    private RabbitTemplate amqp;
    @Autowired
    private Queue outputQueue;
    @Autowired
    private Queue logQueue;

    @Autowired
    private MksLogger logger;

    public void sendLog(String s) {
        amqp.convertAndSend(logQueue.getName(), s);
    }

    public void sendOutput(String s) {
        amqp.convertAndSend(outputQueue.getName(), s);
    }

    public void changeLogQueue(String queueName) {
        logger.logInfo("Changing log queue to:", queueName);
        logQueue = replaceQueue(logQueue, queueName);
    }

    public void changeOutputQueue(String queueName) {
        logger.logInfo("Changing output queue to:", queueName);
        outputQueue = replaceQueue(outputQueue, queueName);
    }

    private Queue replaceQueue(Queue queue, String s) {
        String oldName = queue.getName();
        queue = new Queue(s, false, false, false);
        ra.declareQueue(queue);
        if (ra.deleteQueue(oldName)) {
            logger.logInfo("Removed unused queue:", oldName);
        }
        return queue;
    }
}
