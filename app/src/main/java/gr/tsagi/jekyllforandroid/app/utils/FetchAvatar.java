package gr.tsagi.jekyllforandroid.app.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;

import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.UserService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

/**
 * Created by tsagi on 11/9/14.
 */
public class FetchAvatar extends AsyncTask<Void, Void, Void> {

    private Context context;
    private Utility utility;

    public FetchAvatar(Context c) {
        context = c;
        utility = new Utility(c);
    }

    @Override
    protected Void doInBackground(Void... params) {
        GitHubClient client = new GitHubClient();
        client.setOAuth2Token(utility.getToken());

        UserService userService = new UserService(client);

        User user = new User();
        String imgUrl = "";

        try {
            user = userService.getUser();
            imgUrl = user.getAvatarUrl();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            String path = Environment.getExternalStorageDirectory().toString() + "/jfa";
            OutputStream fOut = null;
            File file = new File(path, "avatar.png");
//            if (!file.exists()) {
                file.mkdir();
//            }
            fOut = new FileOutputStream(file);
            Bitmap pictureBitmap = BitmapFactory.decodeStream((InputStream) new URL(imgUrl).getContent()); // obtaining the Bitmap
            pictureBitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut); // saving the Bitmap to
            // a file compressed as a JPEG with 85% compression rate
            fOut.flush();
            fOut.close(); // do not forget to close the stream
        } catch (Exception e) {
            e.printStackTrace();
        }

        final SharedPreferences settings = context.getSharedPreferences(
                "gr.tsagi.jekyllforandroid", Context.MODE_PRIVATE);

        SharedPreferences.Editor edit = settings.edit();
        edit.putString("AvatarUrl", imgUrl);
        edit.apply();

        return null;

    }

}