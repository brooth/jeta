<div class="page-header">
    <h2>Retain</h2>
</div>

*Androjeta* lets you avoid usage of one of the most annoying-boilerplate-required-thing on Android. No need anymore to use `onSaveInstanceState` callback to retain sensitive data. For those of you who aren't familiar with this issue, here's the listing. In order to use fields for storing activities state, you have to take care of its recovery in case Android has [destroyed](http://developer.android.com/training/basics/activity-lifecycle/recreating.html) this activity.

    :::java
    class SampleActivity extends Activity {
        private String data;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (savedInstanceState != null)
                data = savedInstanceState.getString("data");
        }

        @Override
        protected void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);
            outState.putString("data", data);
        }
    }

Fortunately, `@Retain` annotation can do that for you:

    :::java
    class SampleActivity extends BaseActivity {
        @Retain
        String data;
    }

In the `BaseActivity` we need to call these two helper methods:

    :::java
     @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null)
            MetaHelper.restoreRetains(this, savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        MetaHelper.saveRetains(this, outState);
    }

And this is it. Excited? Yeah, me too.

###MetaHelper

Let's define these two methods:

    :::java
    public static void saveRetains(Activity activity, Bundle bundle) {
        new RetainController(metasitory, activity).save(bundle);
    }

    public static void restoreRetains(Activity activity, Bundle bundle) {
        new RetainController(metasitory, activity).restore(bundle);
    }

