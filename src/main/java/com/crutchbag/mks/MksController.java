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
import com.crutchbag.mks.log.MksLogger;
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
        return parser.convertPojoToJson(commandList);
    }

    public String getCommandArgs(String s) {
        if (callMap.keySet().contains(s)) {
            Call c = callMap.get(s);
            List<String> argsType = new ArrayList<>();
            for (Parameter par : c.method.getParameters()) {
                argsType.add(par.getType().getSimpleName());
            }
            return parser.convertPojoToJson(new Command(s,argsType));
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
        return parser.convertPojoToJson(commandList);
    }

    public void control(String json) {
        // first of all callMap must not be empty otherwise there's no point in going further
        if (callMap.size() == 0) {
            logger.logError("There are no methods exposed for invocation.");
            return;
        }

        // parse incoming JSON to command class
        Command command = parser.isValidJson(json) && !parser.isJsonArray(json) ? (Command) parser.convertJsonToPojo(json, Command.class) : null;
        if (command == null || command.name == null || command.name.isEmpty()) {
            logger.logError("JSON parsing failed. Command field is absent or blank.");
            return;
        }

        // checking if command(s) is registered as exposed method
        Call c = callMap.get(command.name);
        if (c == null) {
            logger.logError(command.name.concat(" is not recognized as a command."));
        } else {
            logger.logInfo("Attemping to run command:", command.name);

            Object[] parsedArgs = parser.parseCommandArgs(command.args);
            Parameter[] declaredArgs = c.method.getParameters();

            // don't even start args processing if arg count doesn't match
            if (parsedArgs.length != declaredArgs.length) {
                logger.logError("Amount of args provided is incorrect. Failed to run command:", command.name);
                // or if we don't need any
            } else if (declaredArgs.length == 0) {
                logger.logInfo("No args required. Attempting method invocation.");
                executeCommand(c, null);
            } else {
                logger.logInfo("Args required. Attempting args processing.");
                // processArgs() checks if args are of correct type for requested method & converts to proper type for usage
                Object[] convertedArgs = parser.processArgs(declaredArgs, parsedArgs);
                if (convertedArgs != null) {
                    logger.logInfo("Attempting method invocation.");
                    executeCommand(c, convertedArgs);
                }
            }
        }

    }

    private void executeCommand(Call c, Object[] args) {
        try {
            if (args == null) {
                c.method.invoke(c.object);
            } else {
                c.method.invoke(c.object, args);
            }
            logger.logInfo("Method invoked.");
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            logger.logError("Expected number of arguments:", c.method.getParameterCount());
            logger.logError("Args object is:", parser.convertPojoToJson(args));
            logger.logException("Failed to invoke method: "+c.method.getName(), e);
        }
    }
}
