package com.crutchbag.mks;

import java.util.List;

import org.springframework.stereotype.Component;

import com.crutchbag.mks.amqp.MQCommand;

/* This is a microservice class.
 * It extends MksTemplate which contains methods for maintenance and configuration
 * Methods that you want to expose to execution through rabbitMQ should be annotated with @MQCommand
 * TODO API
 */
@Component
public class Mks extends MksTemplate {

    @MQCommand
    public void test(String a) {
        sender.sendLog("Run test: arg:"+a);
    }

    @MQCommand
    public void test2(String a, String b) {
        sender.sendLog("Run test2: arg1:"+a+" arg2:"+b);
    }

    @MQCommand
    public void test3(Integer a, Double b, Boolean c) {
        sender.sendLog("Run test3: arg1:"+a+" arg2:"+b+" arg3:"+c);
    }

    @MQCommand
    public void test4(List<Integer> list) {
        String s = "Got a list conaining: ";
        for (Integer l : list) {
            s = s.concat(l+", ");
        }
        sender.sendLog("Run test4 command. "+s);
    }

    @MQCommand
    public void test5(Double[] dArray) {
        String s = "Got an array conaining: ";
        for (Double d : dArray) {
            s = s.concat(d+", ");
        }
        sender.sendLog("Run test5 command. "+s);
    }

    @MQCommand
    public void test6(Integer i, Double[] dArray, boolean b, List<String> list) {
        // nothing to see here
    }

    @MQCommand
    public void ping() {
        sender.sendLog("pong");
    }
}
