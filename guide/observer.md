<div class="page-header">
    <h2>Observer</h2>
</div>

Easy to use implementation of [observer pattern](https://en.wikipedia.org/wiki/Observer_pattern).

###Observable

    :::java
    class Observable {
        @Subject
        Observers<Event> observers;

        public Observable() {
            MetaHelper.createObservable(this);
        }
    }

Through `observers` you can fire an event:

    :::java
    observers.notify(event);

remove all observers:

    :::java
    observers.clear();

or both:

    :::java
    observers.notifyAndClear(event);


###Observer

To create an *Observer*, put `@Observe` annotation on the method that accepts one parameter - *Event* class.

    :::java
    class Observer {
        private ObserverHandler handler;

        public Observer(Observable observable) {
            handler = MetaHelper.registerObserver(this, observable);
        }

        @Observe(Observable.class)
        void onEvent(Event event) {
        }
    }

<span class="label label-info">Note</span> If you use an observable that extends *Observable*, you need to explicitly define the class of observable that fires the event: `MetaHelper.registerObserver(this, observable, Observable.class);`.

The `handler` allows you to detach *Observer* from *Observable*:

    :::java
    handler.unregisterAll(Observable.class);

Stop listening to a certain event:

    :::java
    handler.unregister(Event.class);


You can use one handler to control many *Observables*:

    :::java
    handler.add(MetaHelper.registerObserver(this, otherObservable));

And unregister all the listeners at once:

    :::java
    handler.unregisterAll();

###MetaHelper

    :::java
    public static void createObservable(Object master) {
        new ObservableController<>(metasitory, master).createObservable();
    }

    public static ObserverHandler registerObserver(Object observer, Object observable) {
        return new ObserverController<>(metasitory, observer).registerObserver(observable);
    }

    public static ObserverHandler registerObserver(Object observer, Object observable, Class cls) {
        return new ObserverController<>(metasitory, observer).registerObserver(observable, cls);
    }

Please, read [this article](/guide/meta-helper.html) if you have questions about *MetaHelper*.

You should also look at [*Event-Bus*](/guide/event-bus.html) features in addition to this guide.

