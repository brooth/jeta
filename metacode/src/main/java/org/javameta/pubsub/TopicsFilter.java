/*
 * Copyright 2015 Oleg Khalidov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.javameta.pubsub;

import java.util.Objects;

/**
 * @author Oleg Khalidov (brooth@gmail.com)
 */
public class TopicsFilter implements Filter {

    private String[] topics;

    public TopicsFilter(String... topics) {
        this.topics = topics;
    }

    @Override
    public boolean accepts(Object master, String methodName, Message msg) {
        String topic = msg.getTopic();
        if (topic == null)
            return false;

        for (String s : topics)
            if (Objects.equals(topic, s))
                return true;

        return false;
    }
}
