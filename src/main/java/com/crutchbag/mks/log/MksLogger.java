package com.crutchbag.mks.log;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.crutchbag.mks.amqp.MQSender;
import com.crutchbag.mks.util.MksHelper;

import lombok.NonNull;

@Component
public class MksLogger {

    @Autowired
    private MQSender sender;

    private Boolean isEnabled = true;
    private Boolean awake = false;
    private LogMessage logMessage;

    private static final int SBSIZE = 1000; // StringBuilder size of logMessgae.message
    private static final String ACTIVATOR = ">>> "; // this should be changed to something SANE for sure, atm it's just proof of concept
    private static final String DEACTIVATOR = "<<< ";// and this

    private String timestamp() {
        return MksHelper.formatTimestamp(System.currentTimeMillis()-logMessage.timestamp);
    }

    private Boolean containsTrigger(String s) {
        return (awake && s.contains(DEACTIVATOR)) || (!awake && s.contains(ACTIVATOR));
    }

    private String trimTrigger(String s) {
        return awake ? s.replace(DEACTIVATOR, "") : s.replace(ACTIVATOR, "");
    }

    private void changeState() {
        if (awake) {
            awake = false;
            logMessage.message.append("== LOG MESSAGE END ==");
            sender.sendLog(logMessage);
        } else {
            awake = true;
            logMessage = new LogMessage(LogType.INFO, System.currentTimeMillis(), new StringBuilder(SBSIZE));
            logMessage.message.append("== LOG MESSAGE CREATED ON ".concat(MksHelper.formatDateTime(logMessage.timestamp)).concat(" ==\n"));
        }
    }

    // TODO replace getMessage() with deeper analysis
    private void appendMessage(String prefix, String s, Object obj, Throwable e) {
        if (obj == null && e == null) {
            logMessage.message.append(timestamp().concat(prefix).concat(s).concat("\n"));
        } else if (e == null) {
            logMessage.message.append(timestamp().concat(prefix).concat(s).concat(" "));
            logMessage.message.append(obj);
            logMessage.message.append("\n");
        } else if (obj == null) {
            logMessage.message.append(timestamp().concat(prefix).concat(s).concat("\n"));
            logMessage.message.append(timestamp().concat(prefix).concat(e.getClass().getCanonicalName()).concat("\n"));
            logMessage.message.append(timestamp().concat(prefix).concat(e.getMessage()).concat("\n"));
        } else {
            logMessage.message.append(timestamp().concat(prefix).concat(s).concat(" "));
            logMessage.message.append(obj);
            logMessage.message.append("\n");
            logMessage.message.append(timestamp().concat(prefix).concat(e.getClass().getCanonicalName()).concat("\n"));
            logMessage.message.append(timestamp().concat(prefix).concat(e.getMessage()).concat("\n"));
        }

    }

    private void log(LogType type, String s, Object obj, Throwable e) {
        if (!isEnabled && e != null) {
            e.printStackTrace();
            return;
        } else if (!isEnabled)
            return;

        if (awake) {
            if (containsTrigger(s)) {
                s = trimTrigger(s);
                appendMessage(type.prefix, s, obj, e);
                changeState();
            } else if (type == LogType.ERROR || type == LogType.EXCEPTION) {
                appendMessage(type.prefix, s, obj, e);
                changeState();
            } else {
                appendMessage(type.prefix, s, obj, e);
            }
        } else {
            if (containsTrigger(s)) {
                s = trimTrigger(s);
                changeState();
                if (type == LogType.ERROR || type == LogType.EXCEPTION) {
                    appendMessage(type.prefix, s, obj, e);
                    changeState();
                } else {
                    appendMessage(type.prefix, s, obj, e);
                }
            } else if (type == LogType.EXCEPTION) {
                changeState();
                appendMessage(type.prefix, s, obj, e);
                changeState();
            }
        }
    }

    /*
     * Public methods below
     */

    public void forceSend() {
        if (awake) {
            changeState();
        }
    }

    public void enable() {
        isEnabled = true;
    }

    public void disable() {
        isEnabled = false;
    }

    public void logInfo(@NonNull String s) {
        log(LogType.INFO, s, null, null);
    }

    public void logInfo(@NonNull String s, @NonNull Object obj) {
        log(LogType.INFO, s, obj, null);
    }

    public void logError(@NonNull String s) {
        log(LogType.ERROR, s, null, null);
    }

    public void logError(@NonNull String s, @NonNull Object obj) {
        log(LogType.ERROR, s, obj, null);
    }

    public void logException(@NonNull String s) {
        log(LogType.EXCEPTION, s, null, null);
    }

    public void logException(@NonNull String s, @NonNull Throwable e) {
        log(LogType.EXCEPTION, s, null, e);
    }

}
