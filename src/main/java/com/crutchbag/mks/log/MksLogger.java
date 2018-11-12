package com.crutchbag.mks.log;

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
    private static final String INFOPREFIX = "INFO: ";
    private static final String ERRORPREFIX = "ERROR: ";
    private static final String EXCEPTION = "EXCEPTION THROWN: ";
    private static final String ACTIVATOR = "#!";

    public void enable() {
        isEnabled = true;
    }

    public void disable() {
        isEnabled = false;
    }

    private static String formatDateTime(long time) {
        return String.format("%1$tF %1$tH:%1$tM:%1$tS", time);
    }

    private static String formatTimestamp(long time) {
        String s = String.format("%1$tS.%1$tN ", time);
        return s.substring(0, s.length()-6);
    }

    private String timestamp() {
        return formatTimestamp(System.currentTimeMillis()-msgtime);
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

    private void appendString(String prefix, String s) {
        msg.append(timestamp().concat(prefix).concat(s).concat("\n"));
    }

    private void appendStringObject(String prefix, String s, Object obj) {
        msg.append(timestamp().concat(prefix).concat(s).concat(" "));
        msg.append(obj);
        msg.append("\n");
    }

    public void logInfo(@NonNull String s) {
        if (isEnabled) {
            String prefix = INFOPREFIX;
            if (awake) {
                if (s.contains(ACTIVATOR)) {
                    s = s.replace(ACTIVATOR+" ", "");
                    appendString(prefix, s);
                    toggleState();
                } else {
                    appendString(prefix, s);
                }
            } else {
                if (s.contains(ACTIVATOR)) {
                    toggleState();
                    s = s.replace(ACTIVATOR+" ", "");
                    appendString(prefix, s);
                }
            }

        }
    }

    public void logInfo(@NonNull String s, @NonNull Object obj) {
        if (isEnabled) {
            String prefix = INFOPREFIX;
            if (awake) {
                if (s.contains(ACTIVATOR)) {
                    s = s.replace(ACTIVATOR+" ", "");
                    appendStringObject(prefix, s, obj);
                    toggleState();
                } else {
                    appendStringObject(prefix, s, obj);
                }
            } else {
                if (s.contains(ACTIVATOR)) {
                    toggleState();
                    s = s.replace(ACTIVATOR+" ", "");
                    appendStringObject(prefix, s, obj);
                }
            }

        }
    }

    public void logError(@NonNull String s) {
        if (isEnabled) {
            String prefix = ERRORPREFIX;
            if (awake) {
                if (s.contains(ACTIVATOR)) {
                    s = s.replace(ACTIVATOR+" ", "");
                    appendString(prefix, s);
                    toggleState();
                } else {
                    appendString(prefix, s);
                }
            } else {
                if (s.contains(ACTIVATOR)) {
                    toggleState();
                    s = s.replace(ACTIVATOR+" ", "");
                    appendString(prefix, s);
                }
            }

        }
    }

    public void logError(@NonNull String s, @NonNull Object obj) {
        if (isEnabled) {
            String prefix = ERRORPREFIX;
            if (awake) {
                if (s.contains(ACTIVATOR)) {
                    s = s.replace(ACTIVATOR+" ", "");
                    appendStringObject(prefix, s, obj);
                    toggleState();
                } else {
                    appendStringObject(prefix, s, obj);
                }
            } else {
                if (s.contains(ACTIVATOR)) {
                    toggleState();
                    s = s.replace(ACTIVATOR+" ", "");
                    appendStringObject(prefix, s, obj);
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
