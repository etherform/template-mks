package com.crutchbag.mks.util;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
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
        String s = new Gson().toJson(obj);
        return s;
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
        // parse JSON to an Object first to see if it's a collection, then parse it as a proper class
        Object o = new Object();
        try {
            o = new Gson().fromJson(json, Object.class);
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
            return null;
        }
        List<Command> parsedCommandList = new ArrayList<>();
        if (o instanceof Collection<?>) {
            Command[] commandArray = new Gson().fromJson(json, Command[].class);
            for (Command command : commandArray) {
                parsedCommandList.add(new Command(command.name, parseArgsObject(command.args)));
            }
        } else {
            Command command = new Gson().fromJson(json, Command.class);
            parsedCommandList.add(new Command(command.name, parseArgsObject(command.args)));
        }

        System.out.println("[x] Json parsing complete.");
        return parsedCommandList;
    }

    @SuppressWarnings("unchecked")
    public Object parseArgsObject(Object o){
        List<Object> args = new ArrayList<>();
        if (o == null) // if no args return empty list
            return args;
        else if (o instanceof Collection<?>) {    // checking if multiple args provided
            List<Object> objList = (List<Object>) o;
            for (Object obj : objList) {
                args.add(obj);
            }
        } else {    // single arg provided
            args.add(o);
        }

        return args;
    }

    // have to import java.beans for this, but this supports both basic classes and primitives so it's worth it
    public Object convertBasicClass(Class<?> targetType, String s) {
        if (s.getClass() == targetType)
            return s;

        PropertyEditor editor = PropertyEditorManager.findEditor(targetType);
        try {
            editor.setAsText(s);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        System.out.println("[x] -> Argument conversion completed.");
        return editor.getValue();
    }

    @SuppressWarnings("unchecked")
    public Object processArgs(Parameter[] declaredArgs, Object[] argsArray) throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        List<Object> processedArgs = new ArrayList<>();

        for (int i = 0; i < declaredArgs.length; i++) {
            System.out.println("[x] -> Global Iterator: "+i);
            // check if method expects collection as an argument
            if (Collection.class.isAssignableFrom(declaredArgs[i].getType())) {
                System.out.println("[x] -> Collection type argument found.");
                ParameterizedType pt = (ParameterizedType) declaredArgs[i].getParameterizedType(); // get type of args inside collection
                if (pt.getActualTypeArguments().length != 1) // TODO put error here
                    return null;
                else if (!(argsArray[i] instanceof Collection<?>)) // collection expected, but wasn't provided
                    return null;
                else {
                    List<Object> args = (List<Object>) argsArray[i]; // should be a list<String> at this point
                    List<Object> objList = new ArrayList<>(); // new list to store processed args
                    Class<?> cls = (Class<?>) pt.getActualTypeArguments()[0]; // another way is  Class.forname(typeName) but this seems to be better

                    for (Object arg : args) {
                        Object convertedArg = convertBasicClass(cls, (String) arg);
                        if (convertedArg == null)
                            return null;
                        else {
                            objList.add(convertedArg);
                        }
                    }

                    processedArgs.add(objList);
                }
                // check if method expects array as an argument
            } else if (declaredArgs[i].getType().isArray()) {
                System.out.println("[x] -> Array type argument found.");
                if (!(argsArray[i] instanceof Collection<?>)) // array expected, but wasn't provided
                    return null;
                else {
                    List<Object> args = (List<Object>) argsArray[i]; // should be a list<String> at this point
                    List<Object> objList = new ArrayList<>(); // new list to store processed args
                    Class<?> cls = declaredArgs[i].getType().getComponentType();

                    for (Object arg : args) {
                        Object convertedArg = convertBasicClass(cls, (String) arg);
                        if (convertedArg == null)
                            return null;
                        else {
                            objList.add(convertedArg);
                        }
                    }

                    processedArgs.add(objList.toArray());
                }
                // at this point argsArray[i] should be an argument of basic class or primitive type
            } else {
                Class<?> cls = declaredArgs[i].getType();
                Object convertedArg = convertBasicClass(cls, (String) argsArray[i]);
                if (convertedArg == null)
                    return null;
                else {
                    processedArgs.add(convertedArg);
                }
            }
        }

        System.out.println("[x] Argument processing complete.");

        // dumb check here, might figure another way of doing this
        // for some reason error is thrown when you invoke a method with Object[] array that contains one item in it
        if (processedArgs.size() == 1)
            return processedArgs.get(0);
        else
            return processedArgs.toArray();
    }
}
