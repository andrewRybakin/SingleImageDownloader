package com.mercury.singleimagedownloader;

import android.content.Context;
import android.content.Loader;
import android.os.AsyncTask;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemClock;
import android.util.Log;
import android.widget.ProgressBar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

public class ImageDownloader extends Loader<File> implements Parcelable {
    private static final String LOG_TAG = "ImageDownloader";
    private URL imageUrl;
    private ImageLoader l;
    private int totalSize;
    private ProgressBar pb;
    private static File f;
    private URLConnection urlConnection;
    private boolean abandoned;

    public static Parcelable CREATOR;

    public ImageDownloader(Context c, ProgressBar pbr) {
        super(c);
        pb = pbr;
        Log.d(LOG_TAG, "create ImageLoader");
    }

    public void newProgressBar(ProgressBar bar){ //Боюсь, такой метод является еретическим... Но выполняется он исключительно при повороте
        pb=bar;
    }

    public void begin(String url, String path) throws IOException{
        imageUrl=new URL(url);
        urlConnection = imageUrl.openConnection();
        f = new File(path);
        if (f.exists()) {
            f.delete();
            f.createNewFile();
        }
        forceLoad();
    }

    @Override
    protected void onForceLoad() {
        super.onForceLoad();
        l=new ImageLoader();
        l.execute();
    }

    @Override
    protected void onStopLoading() {
        super.onStopLoading();
        Log.d(LOG_TAG, "onStopLoading");
        if(l!=null)l.cancel(true);
        abandoned=true;
    }

    @Override
    public boolean isAbandoned() {
        return abandoned;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

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
                totalSize = urlConnection.getContentLength();
                iR = urlConnection.getInputStream();
                oR = new FileOutputStream(f);
                byte buf[] = new byte[1024];
                int readBytes;
                int progress = 0;
                while ((readBytes = iR.read(buf)) != -1) {
                    oR.write(buf, 0, readBytes);
                    progress += readBytes;
                    publishProgress(progress);
                    SystemClock.sleep(100); //Эт так, чтоб убедиться что прогрессбар работает
                }
            } catch (IOException e) {
                Log.e(LOG_TAG, "IOException on opening/reading/writing: " + e.getMessage());
                f=null;
            } finally {
                try {
                    if (iR != null) iR.close();
                    if (oR != null) oR.close();
                } catch (IOException e) {
                    Log.e(LOG_TAG, "IOException on closing: " + e.getMessage());
                    f=null;
                }
            }
            return f;
        }

        @Override
        protected void onPostExecute(File file) {
            super.onPostExecute(file);
            deliverResult(file);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            abandon();
        }
    }
}
