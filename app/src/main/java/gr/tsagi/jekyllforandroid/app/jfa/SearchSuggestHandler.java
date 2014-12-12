package gr.tsagi.jekyllforandroid.app.jfa;

import android.app.SearchManager;
import android.content.ContentProviderOperation;
import android.content.Context;
import android.net.Uri;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import java.util.ArrayList;
import java.util.HashSet;

import gr.tsagi.jekyllforandroid.app.provider.PostsContract;

import static gr.tsagi.jekyllforandroid.app.util.LogUtils.makeLogTag;

/**
 * Created by tsagi on 12/12/14.
 */
public class SearchSuggestHandler extends JSONHandler {
    private static final String TAG = makeLogTag( SearchSuggestHandler.class);
    HashSet<String> mSuggestions = new HashSet<String>();

    public SearchSuggestHandler(Context context) {
        super(context);
    }

    @Override
    public void process(JsonElement element) {
        for (String word : new Gson().fromJson(element, String[].class)) {
            mSuggestions.add(word);
        }
    }

    @Override
    public void makeContentProviderOperations(ArrayList<ContentProviderOperation> list) {
        Uri uri = PostsContract.addCallerIsSyncAdapterParameter(
                PostsContract.SearchSuggest.CONTENT_URI);

        list.add(ContentProviderOperation.newDelete(uri).build());
        for (String word : mSuggestions) {
            list.add(ContentProviderOperation.newInsert(uri)
                    .withValue(SearchManager.SUGGEST_COLUMN_TEXT_1, word)
                    .build());
        }
    }
}