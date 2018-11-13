package com.crutchbag.mks.log;

import com.google.gson.annotations.SerializedName;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class LogMessage {

    @SerializedName("type")
    public LogType type;
    @SerializedName("timestamp")
    public long timestamp;
    @SerializedName("message")
    public StringBuilder message;

}
