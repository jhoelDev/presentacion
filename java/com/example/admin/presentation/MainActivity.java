package com.example.admin.presentation;

import java.io.File;
import java.io.IOException;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;

import android.app.Activity;

import android.content.Intent;
import android.media.MediaPlayer;

import android.media.MediaPlayer.OnCompletionListener;

import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;

import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.github.nkzawa.emitter.Emitter;

import org.json.JSONException;
import org.json.JSONObject;

public  class MainActivity extends Activity implements OnCompletionListener,SurfaceHolder.Callback {

    private Socket mSocket;
    {
        try {
            mSocket = IO.socket("http://192.168.137.1:3001");
        } catch (URISyntaxException e) {}
    }

    private SurfaceView surface;
    private MediaPlayer mediaPlayer;

    private ArrayList<String> videoList = new ArrayList<String>();

    private SurfaceHolder holder;

    private int currentVideo = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);


        String path = Environment.getExternalStorageDirectory().toString()+"/Anovo";
        Log.d("Files", "Path: " + path);
        File directory = new File(path);
        File[] files = directory.listFiles();
        Arrays.sort(files);
        for (int i = 0; i < files.length; i++)
        {
            videoList.add(path+"/"+files[i].getName());
        }

        mSocket.on("mensaje", onNewMessage);
        mSocket.connect();

         surface = (SurfaceView) findViewById(R.id.surface);

        holder = surface.getHolder();
        holder.addCallback(MainActivity.this);

        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        currentVideo = 0;
    }

    private Emitter.Listener onNewMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String username;
                    String message;
                    try {
                        Log.i("nombre", data.getString("nombre"));
                        Log.i("empresa", data.getString("empresa"));

                        Intent intent = new Intent(getApplicationContext(), saludoActivity.class);
                        //intent.addCategory(Intent.CATEGORY_HOME);
                        //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra("nombre", data.getString("nombre"));
                        intent.putExtra("empresa", data.getString("empresa"));
                        startActivity(intent);
                        //finish();

                    } catch (JSONException e) {
                        return;
                    }

                }
            });
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        mediaPlayer.stop();
        mediaPlayer.release();
        finish();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        playVideo(videoList.get(0));

    }

    private void playVideo(String videoPath) {

        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setOnCompletionListener(this);
            mediaPlayer.setDisplay(holder);
            mediaPlayer.setDataSource(videoPath);
            mediaPlayer.prepare();
            mediaPlayer.start();

        } catch (IllegalArgumentException e) {
            Log.d("MEDIA_PLAYER", e.getMessage());
        } catch (IllegalStateException e) {
            Log.d("MEDIA_PLAYER", e.getMessage());
        } catch (IOException e) {
            Log.d("MEDIA_PLAYER", e.getMessage());
        }

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d("media player", "play next video");
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        Log.d("player", "playback complete");
        currentVideo++;
        if (currentVideo > videoList.size() - 1) {
            currentVideo = 0;
        }
        mediaPlayer.release();
        playVideo(videoList.get(currentVideo));
    }

    @Override
    public void onBackPressed() {
            /*Intent a = new Intent(Intent.ACTION_MAIN);
            a.addCategory(Intent.CATEGORY_HOME);
            a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(a);*/
        int pid = android.os.Process.myPid();
        android.os.Process.killProcess(pid);
    }
}
