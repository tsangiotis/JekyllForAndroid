package gr.tsagi.jekyllforandroid.app.activities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity

/**
\* Created with IntelliJ IDEA.
\* User: jchanghong
\* Date: 10/20/14
\* Time: 15:14
\*/

abstract class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (layoutResource != 0) {
            setContentView(layoutResource)
        }

    }

    protected abstract val layoutResource: Int

}