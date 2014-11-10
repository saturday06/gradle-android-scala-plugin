package jp.leafytree.android.hello;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

@EActivity(R.layout.activity_hello)
public class HelloActivity extends Activity {
    @ViewById
    TextView scalaTextView;

    @AfterViews
    public void fillScalaTextView() {
        scalaTextView.setText(new HelloJava().say() + "\n" + new HelloScala().say());
    }
}
