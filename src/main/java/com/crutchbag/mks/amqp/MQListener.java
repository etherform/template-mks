package com.crutchbag.mks.amqp;

import java.lang.reflect.InvocationTargetException;
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
    private void onMessage(Message msg) throws InvocationTargetException, IllegalAccessException, InstantiationException, NoSuchMethodException, SecurityException, IllegalArgumentException {
        String body = new String(msg.getBody(), StandardCharsets.UTF_8);
        System.out.println("[x] Message recieved: "+body);
        //logger.logInfo("Message received: "+body);
        controller.control(body);
    }
}
