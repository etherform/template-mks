package com.crutchbag.mks.amqp;

import java.nio.charset.StandardCharsets;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.crutchbag.mks.MksController;
import com.crutchbag.mks.log.MksLogger;

@Component
public class MQListener {

    @Autowired
    private RabbitListenerEndpointRegistry registry;

    @Autowired
    private RabbitAdmin ra;

    @Autowired
    private Queue inputQueue;

    @Autowired
    private MksController controller;

    @Autowired
    private MksLogger logger;

    @RabbitListener(id = "consumer", queues = "#{inputQueue.name}")
    private void onMessage(Message msg) {
        String body = new String(msg.getBody(), StandardCharsets.UTF_8);
        logger.logInfo("#! Message received:", body);
        controller.control(body);
        logger.logInfo("#! Message processing finished.");
        logger.forceSend(); // if second activator wasn't found, but log message is formed force send it from here
    }

    public void changeListenerQueue(String queueName) {
        String oldQueueName = inputQueue.getName();
        inputQueue = new Queue(queueName, false, false, false);
        ra.declareQueue(inputQueue);
        SimpleMessageListenerContainer contaner = (SimpleMessageListenerContainer) registry.getListenerContainer("consumer");
        contaner.stop();
        contaner.setQueues(inputQueue);
        contaner.start();
        logger.logInfo("Changing listener queue to:", inputQueue.getName());
        if (ra.deleteQueue(oldQueueName)) {
            logger.logInfo("Removed unused queue:", oldQueueName);
        }
    }
}
