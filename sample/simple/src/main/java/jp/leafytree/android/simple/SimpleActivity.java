package jp.leafytree.android.simple;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class SimpleActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple);
        TextView scalaTextView = (TextView) findViewById(R.id.scala_text_view);
        scalaTextView.setText(new HelloScala().say());
    }
}
