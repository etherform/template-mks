package com.crutchbag.mks.log;

import com.google.gson.annotations.SerializedName;

import lombok.Data;

@Data
public class MksLogMessage {

    @SerializedName("type")
    public String type;
    @SerializedName("timestamp")
    public long timestamp;
    @SerializedName("message")
    public String message;

}
