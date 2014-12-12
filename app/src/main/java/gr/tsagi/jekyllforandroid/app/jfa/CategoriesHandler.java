package gr.tsagi.jekyllforandroid.app.jfa;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.net.Uri;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import java.util.ArrayList;
import java.util.HashMap;

import gr.tsagi.jekyllforandroid.app.jfa.model.Category;
import gr.tsagi.jekyllforandroid.app.provider.PostsContract;

import static gr.tsagi.jekyllforandroid.app.util.LogUtils.makeLogTag;

/**
 * Created by tsagi on 12/12/14.
 */
public class CategoriesHandler extends JSONHandler{
    private static final String TAG = makeLogTag(CategoriesHandler.class);

    private HashMap<String, Category> mCategories = new HashMap<String, Category>();

    public CategoriesHandler(Context context) {
        super(context);
    }

    @Override
    public void process(JsonElement element) {
        for (Category category : new Gson().fromJson(element, Category[].class)) {
            mCategories.put(category.category, category);
        }
    }

    @Override
    public void makeContentProviderOperations(ArrayList<ContentProviderOperation> list) {
        Uri uri = PostsContract.addCallerIsSyncAdapterParameter(
                PostsContract.Categories.CONTENT_URI);

        // since the number of tags is very small, for simplicity we delete them all and reinsert
        list.add(ContentProviderOperation.newDelete(uri).build());
        for (Category category : mCategories.values()) {
            ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(uri);
            builder.withValue(PostsContract.Categories.CATEGORY_ID, category.category);
            builder.withValue(PostsContract.Categories.CATEGORY_NAME, category.name);
            list.add(builder.build());
        }
    }

    public HashMap<String, Category> getCategoryMap() {
        return mCategories;
    }
}
