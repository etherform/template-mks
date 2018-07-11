package com.ric.mks;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Vector;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MksControl<T> {

    private CommandReturn ret = new CommandReturn();
    private HashMap<String, Method> commandsList;
    private ObjectMapper om = new ObjectMapper();

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
        String commandStr = null;
        Vector<String> args = new Vector<String>();
        try {
            JsonNode in = om.readTree(msg);
            commandStr = in.fieldNames().next();
            if (in.get(commandStr).isArray()) {
                for(JsonNode arg : in.get(commandStr)) {
                    args.add(arg.asText());
                }
            }
        } catch (Exception e) {
            return ret.err("Json parsing error. Json is:"+msg);
        }

        Method m = commandsList.get(commandStr);
        if (m == null) return ret.err("Command not found:"+commandStr);

        int c = m.getParameterCount();
        if (c <= args.size()) {
            ret.err("Error");
            try {
                ret = (CommandReturn)m.invoke(commands,
                        Arrays.copyOfRange(args.toArray(), 0, c)
                        );
            } catch (Exception e) {
                StringWriter errors = new StringWriter();
                e.printStackTrace(new PrintWriter(errors));
                String trace = errors.toString();
                return ret.err("Error when calling "+commandStr+":"
            +e.getClass().getName()+" - "+e.getMessage()
            +"\nStackTrace:\n"+trace);
            }
            return ret;
        }
        return ret.err("Need more args for "+commandStr+"("+c+" vs "+args.size()+")");
    }

}
