package com.mercury.singleimagedownloader;

import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.os.AsyncTask;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class ImageDownloader extends Loader<File> implements Parcelable {
    private static final String LOG_TAG = "ImageDownloader";
    private URL imageUrl;
    private ImageLoader loader;
    private int totalSize;
    private FileOutputStream fOutput;
    private URLConnection urlConnection;
    private boolean abandoned;

    //  Данное поле требует AndroidStudio, ибо в документации указано, что ежели я имплементю Parcelable,
    //  то мой класс должен содержать сие поле...
    public static Parcelable CREATOR=null;
    public static final String PROGRESS_EXTRA = "progressExtra";
    public static final String INTENT_P_UPDATE = "updateProgress";
    public static final int STATE_STANDBY = 0;
    public static final int STATE_DOWNLOADING = 1;
    public static final int STATE_COMPLETE = 2;

    private static int state = STATE_STANDBY;

    public static void setState(int newState) {
        state = newState;
    }

    public static boolean isDownloading() {
        return state == STATE_DOWNLOADING;
    }

    public static boolean isComplete() {
        return state == STATE_DOWNLOADING;
    }

    public ImageDownloader(Context c) {
        super(c);
        Log.d(LOG_TAG, "create ImageLoader");
    }

    public void begin(String url, String name) throws IOException {
        imageUrl = new URL(url);
        urlConnection = imageUrl.openConnection();
        fOutput = getContext().openFileOutput(name, 1);
        forceLoad();
    }

    @Override
    protected void onForceLoad() {
        super.onForceLoad();
        loader = new ImageLoader();
        loader.execute();
    }

    @Override
    protected void onStopLoading() {
        super.onStopLoading();
        Log.d(LOG_TAG, "onStopLoading");
        if (loader != null) {
            loader.cancel(true);
            if (loader.getProgress() < 100) abandoned = true;
        }
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
            LocalBroadcastManager.getInstance(getContext()).sendBroadcast(
                    (new Intent(INTENT_P_UPDATE)).putExtra(PROGRESS_EXTRA, values[0] * 100 / totalSize).setType("text/*"));
            super.onProgressUpdate(values);
        }

        public int getProgress() {
            return progress * 100 / totalSize;
        }

        public int progress;

        @Override
        protected File doInBackground(Void... params) {
            InputStream iR = null;
            File f;
            try {
                totalSize = urlConnection.getContentLength();
                iR = urlConnection.getInputStream();
                byte buf[] = new byte[1024];
                int readBytes;
                progress = 0;
                while ((readBytes = iR.read(buf)) != -1) {
                    fOutput.write(buf, 0, readBytes);
                    progress += readBytes;
                    publishProgress(progress);
                    SystemClock.sleep(100); //Эт так, чтоб убедиться что прогрессбар работает
                }
            } catch (IOException e) {
                Log.e(LOG_TAG, "IOException on opening/reading/writing: " + e.getMessage());
                return null;
            } finally {
                try {
                    if (iR != null) iR.close();
                    if (fOutput != null) fOutput.close();
                    f = new File(getContext().getFilesDir().getAbsolutePath() + "/temp.jpg");
                } catch (IOException e) {
                    Log.e(LOG_TAG, "IOException on closing: " + e.getMessage());
                    f = null;
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
