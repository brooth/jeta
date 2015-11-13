package com.github.brooth.metacode.broadcast;

import javax.annotation.Nullable;

/**
 * @author khalidov
 * @version $Id$
 */
public class BaseMessage implements Message {

    protected int id;
    protected String tag;

    public BaseMessage(int id) {
        this.id = id;
    }

    public BaseMessage(int id, String tag) {
        this.id = id;
        this.tag = tag;
    }

    @Override
    public int getId() {
        return 0;
    }

    @Nullable
    @Override
    public String getTag() {
        return null;
    }
}
