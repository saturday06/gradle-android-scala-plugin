package jp.leafytree.android.libproject;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.TextView;

import jp.leafytree.android.libproject.lib1.Lib1Java;

public class AppActivity extends ActionBarActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app);
        TextView messageTextView = (TextView) findViewById(R.id.message_text_view);
        messageTextView.setText(new Lib1Java().getName());
    }
}
