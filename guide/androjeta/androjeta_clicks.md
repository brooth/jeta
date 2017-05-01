<div class="page-header">
    <h2>OnClick and OnLongClick</h2>
</div>

With `@OnClick` and `@OnLongClick` annotations you can use your methods as `android.view.View.OnClickListener
` and `android.view.View.OnLongClickListener` respectively. Also, no need to implement any interfaces:

    :::java
    public class SampleActivity extends BaseActivity {
        @OnClick
        void onClickSaveButton() {
        }

        @OnLongClick
        void onLongClickSaveButton() {
        }
    }

As well as in case of [@FindView](/guide/androjeta/findviews.html), *Androjeta* composes the IDs by default:

`<lowercased activity name> + "_" + <method name without prefix>`

Where `prefix` is  *onClick* or *onLongClick* respectively.

<span class="label label-info">Note</span> For sure there's no reason to distrust this approach. In case of misspelling, the code won't be compiled.

Clearly, for this example, both `onClickSaveButton` and `onLongClickSaveButton` are bound to a view by id `R.id.sampleActivity_saveButton`.

<span class="label label-info">Note</span> *OnLongClick* allows you to set method's return type to `void`. In this case *Androjeta* will return `true` by default. Change it to `boolean` otherwise.

You can also define IDs explicitly:

    :::java
    @OnClick(R.id.sampleActivity_saveButton)
    void onClickSaveButton() {
    }

or

    :::java
    @OnClick(name = "sampleActivity_saveButton")
    void onClickSaveButton() {
    }


`BaseActivity`:

    :::java
    class BaseActivity extends Activity {
        @Override
        public void setContentView(int layoutResID) {
            super.setContentView(layoutResID);
            MetaHelper.applyOnClicks(this);
        }
    }

###MetaHelper

The helper method would be:

    :::java
    public static void applyOnClicks(Activity activity) {
        new OnClickController(metasitory, activity).addListeners();
    }
