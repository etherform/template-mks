package com.crutchbag.mks.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import com.crutchbag.mks.amqp.MQSender;

import lombok.Getter;

@Component
public class MksLogger {

    @Autowired
    private MQSender sender;

    @Getter
    private Boolean isEnabled = true;

    private long msgtime;
    private Boolean awake = false;
    private StringBuilder msg = new StringBuilder(SBSIZE);

    private static final int SBSIZE = 1000;
    private static final String INFOPREFIX = " INFO: ";
    private static final String ERRORPREFIX = " ERROR: ";
    private static final String EXCEPTION = " EXCEPTION THROWN: ";
    private static final String ACTIVATOR = "#!";

    public void enable() {
        isEnabled = true;
    }

    public void disable() {
        isEnabled = false;
    }

    private static String formatDateTime(long time) {
        return String.format("%1$tY/%1$tm/%1$td %1$tH:%1$tM:%1$tS", time);
    }

    private static String formatTimestamp(long time) {
        String s = String.format("%1$tS.%1$tN", time);
        return s.substring(0, s.length()-6);
    }

    private void toggleState() {
        if (!awake) {
            msgtime = System.currentTimeMillis();
            msg.append("== LOG MESSAGE CREATED ON ".concat(formatDateTime(msgtime)).concat(" ==\n"));
            awake = true;
        } else {
            msg.append("== LOG MESSAGE END ==");
            sender.sendLog(msg.toString());
            msg.delete(0, msg.length());
            awake = false;
        }
    }

    public void forceSend() {
        if (awake) {
            toggleState();
        }
    }

    public void logInfo(@NonNull String s) {
        if (isEnabled) {
            if (awake) {
                String timestamp = formatTimestamp(System.currentTimeMillis()-msgtime);
                if (s.contains(ACTIVATOR)) {
                    msg.append(timestamp.concat(INFOPREFIX).concat(s.replace(ACTIVATOR+" ", "")).concat("\n"));
                    toggleState();
                } else {
                    msg.append(timestamp.concat(INFOPREFIX).concat(s).concat("\n"));
                }
            } else {
                if (s.contains(ACTIVATOR)) {
                    toggleState();
                    String timestamp = formatTimestamp(System.currentTimeMillis()-msgtime);
                    msg.append(timestamp.concat(INFOPREFIX).concat(s.replace(ACTIVATOR+" ", "")).concat("\n"));
                }
            }

        }
    }

    public void logInfo(@NonNull String s, @NonNull Object obj) {
        if (isEnabled) {
            if (awake) {
                String timestamp = formatTimestamp(System.currentTimeMillis()-msgtime);
                if (s.contains(ACTIVATOR)) {
                    msg.append(timestamp.concat(INFOPREFIX).concat(s.replace(ACTIVATOR+" ", "")));
                    msg.append(obj);
                    msg.append("\n");
                    toggleState();
                } else {
                    msg.append(timestamp.concat(INFOPREFIX).concat(s).concat(" "));
                    msg.append(obj);
                    msg.append("\n");
                }
            } else {
                if (s.contains(ACTIVATOR)) {
                    toggleState();
                    String timestamp = formatTimestamp(System.currentTimeMillis()-msgtime);
                    msg.append(timestamp.concat(INFOPREFIX).concat(s.replace(ACTIVATOR+" ", "")));
                    msg.append(obj);
                    msg.append("\n");
                }
            }

        }
    }

    public void logError(@NonNull String s) {
        if (isEnabled) {
            if (awake) {
                String timestamp = formatTimestamp(System.currentTimeMillis()-msgtime);
                if (s.contains(ACTIVATOR)) {
                    msg.append(timestamp.concat(ERRORPREFIX).concat(s.replace(ACTIVATOR+" ", "")).concat("\n"));
                    toggleState();
                } else {
                    msg.append(timestamp.concat(ERRORPREFIX).concat(s).concat("\n"));
                }
            } else {
                if (s.contains(ACTIVATOR)) {
                    toggleState();
                    String timestamp = formatTimestamp(System.currentTimeMillis()-msgtime);
                    msg.append(timestamp.concat(ERRORPREFIX).concat(s.replace(ACTIVATOR+" ", "")).concat("\n"));
                }
            }

        }
    }

    public void logError(@NonNull String s, @NonNull Object obj) {
        if (isEnabled) {
            if (awake) {
                String timestamp = formatTimestamp(System.currentTimeMillis()-msgtime);
                if (s.contains(ACTIVATOR)) {
                    msg.append(timestamp.concat(ERRORPREFIX).concat(s.replace(ACTIVATOR+" ", "")));
                    msg.append(obj);
                    msg.append("\n");
                    toggleState();
                } else {
                    msg.append(timestamp.concat(ERRORPREFIX).concat(s).concat(" "));
                    msg.append(obj);
                    msg.append("\n");
                }
            } else {
                if (s.contains(ACTIVATOR)) {
                    toggleState();
                    String timestamp = formatTimestamp(System.currentTimeMillis()-msgtime);
                    msg.append(timestamp.concat(ERRORPREFIX).concat(s.replace(ACTIVATOR+" ", "")));
                    msg.append(obj);
                    msg.append("\n");
                }
            }

        }
    }

    // TODO replace getMessage() with deeper analysis
    public void logException(@NonNull String s, @NonNull Throwable e) {
        if (isEnabled) {
            if (awake) {
                msg.append(ERRORPREFIX.concat(s).concat("\n"));
                msg.append(EXCEPTION.concat(e.getClass().getCanonicalName()).concat("\n"));
                msg.append(e.getMessage().concat("\n"));
            } else {
                toggleState();
                msg.append(ERRORPREFIX.concat(s));
                msg.append(EXCEPTION.concat(e.getClass().getCanonicalName()).concat("\n"));
                msg.append(e.getMessage().concat("\n"));
                toggleState();
            }
        } else {
            logConsole(ERRORPREFIX.concat(s).concat("\n"));
            e.printStackTrace();
        }
    }

    // just in case
    public void logConsole(@NonNull String s) {
        System.err.println(s);
    }

}
