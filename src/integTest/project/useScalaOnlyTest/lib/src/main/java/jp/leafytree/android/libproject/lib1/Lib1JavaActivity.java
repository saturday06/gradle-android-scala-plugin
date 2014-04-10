package jp.leafytree.android.libproject.lib1;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class Lib1JavaActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TextView textView = new TextView(this);
        textView.setText("Lib1Java");
        setContentView(textView);
    }
}
