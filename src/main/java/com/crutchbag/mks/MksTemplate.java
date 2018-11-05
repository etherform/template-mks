package com.crutchbag.mks;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;

import com.crutchbag.mks.amqp.MQCommand;
import com.crutchbag.mks.amqp.MQConfig;
import com.crutchbag.mks.amqp.MQSender;
import com.crutchbag.mks.util.MksHelper.Call;
import com.crutchbag.mks.util.MksLogger;
import com.crutchbag.mks.util.MksParser;

/* This is a microservice template class.
 * It contains maintenance methods and methods that every microservice should inherit.
 * Actual microservice class should extend this template.
 */
public class MksTemplate {

    @Autowired
    protected MQConfig mqconfig;

    @Autowired
    protected MQSender sender;

    @Autowired
    protected MksParser parser;

    @Autowired
    protected MksController controller;

    @Autowired
    protected MksLogger logger;

    @Bean
    public Map<String, Call> getCallMap() {
        return parser.parseAnnotatedMethods(this, MQCommand.class);
    }

    /*
     * Below is the block of @MQCommand methods
     */

    @MQCommand
    public void setInputQueue(String s) {
        mqconfig.setInputQueueName(s);
    }

    @MQCommand
    public void setLogQueue(String s) {
        mqconfig.setLogQueueName(s);
    }

    @MQCommand
    public void setOutputQueue(String s) {
        mqconfig.setOutputQueueName(s);
    }

    @MQCommand
    public void enableLog() {
        logger.enable();
    }

    @MQCommand
    public void disableLog() {
        logger.disable();
    }

    @MQCommand
    public void getCommands() {
        sender.sendOutput(controller.getCommands());
    }

    @MQCommand
    public void getCommandArgs(String s) {
        sender.sendOutput(controller.getCommandArgs(s));
    }

    @MQCommand
    public void getCommandsWithArgs() {
        sender.sendOutput(controller.getCommandsWithArgs());
    }
}
