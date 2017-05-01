<div class="page-header">
    <h2>Jeta Collectors</h2>
</div>

Well, *Jeta Collectors* allow you to "collect" scattered classes (types) in your project. Let's get through with an example. Assume we have an *EventManager* that receives events outside and invokes appropriate handlers:

    :::java
    interface EventHandler {
        void handle(Event event);
    }

    enum EventType {
        EVENT_ONE,
        EVENT_TWO
    }

    class EventManager {
        Map<EventType, EventHandler> handlers;
        //...
    }

Commonly used practice is to create an xml file and define handlers in it. In the event manager we have to parse this xml file and load the handlers using `Class.forName()`.

    :::java
    class HandlerOne implements EventHandler {
        void handle(Event event) {
        }
    }

    class HandlerTwo implements EventHandler {
        void handle(Event event) {
        }
    }

<span/>

    :::xml
    <handlers>
        <handler type="EVENT_ONE" class="com.example.collector.abc.HandlerOne"/>
        <handler type="EVENT_TWO" class="com.example.collector.bcd.abc.HandlerTwo"/>
    </handlers>

<span/>

    :::java
    class EventManager {
        void collectHandlers() {
            // parse XML, Class.forName(), etc
        }
    }

Besides, this approach has no validation - fails at runtime in case of misspelling. It's frequently forgotten to modify the xml file each time you add a new handler into your project.

###TypeCollector

*TypeCollector* searches for the types that use given annotation and collect them during compilation. Let's modify the example above to demonstrate its potential:

    :::java
    @interface Handler {
        EventType value();
    }

    @Handler(EVENT_ONE)
    class HandlerOne implements EventHandler {
        void handle(Event event) {
        }
    }

    @Handler(EVENT_TWO)
    class HandlerTwo implements EventHandler {
        void handle(Event event) {
        }
    }

    @TypeCollector(Handler.class)
    class EventManager {
        void collectHandlers() {
            Collection<? extends Class> types =
                MetaHelper.collectTypes(EventManager.class, Handler.class);
            Map<EventType, Class> handlers = new HashMap(types.size());
            for (Class type : types)
                handlers.put(type.getAnnotation(Handler.class).value(), type);
        }
    }

As you expect, `MetaHelper.collectTypes` will return a collection with two items - `HandlerOne` and `HandlerTwo`. So, if you add a new handler and put `@Handler` on, *TypeCollector* will return it as well, without any amendments.

###ObjectCollector

*ObjectCollector* does the same as *TypeCollector*, but returns providers instead of classes. It's useful if you need to create instances of the annotation users:

    :::java
    @ObjectCollector(Handler.class)
    class EventManager {
        Map<EventType, Provider<EventHandler>> handlers;

        void collectHandlers() {
            List<Provider<?>> objects =
                MetaHelper.collectObjects(EventManager.class, Handler.class);
        }
    }

###MetaHelper

Here are two helper method for the examples above:

    :::java
    public static List<Class<?>> collectTypes(Class<?> masterClass, Class<? extends Annotation> annotationClass) {
        return new TypeCollectorController(metasitory, masterClass).getTypes(annotationClass);
    }

    public static List<Provider<?>> collectObjects(Class<?> masterClass, Class<? extends Annotation> annotationClass) {
        return new ObjectCollectorController(metasitory, masterClass).getObjects(annotationClass);
    }

