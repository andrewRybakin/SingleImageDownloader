package com.mercury.singleimagedownloader;

import android.content.Context;
import android.content.Loader;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.Log;
import android.widget.ProgressBar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class ImageDownloader extends Loader<File> {
    private static final String LOG_TAG = "ImageDownloader";
    private URL imageUrl;
    private Context context;
    private ImageLoader l;
    private int totalSize;
    private ProgressBar pb;
    private File f;

    public ImageDownloader(Context c, String url, ProgressBar pbr) throws MalformedURLException {
        super(c);
        imageUrl = new URL(url);
        context = c;
        pb = pbr;
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

    private class ImageLoader extends AsyncTask<Void, Integer, File> {

        @Override
        protected void onProgressUpdate(Integer... values) {
            pb.setProgress(values[0] * 100 / totalSize);
            Log.d(LOG_TAG, values[0] * 100 / totalSize + " downloaded");
            super.onProgressUpdate(values);
        }

        @Override
        protected File doInBackground(Void... params) {
            InputStream iR = null;
            OutputStream oR = null;
            try {
                f = new File("/sdcard/aaa.jpg");
                if (f.length() > 0) {
                    f.delete();
                    f.createNewFile();
                }
                URLConnection urlConnection = imageUrl.openConnection();
                totalSize = urlConnection.getContentLength();
                iR = imageUrl.openStream();
                oR = new FileOutputStream(f);
                byte buf[] = new byte[1024];
                int readBytes;
                int progress = 0;
                while ((readBytes = iR.read(buf)) != -1) {
                    oR.write(buf, 0, readBytes);
                    progress += readBytes;
                    publishProgress(progress);
                    SystemClock.sleep(100);
                }
            } catch (IOException e) {
                e.printStackTrace();
                abandon();
                return null;
            } finally {
                try {
                    if (iR != null) {
                        iR.close();
                    }
                    if (oR != null) {
                        oR.close();
                    }
                } catch (IOException e) {
                    Log.e(LOG_TAG, "IOException on closing: " + e.getMessage());
                }
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
