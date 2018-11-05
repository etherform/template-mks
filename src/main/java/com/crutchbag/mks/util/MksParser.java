package com.crutchbag.mks.util;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import com.crutchbag.mks.util.MksHelper.Call;
import com.crutchbag.mks.util.MksHelper.Command;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;


@Component
public class MksParser {

    @Autowired
    private MksLogger logger;

    public String convertObjectToJson(Object obj) {
        return new Gson().toJson(obj);
    }

    public Map<String, Call> parseAnnotatedMethods(Object obj, Class<? extends Annotation> a) {
        Map<String, Call> map = new HashMap<>();
        Method[] ms = obj.getClass().getMethods();
        for (Method m : ms)
            if (m.isAnnotationPresent(a)) {
                map.put(m.getName(), new Call(obj, m));
            }
        return map;
    }

    public List<Command> parseCommandListFromMessage(@NonNull String json) {
        List<Command> parsedCommandList = new ArrayList<>();

        // parse JSON to an Object first to see if it's a collection, then parse it as a proper class
        Object o = new Object();
        try {
            o = new Gson().fromJson(json, Object.class);
        } catch (JsonSyntaxException e) {
            logger.logException("Failed to parse JSON to an object.", e);
            return parsedCommandList;
        }
        if (o instanceof Collection<?>) {
            logger.logInfo("Provided JSON supposedly contains multiple commands.");
            try {
                Command[] commandArray = new Gson().fromJson(json, Command[].class);
                for (Command command : commandArray) {
                    parsedCommandList.add(new Command(command.name, parseArgsObject(command.args)));
                }
            } catch (JsonSyntaxException e) {
                logger.logException("Failed to parse commands from JSON.", e);
                return parsedCommandList;
            }
        } else {
            logger.logInfo("Provided JSON supposedly contains a single command.");
            try {
                Command command = new Gson().fromJson(json, Command.class);
                parsedCommandList.add(new Command(command.name, parseArgsObject(command.args)));
            } catch (JsonSyntaxException e) {
                logger.logException("Failed to parse commands from JSON.", e);
                return parsedCommandList;
            }
        }

        logger.logInfo("Json parsing complete. Got:", parsedCommandList);
        return parsedCommandList;
    }

    @SuppressWarnings("unchecked")
    public Object parseArgsObject(Object o){    // this just checks if we got a list of strings or a single string as an arg
        List<Object> args = new ArrayList<>();
        if (o == null)
            return args;
        else if (o instanceof Collection<?>) {
            List<Object> objList = (List<Object>) o;
            for (Object obj : objList) {
                args.add(obj);
            }
        } else {
            args.add(o);
        }

        return args;
    }

    // have to import java.beans for this, but this supports both basic classes and primitive types so it's worth it
    public Object convertBasicClass(Class<?> targetType, String s) {
        if (s.getClass() == targetType) // if we need string type return it right away
            return s;

        PropertyEditor editor = PropertyEditorManager.findEditor(targetType);
        try {
            editor.setAsText(s);
        } catch (Exception e) {
            logger.logException("Failed to convert arg to expected type.", e);
            return null;
        }
        return editor.getValue();
    }

    @SuppressWarnings("unchecked")
    public Object[] processArgs(Parameter[] declaredArgs, Object[] argsArray) {
        List<Object> processedArgs = new ArrayList<>();
        Boolean isCollection;
        Boolean isArray;

        logger.logInfo("Required args amount:", declaredArgs.length);
        for (int i = 0; i < declaredArgs.length; i++) {
            logger.logInfo("Iterator:", i);
            logger.logInfo("Attempting to convert to:", declaredArgs[i].getType().getSimpleName());
            // check if method expects collection or array as an argument
            isCollection = Collection.class.isAssignableFrom(declaredArgs[i].getType());
            isArray = declaredArgs[i].getType().isArray();
            if ( isCollection || isArray) {
                if (!(argsArray[i] instanceof Collection<?>)) {
                    logger.logError("Collection expected, but wasn't provided. Provided:", convertObjectToJson(argsArray[i]));
                    return null;
                }
                List<Object> args = (List<Object>) argsArray[i];

                if (isCollection) {
                    ParameterizedType pt = (ParameterizedType) declaredArgs[i].getParameterizedType(); // get collection element type
                    if (pt.getActualTypeArguments().length != 1) // TODO put error here
                        return null;
                    Class<?> cls = (Class<?>) pt.getActualTypeArguments()[0]; // another way is Class.forname(typeName) but this seems to be better
                    logger.logInfo("Element type is:", cls.getSimpleName());
                    List<Object> objList = new ArrayList<>(); // new list to store processed args
                    for (Object arg : args) {
                        Object convertedArg = convertBasicClass(cls, (String) arg);
                        if (convertedArg == null)
                            return null;
                        else {
                            objList.add(convertedArg);
                        }
                    }
                    processedArgs.add(objList);
                } else {
                    Class<?> cls = declaredArgs[i].getType().getComponentType(); // get array element type
                    Object objArray = Array.newInstance(cls, args.size());
                    for (int j = 0; j < args.size(); j++) {
                        Object convertedArg = convertBasicClass(cls, (String) args.toArray()[j]);
                        if (convertedArg == null)
                            return null;
                        else {
                            Array.set(objArray, j, convertedArg);
                        }
                    }
                    processedArgs.add(objArray);
                }
                logger.logInfo("Arg conversion complete.");
            } else {
                Class<?> cls = declaredArgs[i].getType();
                Object convertedArg = convertBasicClass(cls, (String) argsArray[i]);
                if (convertedArg == null)
                    return null;
                else {
                    processedArgs.add(convertedArg);
                    logger.logInfo("Arg conversion complete.");
                }
            }
        }

        logger.logInfo("Args processing complete.");
        return processedArgs.toArray();
    }
}
