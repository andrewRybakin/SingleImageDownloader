package com.mercury.singleimagedownloader;

import android.content.Context;
import android.content.Loader;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class ImageDownloader extends Loader<File> {
    private static final String LOG_TAG = "ImageDownloader";
    private URL imageUrl;
    private Context context;
    private ImageLoader l;
    private int totalSize;
    private int downloadedSize;
    private File f;

    public ImageDownloader(Context c, String url) throws MalformedURLException {
        super(c);
        imageUrl = new URL(url);
        context = c;
        Log.d(LOG_TAG, "create ImageLoader");
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        Log.d(LOG_TAG, "onStartLoading");
        l = new ImageLoader();
        l.execute();
    }

    @Override
    protected void onStopLoading() {
        super.onStopLoading();
        Log.d(LOG_TAG, "onStopLoading");
        l.cancel(true);
    }

    @Override
    protected void onReset() {
        super.onReset();
        Log.d(LOG_TAG, "onReset");
        if (f.exists()) f.delete();
    }

    private class ImageLoader extends AsyncTask<Void, Void, File> {

        @Override
        protected File doInBackground(Void... params) {
            try {
                f = new File(context.getFilesDir(), "temp.jpg");
                URLConnection urlConnection = imageUrl.openConnection();
                totalSize = urlConnection.getContentLength();
                Log.d(LOG_TAG,totalSize+"");
                InputStream iR = imageUrl.openStream();
                OutputStream oR=new FileOutputStream(f);
                byte buf[] = new byte[1024];
                downloadedSize = 0;
                int readBytes;
                while ((readBytes = iR.read(buf)) != -1) {
                    oR.write(buf);
                    downloadedSize += readBytes;
                    MainActivity.updateProgress(downloadedSize, totalSize);
                }
                Log.d(LOG_TAG, downloadedSize + "");
                iR.close();
                oR.close();
            } catch (IOException e) {
                e.printStackTrace();
                abandon();
                return null;
            }
            return f;
        }

        @Override
        protected void onPostExecute(File file) {
            super.onPostExecute(file);
            deliverResult(file);
        }
    }
}
