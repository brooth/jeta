package org.javameta.pubsub;

import javax.annotation.Nullable;

/**
 * 
 */
public class BaseMessage implements Message {

    protected int id = 0;
    protected String topic;

    public BaseMessage() {
    }

    public BaseMessage(int id) {
        this.id = id;
    }

    public BaseMessage(int id, String topic) {
        this.id = id;
        this.topic = topic;
    }

    public BaseMessage(String topic) {
        this.topic = topic;
    }

    @Override
    public int getId() {
        return id;
    }

    @Nullable
    @Override
    public String getTopic() {
        return topic;
    }
}
