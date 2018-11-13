package com.crutchbag.mks.util;

import java.lang.reflect.Method;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import lombok.AllArgsConstructor;

public final class MksHelper {

    private MksHelper() {
        throw new IllegalStateException("Utility class");
    }

    /*
     * Static Helper classes below
     */
    @AllArgsConstructor
    public static class Call {
        public Object object;
        public Method method;
    }

    // can't use @AllArgsConstructor or @Data because constructor needs to accept null field for this class
    public static class Command {

        @SerializedName("command")
        @Expose
        public String name;

        // we could define it as list<String>, but it can contain a list<String> as an element
        @SerializedName("args")
        @Expose
        public Object args;

        public Command(String s, Object o) {
            name = s;
            args = o;
        }

        @Override
        public String toString() {
            return new Gson().toJson(this);
        }
    }

    // keeping this for now
    @AllArgsConstructor
    public static class Pair<A, B> {
        public A a;
        public B b;

    }

    /*
     * Static Helper methods below
     */

    public static String formatDateTime(long time) {
        return String.format("%1$tF %1$tH:%1$tM:%1$tS", time);
    }

    public static String formatTimestamp(long time) {
        String s = String.format("%1$tS.%1$tN ", time);
        return s.substring(0, s.length()-6);
    }
}
