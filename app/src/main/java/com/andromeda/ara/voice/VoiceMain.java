/*
 * Copyright (c) 2020. Fulton Browne
 *  This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.andromeda.ara.voice;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.textservice.*;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.andromeda.ara.R;
import com.andromeda.ara.search.Search;
import com.andromeda.ara.util.Adapter;
import com.andromeda.ara.util.RssFeedModel;
import com.andromeda.ara.util.SpellChecker;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.andromeda.ara.constants.ConstantUtils.*;
import static com.andromeda.ara.util.VoiceMainUtils.*;

public class VoiceMain extends AppCompatActivity {
    private FileOutputStream os = null;
    private Thread recordingThread;
    boolean isRecording;
    ImageView imageView;
    Boolean blankRunning = false;
    public Context ctx = this;

    private int bufferSizeInBytes = AudioRecord.getMinBufferSize(SAMPLE_RATE_HZ, CHANNEL_CONFIG, AUDIO_FORMAT);

    private byte[] Data = new byte[bufferSizeInBytes];
    private ByteArrayOutputStream byteIS = new ByteArrayOutputStream();
    RecyclerView recyclerView;

    private AudioRecord audioRecorder = new AudioRecord(AUDIO_SOURCE,
            SAMPLE_RATE_HZ,
            CHANNEL_CONFIG,
            AUDIO_FORMAT,
            bufferSizeInBytes);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        System.out.println(bufferSizeInBytes);
        copyAssets();
        setContentView(R.layout.activity_voice_main);
        final TextServicesManager tsm = (TextServicesManager) getSystemService(Context.TEXT_SERVICES_MANAGER_SERVICE);

        recyclerView = findViewById(R.id.listVoice);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        Adapter adapter = new Adapter(Collections.singletonList(new RssFeedModel("hello", "how can I help", "", "", "", true)), this);
        recyclerView.setAdapter(adapter);
        requestMicrophonePermission();


        super.onCreate(savedInstanceState);

        startRecording();
        runTransition();

    }

    void runTransition() {

        runOnUiThread(() -> {
            imageView = findViewById(R.id.imageView);
            imageView.setVisibility(View.VISIBLE);

            AnimationDrawable transition = (AnimationDrawable) imageView.getBackground();
            transition.setEnterFadeDuration(5000);

            // setting exit fade animation duration to 2 seconds
            transition.setExitFadeDuration(2000);
            transition.run();

        });
    }


    public void back(View view) {
        if (isRecording) {
                stopRecording();
                FloatingActionButton fab2 = findViewById(R.id.floatingActionButton2);
                fab2.setVisibility(View.VISIBLE);

        } else onBackPressed();
    }

    private void requestMicrophonePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(
                    new String[]{android.Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO);
        }
    }
    private synchronized void startRecording() {
        recordingThread = new Thread(() -> {
        final MediaPlayer mp = new MediaPlayer();
        mp.reset();
        AssetFileDescriptor afd;
        try{
            afd = getAssets().openFd("start.mp3");
            mp.setDataSource(afd.getFileDescriptor(),afd.getStartOffset(),afd.getLength());
            mp.prepare();
            mp.start();
            while(mp.isPlaying()) System.out.println("playing");
        }
        catch (IllegalStateException | IOException e) {
            e.printStackTrace();
        }

        audioRecorder.startRecording();
        isRecording = true;

            try {
                new File(getCacheDir(), "record.pcm");
                os = new FileOutputStream(getCacheDir() + "/record.pcm");

                while (isRecording) {
                    audioRecorder.read(Data, 0, getRawDataLength(Data));
                    System.out.println(Data[0]);
                    byteIS.write(Data);
                    if (Data[0] == 0) {
                        System.out.println("blank");
                        blankRunning = false;
                        new Timer().schedule(new TimerTask() {
                            @Override
                            public void run() {
                                runOnUiThread(() -> {
                                    if (!blankRunning) {
                                        stopRecording();
                                        FloatingActionButton fab2 = findViewById(R.id.floatingActionButton2);
                                        fab2.setVisibility(View.VISIBLE);
                                    }
                                });

                            }
                        }, 4000);
                    } else blankRunning = true;
                    try {
                        os.write(Data, 0, bufferSizeInBytes);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                stopAnimation();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        recordingThread.start();
    }

    private void stopRecording() {
        final MediaPlayer mp = new MediaPlayer();
        mp.reset();
        AssetFileDescriptor afd;
        try{
            afd = getAssets().openFd("end.mp3");
            mp.setDataSource(afd.getFileDescriptor(),afd.getStartOffset(),afd.getLength());
            mp.prepare();
            mp.start();
        }
        catch (IllegalStateException | IOException e) {
            e.printStackTrace();
        }
        if (null != audioRecorder) {
            isRecording = false;
            audioRecorder.stop();
            audioRecorder.release();
            audioRecorder = null;
            recordingThread = null;
            recognize();
        }
    }

    private void recognize() {
        final String[] phrase = new String[1];
        Thread recognize = new Thread(() -> {
            try {
                phrase[0] = new DeepSpeech().voiceV2(byteIS.toByteArray(), this);
                phrase[0] = new SpellChecker().check(phrase[0]);
                byteIS.reset();
            } catch (Exception e) {
                e.printStackTrace();
            }
            ArrayList<RssFeedModel> rssFeedModels = new ArrayList<>(new Search().main(phrase[0], getApplicationContext(), VoiceMain.this));

            runOnUiThread(() -> recyclerView.setAdapter(new Adapter(rssFeedModels, this)));
            try {
                new TTS().start(getApplicationContext(), rssFeedModels.get(0).out);
            } catch (Exception ignored) {

            }
        });
        recognize.setPriority(Thread.MAX_PRIORITY);
        recognize.start();
        System.out.println("result =" + phrase[0]);
    }

    private int getRawDataLength(byte[] rawData) {
        return rawData.length;
    }
    private void copyAssets() {
        AssetManager assetManager = getAssets();
        String[] files = null;
        try {
            files = assetManager.list("");
        } catch (IOException e) {
            Log.e("tag", "Failed to get asset file list.", e);
        }
        if (files != null) for (String filename : files) {
            InputStream in = null;
            OutputStream out = null;
            try {
                in = assetManager.open(filename);
                File outFile = new File(getCacheDir(), filename);
                out = new FileOutputStream(outFile);
                copyFile(in, out);
                System.out.println(filename);
            } catch (IOException e) {
                Log.e("tag", "Failed to copy asset file: " + filename, e);
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        // NOOP
                    }
                }
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e) {
                        // NOOP
                    }
                }
            }
        }
    }
    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }
    private void stopAnimation() {
        runOnUiThread(() -> imageView.setVisibility(View.INVISIBLE));
    }

    public void exit(View view) {
        onBackPressed();
    }

    public void record(View view) {
        audioRecorder = new AudioRecord(AUDIO_SOURCE,
                SAMPLE_RATE_HZ,
                CHANNEL_CONFIG,
                AUDIO_FORMAT,
                bufferSizeInBytes);
        runTransition();
        startRecording();
    }
}
