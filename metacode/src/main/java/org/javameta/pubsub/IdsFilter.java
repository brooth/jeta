package org.javameta.pubsub;

/**
 * 
 */
public class IdsFilter implements Filter {

    private int[] ids;

    public IdsFilter(int... ids) {
        this.ids = ids;
    }

    @Override
    public boolean accepts(Object master, String methodName, Message msg) {
        int msgId = msg.getId();
        for (int id : ids)
            if (msgId == id)
                return true;

        return false;
    }
}
