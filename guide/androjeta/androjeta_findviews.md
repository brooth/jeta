<div class="page-header">
    <h2>FindView</h2>
</div>

`@FindView` allows you to bind UI-components from the layouts into your fragments and activities. You don't have to use `findViewById()` anymore, cast `View` to the actual class, pass the IDs from `R` file. *Androjeta* does it all for you.

Let's go through this feature with an example. Assume we have a layout:

    :::xml
    <LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
        android:layout_width="match_parent"
        android:layout_height="wrap_content">

        <TextView
            android:id="@+id/sampleActivity_textView"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"/>
    </LinearLayout>


To bind the `TextView` into our Activity as simple as:

    :::java
    class SampleActivity extends BaseActivity {
        @FindView
        TextView textView;
    }

How does it work? Well, by default Androjeta composes the ID as:

`<lowercased activity name> + "_" + <field name>`

So, in our example it's: *"sampleActivity_textView"*.

<span class="label label-info">Note</span> You shouldn't distrust this approach. In case of misspelling, it get compile-time error. Also, it encourages you to keep your code clean and in one style.

Here's the `BaseActivity`:

    :::java
    class BaseActivity extends Activity {
        @Override
        public void setContentView(int layoutResID) {
            super.setContentView(layoutResID);
            MetaHelper.findViews(this);
        }
    }

For sure, you can pass whatever `R.id` you want:

    :::java
    class SampleActivity extends BaseActivity {
        @FindView(R.id.sampleActivity_textView)
        TextView textView;
    }

or, what is important for library modules (`aar` files), you can pass only ID as a string:

    :::java
    class SampleActivity extends BaseActivity {
        @FindView(name = "sampleActivity_textView")
        TextView textView;
    }

You can use `FindView` not just inside an activity, but any object. Pass the `View` instance in which you want to find the views as parameter:

    :::java
    public static class ViewHolder extends RecyclerView.ViewHolder {
        @FindView
        protected CardView cardView;
        @FindView
        protected TextView titleTextView;
        @FindView
        protected Button okButton;

        public ViewHolder(View view) {
            super(view);
            MetaHelper.findViews(this, view);
        }
    }


###MetaHelper

    :::java
    public static void findViews(Activity activity) {
        new FindViewController(metasitory, activity).findViews();
    }

    public static void findViews(Object master, View view) {
        new FindViewController(metasitory, master).findViews(view);
    }

Please, read the article about [MetaHelper](/guide/meta-helper.html) if you haven't yet.
