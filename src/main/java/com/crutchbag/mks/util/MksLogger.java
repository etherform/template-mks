package com.crutchbag.mks.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import com.crutchbag.mks.amqp.MQSender;

import lombok.Getter;

@Component
public class MksLogger {
    /*
     * TODO timestamps
     */
    @Autowired
    private MQSender sender;

    @Getter
    private Boolean isEnabled = true;

    private Boolean awake = false;
    private StringBuilder msg = new StringBuilder(SBSIZE);

    private static final int SBSIZE = 1000;
    private static final String INFOPREFIX = "INFO: ";
    private static final String ERRORPREFIX = "ERROR: ";
    private static final String EXCEPTION = "EXCEPTION THROWN:";
    private static final String ACTIVATOR = "#!";

    public void enable() {
        isEnabled = true;
    }

    public void disable() {
        isEnabled = false;
    }


    private void toggleState() {
        if (!awake) {
            msg.append("### LOGGING MESSAGE STARTED ###");
            msg.append(System.lineSeparator());
            awake = true;
        } else {
            msg.append("### LOGGING MESSAGE FINISHED ###");
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
                if (s.contains(ACTIVATOR)) {
                    msg.append(INFOPREFIX.concat(s.replace(ACTIVATOR+" ", "")));
                    msg.append(System.lineSeparator());
                    toggleState();
                } else {
                    msg.append(INFOPREFIX.concat(s));
                    msg.append(System.lineSeparator());
                }
            } else {
                if (s.contains(ACTIVATOR)) {
                    toggleState();
                    msg.append(INFOPREFIX.concat(s.replace(ACTIVATOR+" ", "")));
                    msg.append(System.lineSeparator());
                }
            }

        }
    }

    public void logInfo(@NonNull String s, @NonNull Object obj) {
        if (isEnabled) {
            if (awake) {
                if (s.contains(ACTIVATOR)) {
                    msg.append(INFOPREFIX.concat(s.replace(ACTIVATOR+" ", "")));
                    msg.append(obj);
                    msg.append(System.lineSeparator());
                    toggleState();
                } else {
                    msg.append(INFOPREFIX.concat(s).concat(" "));
                    msg.append(obj);
                    msg.append(System.lineSeparator());
                }
            } else {
                if (s.contains(ACTIVATOR)) {
                    toggleState();
                    msg.append(INFOPREFIX.concat(s.replace(ACTIVATOR+" ", "")));
                    msg.append(obj);
                    msg.append(System.lineSeparator());
                }
            }

        }
    }

    public void logError(@NonNull String s) {
        if (isEnabled) {
            if (awake) {
                if (s.contains(ACTIVATOR)) {
                    msg.append(ERRORPREFIX.concat(s.replace(ACTIVATOR+" ", "")));
                    msg.append(System.lineSeparator());
                    toggleState();
                } else {
                    msg.append(ERRORPREFIX.concat(s));
                    msg.append(System.lineSeparator());
                }
            } else {
                if (s.contains(ACTIVATOR)) {
                    toggleState();
                    msg.append(ERRORPREFIX.concat(s.replace(ACTIVATOR+" ", "")));
                    msg.append(System.lineSeparator());
                }
            }

        }
    }

    public void logError(@NonNull String s, @NonNull Object obj) {
        if (isEnabled) {
            if (awake) {
                if (s.contains(ACTIVATOR)) {
                    msg.append(ERRORPREFIX.concat(s.replace(ACTIVATOR+" ", "")));
                    msg.append(obj);
                    msg.append(System.lineSeparator());
                    toggleState();
                } else {
                    msg.append(ERRORPREFIX.concat(s).concat(" "));
                    msg.append(obj);
                    msg.append(System.lineSeparator());
                }
            } else {
                if (s.contains(ACTIVATOR)) {
                    toggleState();
                    msg.append(ERRORPREFIX.concat(s.replace(ACTIVATOR+" ", "")));
                    msg.append(obj);
                    msg.append(System.lineSeparator());
                }
            }

        }
    }

    public void logException(@NonNull String s, @NonNull Throwable e) {
        if (isEnabled) {
            if (awake) {
                msg.append(ERRORPREFIX.concat(s));
                msg.append(System.lineSeparator());
                msg.append(EXCEPTION);
                msg.append(System.lineSeparator());
                msg.append(e.getClass().getCanonicalName());
                msg.append(System.lineSeparator());
                msg.append(e.getMessage());
                msg.append(System.lineSeparator());
            } else {
                toggleState();
                msg.append(ERRORPREFIX.concat(s));
                msg.append(System.lineSeparator());
                msg.append(EXCEPTION);
                msg.append(System.lineSeparator());
                msg.append(e.getClass().getCanonicalName());
                msg.append(System.lineSeparator());
                msg.append(e.getMessage());
                msg.append(System.lineSeparator());
                toggleState();
            }
        } else {
            logConsole(ERRORPREFIX.concat(s));
            logConsole(e.getMessage());
        }
    }

    // just in case
    public void logConsole(@NonNull String s) {
        System.err.println(s);
    }

}
