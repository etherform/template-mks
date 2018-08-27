package com.ric.mks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Commands {
    @Autowired
    private App app;

    private CommandReturn ret = new CommandReturn();

    @MQCommand
    public CommandReturn setControlQueue(String s) {
        app.setControlQueueName(s);
        return ret.ok("Ok.");
    }

    @MQCommand
    public CommandReturn setLogQueue(String s) {
        app.setLogQueueName(s);
        return ret.ok("Ok.");
    }

    @MQCommand
    public CommandReturn setOutQueue(String s) {
        app.setOutQueueName(s);
        return ret.ok("Ok.");
    }

    @MQCommand
    public CommandReturn test(String a) {
        return ret.ok("Run test: arg:"+a);
    }

    @MQCommand
    public CommandReturn test2(String a, String b) {
        return ret.ok("Run test2: arg1:"+a+" arg2:"+b);
    }

    @MQCommand
    public CommandReturn ping() {
        return ret.ok("pong");
    }

    @MQCommand
    public CommandReturn getName() {
        return ret.ok("{\"appName\":\""+app.getName()+"\"}");
    }

    @MQCommand
    public CommandReturn getCommands() {
        String cmds = "";
        for (String s : app.getCommands()) cmds += "\""+s+"\",";
        cmds = cmds.substring(0, cmds.length() - 1);
        return ret.ok("{\"commands\":["+cmds+"]}");
    }

}
