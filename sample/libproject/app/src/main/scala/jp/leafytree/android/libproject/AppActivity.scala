package jp.leafytree.android.libproject

import android.app.Activity
import android.os.Bundle
import android.widget.TextView
import jp.leafytree.android.libproject.lib1.Lib1Java

class AppActivity extends Activity {
    override def onCreate(savedInstanceState: Bundle) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app)
        val messageTextView = findViewById(R.id.message_text_view).asInstanceOf[TextView]
        messageTextView.setText(new Lib1Java().getName())
    }
}
