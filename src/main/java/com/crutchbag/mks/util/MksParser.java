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
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;


@Component
public class MksParser {

    @Autowired
    private MksLogger logger;

    private Gson gson = new Gson();

    public Map<String, Call> parseAnnotatedMethods(Object obj, Class<? extends Annotation> a) {
        Map<String, Call> map = new HashMap<>();
        Method[] ms = obj.getClass().getMethods();
        for (Method m : ms)
            if (m.isAnnotationPresent(a)) {
                map.put(m.getName(), new Call(obj, m));
            }
        return map;
    }

    @SuppressWarnings("unused")
    public Boolean isValidJson(@NonNull String s) {
        try {
            Object o = gson.fromJson(s, Object.class);
            return true;
        } catch(JsonSyntaxException e) {
            logger.logException("Provided JSON is invalid.", e);
            return false;
        }
    }

    public Boolean isJsonArray(@NonNull String s) {
        return gson.fromJson(s, JsonElement.class).isJsonArray();
    }

    public Object convertJsonToPojo(@NonNull String json, @NonNull Class<?> cls) {
        return gson.fromJson(json, cls);
    }

    public String convertPojoToJson(Object obj) {
        return gson.toJson(obj);
    }

    @SuppressWarnings("unchecked")
    public Object[] parseCommandArgs(Object o){    // this checks if we got ( no args || single arg || multiple args)
        if (o == null)
            return new Object[] {};
        else if (o instanceof Collection<?>)
            return ((List<Object>) o).toArray();
        else
            return new Object[] { o };
    }

    // have to import java.beans for this, but this supports both basic classes and primitive types so it's worth it
    public Object convertBasicClass(Class<?> targetType, String s) {
        if (s.getClass() == targetType) // if we need string type return it right away
            return s;

        PropertyEditor editor = PropertyEditorManager.findEditor(targetType);
        try {
            editor.setAsText(s);
        } catch (IllegalArgumentException e) { // TODO figure list of more specific exceptions
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

        logger.logInfo("Required arg amount:", declaredArgs.length);
        for (int i = 0; i < declaredArgs.length; i++) {
            logger.logInfo("Iterator:", i);
            logger.logInfo("Attempting to convert to:", declaredArgs[i].getType().getSimpleName());
            // check if method expects collection or array as an argument
            isCollection = Collection.class.isAssignableFrom(declaredArgs[i].getType());
            isArray = declaredArgs[i].getType().isArray();
            if ( isCollection || isArray) {
                if (!(argsArray[i] instanceof Collection<?>)) {
                    logger.logError("Collection expected, but wasn't provided. Provided:", convertPojoToJson(argsArray[i]));
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
