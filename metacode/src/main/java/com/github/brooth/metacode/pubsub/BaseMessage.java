package com.github.brooth.metacode.pubsub;

import javax.annotation.Nullable;

/**
 * @author khalidov
 * @version $Id$
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
