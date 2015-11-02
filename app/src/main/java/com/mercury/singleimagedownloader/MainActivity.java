package com.mercury.singleimagedownloader;

import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks {

    private static final String LOG_TAG = "MainActivity";
    private static final String KEY_DOWNLOADING = "com.mercury.singleimagedownloader.MainActivity.isDownloading";
    private static final String KEY_DOWNLOADER_OBJECT = "com.mercury.singleimagedownloader.MainActivity.downloaderObject";

    private ProgressBar progressBar;
    private Button button;
    private TextView textView;

    private ImageDownloader imageDownloader;
    private File imageFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setMax(100);
        button = (Button) findViewById(R.id.action_button);
        textView = (TextView) findViewById(R.id.status_label);
        progressBar.setVisibility(View.INVISIBLE);
        textView.setText(R.string.state_standby);
        button.setText(R.string.button_start);
        ImageDownloader.setState(ImageDownloader.STATE_STANDBY);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (imageDownloader != null) {
                        imageDownloader.begin(getString(R.string.image_URL), "temp.jpg");
                        loadingBegins();
                    }
                } catch (IOException e) {
                    sayToast("IO Error: " + e.getMessage());
                }
            }
        });
        LocalBroadcastManager.getInstance(getApplicationContext())
                .registerReceiver(new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        progressBar.setProgress(intent.getIntExtra(ImageDownloader.PROGRESS_EXTRA, 0));
                    }
                }, IntentFilter.create(ImageDownloader.INTENT_P_UPDATE, "text/*"));

        getLoaderManager().initLoader(0, null, this);
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_DOWNLOADING, ImageDownloader.isDownloading());
        if ((!ImageDownloader.isComplete()) && (imageDownloader != null))
            outState.putParcelable(KEY_DOWNLOADER_OBJECT, imageDownloader);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (getLoaderManager().getLoader(0).isAbandoned()) loadingRollback();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        imageDownloader = savedInstanceState.getParcelable(KEY_DOWNLOADER_OBJECT);
        //Log.d(LOG_TAG, state.name());
        if (savedInstanceState.getBoolean(KEY_DOWNLOADING))
            loadingBegins();
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        if (id == 0) imageDownloader = new ImageDownloader(getApplicationContext());
        return imageDownloader;
    }

    @Override
    public void onLoadFinished(Loader loader, Object data) {
        Log.d(LOG_TAG, "onLoadFinished + " + (data == null));
        if (data != null) {
            imageFile = (File) data;
            loadComplete();
        } else {
            loadingRollback();
            loader.reset();
            sayToast("There was an error occurred while downloading. Try later");
        }
    }

    @Override
    public void onLoaderReset(Loader loader) {
        Log.d(LOG_TAG, "onLoadReset");
    }

    private void loadingBegins() {
        Log.d(LOG_TAG, "Begin");
        ImageDownloader.setState(ImageDownloader.STATE_DOWNLOADING);
        progressBar.setVisibility(View.VISIBLE);
        textView.setText(R.string.state_downloading);
        button.setText(R.string.button_wait);
        button.setEnabled(false);
    }

    private void loadingRollback() {
        Log.d(LOG_TAG, "Rollback");
        ImageDownloader.setState(ImageDownloader.STATE_STANDBY);
        progressBar.setVisibility(View.INVISIBLE);
        textView.setText(R.string.state_standby);
        button.setText(R.string.button_start);
        button.setEnabled(true);
    }

    private void loadComplete() {
        Log.d(LOG_TAG, "Complete!");
        ImageDownloader.setState(ImageDownloader.STATE_COMPLETE);
        progressBar.setVisibility(View.INVISIBLE);
        textView.setText(R.string.state_downloaded);
        button.setText(R.string.button_show);
        button.setEnabled(true);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(imageFile), "image/*");
                startActivity(intent);
            }
        });
    }

    private void sayToast(String tost) {
        Toast.makeText(getApplicationContext(), tost, Toast.LENGTH_LONG).show();
    }
}
