package gr.tsagi.jekyllforandroid;

import in.uncod.android.bypass.Bypass;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

public class PreviewMarkdownActivity extends Activity {
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);
        
        Intent myIntent = getIntent();

        String mContent = myIntent.getStringExtra("content");
        
        TextView text = (TextView) findViewById(R.id.previewTextView);
        Bypass bypass = new Bypass();
        CharSequence string = bypass.markdownToSpannable(mContent);
        text.setText(string);
        text.setMovementMethod(LinkMovementMethod.getInstance());
    }

}
