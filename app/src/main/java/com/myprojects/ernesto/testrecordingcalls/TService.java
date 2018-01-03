package com.myprojects.ernesto.testrecordingcalls;

import java.io.File;
import java.util.Timer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.io.IOException;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.media.MediaRecorder;
import android.widget.Toast;
import android.os.IBinder;
import android.os.Handler;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.telephony.TelephonyManager;

/**
 * Created by ernesto on 02/01/18.
 */

public class TService extends Service {


    MediaRecorder recorder;
    File audiofile;
    Context context;
    private boolean recordstarted = false;

    String name, phonenumber;
    String audio_format;
    public String Audio_Type;
    int audioSource;
    private Handler handler;
    Timer timer;
    Boolean offHook = false, ringing = false;
    Toast toast;
    Boolean isOffHook = false;


    private static final String ACTION_IN = "android.intent.action.PHONE_STATE";
    private static final String ACTION_OUT = "android.intent.action.NEW_OUTGOING_CALL";
    private CallReceiver phoneCall;

    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onDestroy() {
        Log.d("service", "destroy");

        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // final String terminate =(String)
        // intent.getExtras().get("terminate");//
        // intent.getStringExtra("terminate");
        // Log.d("TAG", "service started");
        //
        // TelephonyManager telephony = (TelephonyManager)
        // getSystemService(Context.TELEPHONY_SERVICE); // TelephonyManager
        // // object
        // CustomPhoneStateListener customPhoneListener = new
        // CustomPhoneStateListener();
        // telephony.listen(customPhoneListener,
        // PhoneStateListener.LISTEN_CALL_STATE);
        // context = getApplicationContext();

        Log.d("StartService", "TService");

        final IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_OUT);
        filter.addAction(ACTION_IN);
        this.phoneCall = new CallReceiver();
        this.registerReceiver(this.phoneCall, filter);

        // if(terminate != null) {
        // stopSelf();
        // }
        //return START_NOT_STICKY;
        return super.onStartCommand(intent, flags, startId);
    }

    private void startRecording(){
        if(!recordstarted) {
            boolean cont = true;

            String out = new SimpleDateFormat("yyyy-MM-dd_hh-mm-ss").format(new Date());
            File sampleDir = new File(Environment.getExternalStorageDirectory(), "/TestRecordingCalls");
            if (!sampleDir.exists()) {
                sampleDir.mkdirs();
            }

            String file_name = "Record";

            try {
                audiofile = File.createTempFile(file_name, ".aac", sampleDir);
            } catch (IOException e) {
                e.printStackTrace();
            }

            String path = Environment.getExternalStorageDirectory().getAbsolutePath();

            recorder = new MediaRecorder();

            //recorder.setAudioSource(MediaRecorder.AudioSource.VOICE_CALL);
            recorder.setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION);

            recorder.setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

            //recorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
            //recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);


            recorder.setOutputFile(audiofile.getAbsolutePath());


            recorder.setOnErrorListener(errorListener);
            recorder.setOnInfoListener(infoListener);


            try {
                recorder.prepare();
            } catch (IllegalStateException e) {
                Log.e("RECORDING(OnPrepare)::", e.getMessage());
                e.printStackTrace();
                cont = false;
            } catch (IOException e) {
                Log.e("RECORDING(OnPrepare)::", e.getMessage());
                e.printStackTrace();
                cont = false;
            } catch (Exception e) {
                Log.e("RECORDING(OnPrepare)::", e.getMessage());
                e.printStackTrace();
            }

            if (cont) {
                try {
                    //recorder.prepare();
                    recorder.start();
                    recordstarted = true;
                } catch (IllegalStateException e) {
                    Log.e("RECORDING(OnStart)::", e.getMessage());
                    e.printStackTrace();
                } catch (Exception e) {
                    Log.e("RECORDING(OnStart)::", e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    private void stopRecording(){
        if(recorder != null){
            try {
                recorder.stop();
                recorder.reset();
                recorder.release();

                recorder = null;

            }catch(RuntimeException stopException) {
                Log.e("Error on stop recording", stopException.getMessage());
                stopException.getStackTrace();
            }
        }
        recordstarted = false;
    }

    private MediaRecorder.OnErrorListener errorListener = new MediaRecorder.OnErrorListener() {
        @Override
        public void onError(MediaRecorder mr, int what, int extra) {
            Toast.makeText(context,"Error: " + what + ", " + extra, Toast.LENGTH_SHORT).show();
        }
    };
    private MediaRecorder.OnInfoListener infoListener = new MediaRecorder.OnInfoListener() {
        @Override
        public void onInfo(MediaRecorder mr, int what, int extra) {
            Toast.makeText(context,"Warning: " + what + ", " + extra, Toast.LENGTH_SHORT).show();
        }
    };

    public abstract class PhonecallReceiver extends BroadcastReceiver {

        //The receiver will be recreated whenever android feels like it.  We need a static variable to remember data between instantiations

        private int lastState = TelephonyManager.CALL_STATE_IDLE;
        private Date callStartTime;
        private boolean isIncoming;
        private String savedNumber;  //because the passed incoming is only valid in ringing

        /*String stateStr;
        int stateInt;
        String inCallNumber, outCallNumber;
        //public boolean wasRinging = false;*/

        @Override
        public void onReceive(Context context, Intent intent) {
            // Start recording
            //We listen to two intents.  The new outgoing call only tells us of an outgoing call.
            //  We use it to get the number.
            if (intent.getAction().equals(ACTION_OUT)) {

                savedNumber = intent.getExtras().getString("android.intent.extra.PHONE_NUMBER");

            } else {

                Bundle bundle = intent.getExtras();

                if (bundle!= null) {

                    int stateInt = 0;
                    String stateStr = bundle.getString(TelephonyManager.EXTRA_STATE);
                    String number = bundle.getString(TelephonyManager.EXTRA_INCOMING_NUMBER);

                    if (stateStr.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                        stateInt = TelephonyManager.CALL_STATE_IDLE;
                    } else if (stateStr.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                        stateInt = TelephonyManager.CALL_STATE_OFFHOOK;
                    } else if (stateStr.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                        stateInt = TelephonyManager.CALL_STATE_RINGING;
                    }

                    onCallStateChanged(context, stateInt, number);
                }
            }
        }

        //Derived classes should override these to respond to specific events of interest
        protected abstract void onIncomingCallReceived(Context ctx, String number, Date start);

        protected abstract void onIncomingCallAnswered(Context ctx, String number, Date start);

        protected abstract void onIncomingCallEnded(Context ctx, String number, Date start, Date end);

        protected abstract void onOutgoingCallStarted(Context ctx, String number, Date start);

        protected abstract void onOutgoingCallEnded(Context ctx, String number, Date start, Date end);

        protected abstract void onMissedCall(Context ctx, String number, Date start);


        //Incoming call-  goes from IDLE to RINGING when it rings, to OFFHOOK when it's answered, to IDLE when its hung up
        //Outgoing call-  goes from IDLE to OFFHOOK when it dials out, to IDLE when hung up
        private void onCallStateChanged(Context context, int state, String number){
            if(lastState == state){
                // No changes, debounce, extras
                return;
            }
            switch (state){
                case TelephonyManager.CALL_STATE_RINGING:
                    isIncoming = true;
                    callStartTime = new Date();
                    savedNumber = number;
                    onIncomingCallReceived(context, number, callStartTime);
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    //Transition of ringing->offhook are pickups of incoming calls.  Nothing done on them
                    if (lastState != TelephonyManager.CALL_STATE_RINGING) {
                        isIncoming = false;
                        callStartTime = new Date();
                        startRecording();
                        onOutgoingCallStarted(context, savedNumber, callStartTime);
                    } else {
                        isIncoming = true;
                        callStartTime = new Date();
                        startRecording();
                        onIncomingCallAnswered(context, savedNumber, callStartTime);
                    }
                    break;
                case TelephonyManager.CALL_STATE_IDLE:
                    //Went to idle-  this is the end of a call.  What type depends on previous state(s)
                    if (lastState == TelephonyManager.CALL_STATE_RINGING) {
                        //Ring but no pickup-  a miss
                        onMissedCall(context, savedNumber, callStartTime);
                    } else if (isIncoming) {
                        stopRecording();
                        onIncomingCallEnded(context, savedNumber, callStartTime, new Date());
                    } else {
                        stopRecording();
                        onOutgoingCallEnded(context, savedNumber, callStartTime, new Date());
                    }
                    break;
            }
            lastState = state;
        }
    }

    public class CallReceiver extends PhonecallReceiver {

        @Override
        protected void onIncomingCallReceived(Context ctx, String number, Date start) {
            Log.d("onIncomingCallReceived", number + " " + start.toString());
            Toast.makeText(ctx, "IncomingCall-Received : " + number, Toast.LENGTH_LONG).show();
        }

        @Override
        protected void onIncomingCallAnswered(Context ctx, String number, Date start) {
            Log.d("onIncomingCallAnswered", number + " " + start.toString());
            Toast.makeText(ctx, "IncomingCall-Answered : " + number, Toast.LENGTH_LONG).show();
        }

        @Override
        protected void onIncomingCallEnded(Context ctx, String number, Date start, Date end) {
            Log.d("onIncomingCallEnded", number + " " + start.toString() + "\t" + end.toString());
            Toast.makeText(ctx, "IncomingCall-Ended : " + number, Toast.LENGTH_LONG).show();
        }

        @Override
        protected void onOutgoingCallStarted(Context ctx, String number, Date start) {
            Log.d("onOutgoingCallStarted", number + " " + start.toString());
            Toast.makeText(ctx, "OutgoingCall-Started : " + number, Toast.LENGTH_LONG).show();
        }

        @Override
        protected void onOutgoingCallEnded(Context ctx, String number, Date start, Date end) {
            Log.d("onOutgoingCallEnded", number + " " + start.toString() + "\t" + end.toString());
            Toast.makeText(ctx, "OutgoingCall-Ended : " + number, Toast.LENGTH_LONG).show();
        }

        @Override
        protected void onMissedCall(Context ctx, String number, Date start) {
            Log.d("onMissedCall", number + " " + start.toString());
            Toast.makeText(ctx, "Missed Call : " + number, Toast.LENGTH_LONG).show();
//        PostCallHandler postCallHandler = new PostCallHandler(number, "janskd" , "")
        }
    }
}
