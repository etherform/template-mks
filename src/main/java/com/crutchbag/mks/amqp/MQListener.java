package com.crutchbag.mks.amqp;

import java.nio.charset.StandardCharsets;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.crutchbag.mks.MksController;
import com.crutchbag.mks.util.MksLogger;

@Component
public class MQListener {

    @Autowired
    private MksController controller;

    @Autowired
    private MksLogger logger;

    @SuppressWarnings("unused")
    @Autowired
    private Queue inputQueue;

    @RabbitListener(queues = "#{inputQueue.name}")
    private void onMessage(Message msg) {
        String body = new String(msg.getBody(), StandardCharsets.UTF_8);
        logger.logInfo("#! Message received:", body);
        controller.control(body);
        logger.logInfo("#! Message processing finished.");
        logger.forceSend(); // if second activator wasn't found, but log message is formed force send it from here
    }
}
