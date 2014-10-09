package jp.leafytree.android.hello;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class HelloJavaActivity extends Activity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hello);
        TextView scalaTextView = (TextView) findViewById(R.id.scala_text_view);
        scalaTextView.setText(new FlavorJava().name() + new FlavorScala().name());
    }
}
