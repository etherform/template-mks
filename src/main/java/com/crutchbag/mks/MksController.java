package com.crutchbag.mks;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.crutchbag.mks.util.MksHelper.Call;
import com.crutchbag.mks.util.MksHelper.Command;
import com.crutchbag.mks.util.MksLogger;
import com.crutchbag.mks.util.MksParser;

@Component
public class MksController {

    @Autowired
    private MksLogger logger;

    @Autowired
    private MksParser parser;

    @Autowired
    private Map<String, Call> callMap;

    public String getCommands() {
        List<Command> commandList = new ArrayList<>();
        for (String s : callMap.keySet()) {
            commandList.add(new Command(s, null));
        }
        return parser.convertObjectToJson(commandList);
    }

    public String getCommandArgs(String s) {
        if (callMap.keySet().contains(s)) {
            Call c = callMap.get(s);
            List<String> argsType = new ArrayList<>();
            for (Parameter par : c.method.getParameters()) {
                argsType.add(par.getType().getSimpleName());
            }
            return parser.convertObjectToJson(new Command(s,argsType));
        } else
            return "Command not found.";
    }

    public String getCommandsWithArgs() {
        List<Command> commandList = new ArrayList<>();
        for (Map.Entry<String, Call> entry : callMap.entrySet()) {
            Call c = entry.getValue();
            List<String> argsType = new ArrayList<>();
            for (Parameter par : c.method.getParameters()) {
                argsType.add(par.getType().getSimpleName());
            }
            commandList.add(new Command(entry.getKey(),argsType));
        }
        return parser.convertObjectToJson(commandList);
    }

    public void control(String json) throws InvocationTargetException, IllegalAccessException, InstantiationException, NoSuchMethodException {
        // parse incoming JSON
        List<Command> commandList = parser.parseCommandListFromMessage(json);
        if (commandList ==  null)
            return;

        for (Command command : commandList) {
            // checking if command(s) is registered as exposed method
            Call c = callMap.get(command.name);
            if (c == null) {
                logger.logError(command.name + " is not recognized as a command.");
            } else {
                System.out.println("[x] Running command: "+command.name);
                @SuppressWarnings("unchecked")
                List<Object> argList = (List<Object>) command.args; // it should always be a list at this point
                Parameter[] declaredArgs = c.method.getParameters();

                // don't even start args processing if don't need them
                if (argList.size() == declaredArgs.length && declaredArgs.length == 0) {
                    c.method.invoke(c.object, argList.toArray()); // empty array here
                } else  if (argList.size() != declaredArgs.length) { // don't even start args processing if arg count doesn't match
                    logger.logError("Failed to run command: "+command.name+". With provided amount of args.");
                } else {
                    System.out.println("[x] Processing arguments.");
                    // processArgs() checks if they are of correct type for requested method, converts them to proper type for usage
                    Object convertedArgs = parser.processArgs(declaredArgs, argList.toArray());
                    if (convertedArgs != null) {
                        System.out.println("[x] Invoking method.");
                        c.method.invoke(c.object, convertedArgs);
                        System.out.println("[x] Finished.");
                    }
                }
            }

        }
    }

}
