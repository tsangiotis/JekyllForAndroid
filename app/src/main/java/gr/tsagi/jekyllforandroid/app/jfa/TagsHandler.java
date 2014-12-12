package gr.tsagi.jekyllforandroid.app.jfa;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.net.Uri;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import java.util.ArrayList;
import java.util.HashMap;

import gr.tsagi.jekyllforandroid.app.jfa.model.Tag;
import gr.tsagi.jekyllforandroid.app.provider.PostsContract;

import static gr.tsagi.jekyllforandroid.app.util.LogUtils.makeLogTag;

/**
 * Created by tsagi on 12/12/14.
 */
public class TagsHandler extends JSONHandler {
    private static final String TAG = makeLogTag(TagsHandler.class);

    private HashMap<String, Tag> mTags = new HashMap<String, Tag>();

    public TagsHandler(Context context) {
        super(context);
    }

    @Override
    public void process(JsonElement element) {
        for (Tag tag : new Gson().fromJson(element, Tag[].class)) {
            mTags.put(tag.tag, tag);
        }
    }

    @Override
    public void makeContentProviderOperations(ArrayList<ContentProviderOperation> list) {
        Uri uri = PostsContract.addCallerIsSyncAdapterParameter(
                PostsContract.Tags.CONTENT_URI);

        // since the number of tags is very small, for simplicity we delete them all and reinsert
        list.add(ContentProviderOperation.newDelete(uri).build());
        for (Tag tag : mTags.values()) {
            ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(uri);
            builder.withValue(PostsContract.Tags.TAG_ID, tag.tag);
            builder.withValue(PostsContract.Tags.TAG_NAME, tag.name);
            list.add(builder.build());
        }
    }

    public HashMap<String, Tag> getTagMap() {
        return mTags;
    }
}