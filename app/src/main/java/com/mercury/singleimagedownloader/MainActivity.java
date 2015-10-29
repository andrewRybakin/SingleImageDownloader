package com.mercury.singleimagedownloader;

import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
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
    private static final String KEY_DOWNLODER_OBJECT = "com.mercury.singleimagedownloader.MainActivity.downloaderObject";

    private enum STATE {
        STANDBY, DOWNLOADING, COMPLETE
    }

    ;

    private STATE state;

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
        state = STATE.STANDBY;
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (imageDownloader != null) {
                        imageDownloader.begin(getString(R.string.image_URL), Environment.getExternalStorageDirectory()+"/temp.jpg");
                        loadingBegins();
                    }
                } catch (IOException e) {
                    сказатьТост("IO Error: " + e.getMessage());
                }
            }
        });

        getLoaderManager().initLoader(0, null, this);
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_DOWNLOADING, (state == STATE.DOWNLOADING));
        if ((state != STATE.COMPLETE) && (imageDownloader != null))
            outState.putParcelable(KEY_DOWNLODER_OBJECT, imageDownloader);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(getLoaderManager().getLoader(0).isAbandoned()) loadingRollback();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        imageDownloader = savedInstanceState.getParcelable(KEY_DOWNLODER_OBJECT);
        Log.d(LOG_TAG, state.name());
        if (imageDownloader != null)
            imageDownloader.newProgressBar(progressBar);
        if (savedInstanceState.getBoolean(KEY_DOWNLOADING))
            loadingBegins();
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        if (id == 0) imageDownloader = new ImageDownloader(getApplicationContext(), progressBar);
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
            сказатьТост("There was an error occurred while downloading. Try later");
        }
    }

    @Override
    public void onLoaderReset(Loader loader) {
        Log.d(LOG_TAG, "onLoadReset");
    }

    private void loadingBegins() {
        Log.d(LOG_TAG, "Begin");
        state = STATE.DOWNLOADING;
        progressBar.setVisibility(View.VISIBLE);
        textView.setText(R.string.state_downloading);
        button.setText(R.string.button_wait);
        button.setEnabled(false);
    }

    private void loadingRollback() {
        Log.d(LOG_TAG, "Rollback");
        state = STATE.STANDBY;
        progressBar.setVisibility(View.INVISIBLE);
        textView.setText(R.string.state_standby);
        button.setText(R.string.button_start);
        button.setEnabled(true);
    }

    private void loadComplete() {
        Log.d(LOG_TAG, "Complete!");
        state = STATE.COMPLETE;
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

    private void сказатьТост(String tost) {
        Toast.makeText(getApplicationContext(), tost, Toast.LENGTH_LONG).show();
    }
}
