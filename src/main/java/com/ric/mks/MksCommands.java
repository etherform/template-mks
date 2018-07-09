package com.ric.mks;

import org.springframework.stereotype.Component;

@Component
public class MksCommands {

    private CommandReturn ret = new CommandReturn();

    @MQCommand
    public CommandReturn someCommand(String orgId) {
        return ret.ok("Some response");
    }

}
