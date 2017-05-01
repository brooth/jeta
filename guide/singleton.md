<div class="page-header">
    <h2>Singleton and Multiton</h2>
</div>

###Singleton

*Singleton* is the less useful feature of *Jeta*. You can use it if you feel too lazy and, for example, in tests when you need a thread-safe singleton.

    :::java
    @Singleton
    public class SingletonSample {
        public static SingletonSample getInstance() {
            return MetaHelper.getSingleton(SingletonSample.class).getSingleton();
        }
    }


###Multiton

Unlike *Singleton*, *Multiton* solves quite complex problem. If you've ever tried to implement a thread-safe multiton pattern, you would probably know. If you haven't, you can read [this thread](http://stackoverflow.com/questions/11126866/thread-safe-multitons-in-java) on *StackOverFlow* to be aware.

    :::java
    @Multiton
    public class MultitonSample {
        static MultitonMetacode<MultitonSample> multiton =
            MetaHelper.getMultiton(MultitonSample.class);

        public static MultitonSample getInstance(String key) {
            return multiton.getMultiton(key);
        }

        private Object key;

        MultitonSample(Object key) {
            this.key = key;
        }
    }

For the performance reasons we hold multiton metacode in a static field.


###MetaHelper

The helper methods would be:

    :::java
    public static <M> getSingleton(Class<M> masterClass) {
        return new SingletonController<>(getInstance().metasitory, masterClass).getSingleton();
    }

    public static <M> MultitonMetacode<M> getMultiton(Class<M> masterClass) {
        return new MultitonController<>(getInstance().metasitory, masterClass).getMetacode();
    }
