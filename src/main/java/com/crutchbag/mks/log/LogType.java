package com.crutchbag.mks.log;

public enum LogType {
    DEBUG("DEBUG: "),
    INFO("INFO: "),
    ERROR("ERROR: "),
    EXCEPTION("EXCEPTION: ");

    String prefix;

    LogType(String prefix) {
        this.prefix = prefix;
    }
}
