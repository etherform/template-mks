package com.crutchbag.mks.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.crutchbag.mks.amqp.MQSender;

import lombok.Getter;
import lombok.Setter;

@Component
public class MksLogger {

    @Autowired
    private MQSender sender;

    @Getter
    @Setter
    private Boolean isEnabled = false;

    @Getter
    @Setter
    private Boolean logToMQ = false;

    private static final String INFOSTRING = "INFO: ";
    private static final String ERRORSTRING = "ERROR: ";

    public void logInfo(String s) {
        if (isEnabled)
            if (logToMQ) {
                sender.sendLog(INFOSTRING+s);
            } else {
                System.out.println(INFOSTRING+s);
            }
    }

    public void logError(String s) {
        if (isEnabled)
            if (logToMQ) {
                sender.sendLog(ERRORSTRING+s);
            } else {
                System.out.println(ERRORSTRING+s);
            }
    }

    public void logInfoConsole(String s) {
        if (isEnabled) {
            System.out.println(INFOSTRING+s);
        }
    }

    public void logErrorConsole(String s) {
        if (isEnabled) {
            System.out.println(ERRORSTRING+s);
        }
    }

    public void logInfoMQ(String s) {
        if (isEnabled) {
            sender.sendLog(INFOSTRING+s);
        }
    }

    public void logErrorMQ(String s) {
        if (isEnabled) {
            sender.sendLog(ERRORSTRING+s);
        }
    }


}
