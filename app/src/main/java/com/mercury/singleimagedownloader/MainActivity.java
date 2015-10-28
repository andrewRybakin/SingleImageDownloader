package com.mercury.singleimagedownloader;

import android.app.LoaderManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.net.MalformedURLException;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks {

    private static final String LOG_TAG = "MainActivity";

    private static ProgressBar progressBar;
    private static Button button;

    private TextView textView;
    ImageDownloader imageDownloader;
    File imageFile;

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
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initLoader();
            }
        });
    }

    public void initLoader() {
        progressBar.setVisibility(View.VISIBLE);
        textView.setText(R.string.state_downloading);
        button.setText(R.string.button_wait);
        button.setEnabled(false);
        getLoaderManager().initLoader(0, null, this);
        //broadcast local broadcast manager
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        try {
            imageDownloader = new ImageDownloader(getApplicationContext(), getString(R.string.image_URL));
        } catch (MalformedURLException e) {
            Toast.makeText(getApplicationContext(), "Bad URL: " + e.getMessage(), Toast.LENGTH_LONG).show();
            return null;
        }
        Log.d(LOG_TAG, "onCreateLoader");
        return imageDownloader;
    }

    @Override
    public void onLoadFinished(Loader loader, Object data) {
        Log.d(LOG_TAG, "onLoadFinished");
        //progressUpdater.interrupt();
        progressBar.setVisibility(View.INVISIBLE);
        textView.setText(R.string.state_downloaded);
        button.setText(R.string.button_show);
        button.setEnabled(true);
        imageFile = (File) data;
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(android.content.Intent.ACTION_VIEW);
                intent.setType("image/*");
                intent.setData(Uri.fromFile(imageFile));
                startActivity(intent);
            }
        });
    }

    @Override
    public void onLoaderReset(Loader loader) {
        Log.d(LOG_TAG, "onLoadReset");
    }

    public static void updateProgress(int now, int max) {
        progressBar.setProgress(now);
        progressBar.setMax(max);
    }
}
