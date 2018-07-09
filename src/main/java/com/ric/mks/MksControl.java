package com.ric.mks;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;

public class MksControl<T> {

    private CommandReturn ret = new CommandReturn();
    private HashMap<String, Method> commandsList;

    public T commands;

    public MksControl(T tp) {
        commands = tp;
        commandsList = new HashMap<String, Method>();
        Method[] ms = commands.getClass().getMethods();
        for (Method m : ms) {
            if (m.isAnnotationPresent(MQCommand.class)) {
                commandsList.put(m.getName(), m);
            }
        }
    }

    public CommandReturn control(String msg) {

        String[] cmd = msg.split(" ");

        String commandStr = cmd[0];
        String[] args = Arrays.copyOfRange(cmd, 1, cmd.length);

        Method m = commandsList.get(commandStr);
        if (m == null) return ret.err("Command not found:"+commandStr);

        int c = m.getParameterCount();
        if (c <= args.length) {
            ret.err("Error");
            try {
                args = Arrays.copyOfRange(args, 0, c);
                ret = (CommandReturn)m.invoke(commands, (Object[])args);
            } catch (Exception e) {
                return ret.err("Error when calling command:"+e.getMessage());
            }
            return ret;
        }
        return ret.err("Need more args for "+commandStr+"("+c+" vs "+args.length+")");
    }

}
