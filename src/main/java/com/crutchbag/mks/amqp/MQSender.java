package com.crutchbag.mks.amqp;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MQSender {

    @Autowired
    private RabbitTemplate amqp;

    @Autowired
    private Queue logQueue;

    @Autowired
    private Queue outputQueue;

    /*
	public void sendTo(String replyto, Message replymsg) {
		amqp.send(replyto, replymsg);
	}
     */

    public void sendLog(String s) {
        amqp.convertAndSend(logQueue.getName(), s);
    }

    public void sendOutput(String s) {
        amqp.convertAndSend(outputQueue.getName(), s);
    }
}
