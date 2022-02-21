package com.example.demo;

import java.util.StringJoiner;

public class MyObjectWithTracing
{
    private String traceId;
    private String spanId;

    public String getTraceId()
    {
        return traceId;
    }

    public String getSpanId()
    {
        return spanId;
    }

    public void setTraceId(String traceId)
    {
        this.traceId = traceId;
    }

    public void setSpanId(String spanId)
    {
        this.spanId = spanId;
    }

    @Override
    public String toString()
    {
        return new StringJoiner(", ", MyObjectWithTracing.class.getSimpleName() + "[", "]").add("traceId='" + traceId + "'")
                                                                                           .add("spanId='" + spanId + "'")
                                                                                           .toString();
    }
}

