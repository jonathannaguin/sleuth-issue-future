package com.example.demo;

import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

public class Message
{
    private String body;
    private Map<String, String> messageAttributes = new HashMap<>();

    public String getBody()
    {
        return body;
    }

    public void setBody(String body)
    {
        this.body = body;
    }

    public Map<String, String> getMessageAttributes()
    {
        return messageAttributes;
    }

    public void setMessageAttributes(Map<String, String> messageAttributes)
    {
        this.messageAttributes = messageAttributes;
    }

    @Override
    public String toString()
    {
        return new StringJoiner(", ", Message.class.getSimpleName() + "[", "]").add("body='" + body + "'")
                                                                               .add("messageAttributes=" + messageAttributes)
                                                                               .toString();
    }
}
