package com.ric.mks;

public class CommandReturn {
    public String body;
    public String log;
    public boolean error;

    public CommandReturn ok(String s) {
        body = s;
        log = s;
        error = false;
        return this;
    }

    public CommandReturn err(String s) {
        body = "";
        log = s;
        error = true;
        return this;
    }

    public String toString() {
        return body;
    }
}
