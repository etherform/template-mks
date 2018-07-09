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
        app.setControlQueneName(s);
        return ret.ok("Ok.");
    }

    @MQCommand
    public CommandReturn setLogQueue(String s) {
        app.setLogQueneName(s);
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

}
