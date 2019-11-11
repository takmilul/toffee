package com.banglalink.toffee.ui.player;

import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.banglalink.toffee.data.storage.Preference;
import com.banglalink.toffee.listeners.OnPlayerControllerChangedListener;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.SessionManagerListener;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by shantanu on 5/5/17.
 */

public class PlayerActivity extends AppCompatActivity implements OnPlayerControllerChangedListener {
    protected PlayerFragment2 mediaPlayer;
    protected Handler handler;
    private CastContext mCastContext;
    protected CastSession mCastSession;
    private AudioManager audioManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_tmp);
//        mediaPlayer = (PlayerFragment2) getSupportFragmentManager().findFragmentById(R.id.media_player);
        mCastContext = CastContext.getSharedInstance(this);
//        mCastContext.registerLifecycleCallbacksBeforeIceCreamSandwich(this, savedInstanceState);
        mCastSession = mCastContext.getSessionManager().getCurrentCastSession();
        handler = new Handler();
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);

        //test
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                Channel channel = new Channel("TEST", "https://streamer-4.nexhls.com/vod/mobile/9153/5/e012ea2c86255050cbded8ded6dfccf0/auto");
//                mediaPlayer.loadChannel(channel);
//            }
//        },2000);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(mCastSession == null || !mCastSession.isConnected()) {
            if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    public CastSession getCastSession(){
        return this.mCastSession;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCastContext.getSessionManager().addSessionManagerListener(
                mSessionManagerListener, CastSession.class);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mCastContext.getSessionManager().removeSessionManagerListener(
                mSessionManagerListener, CastSession.class);
    }

    private SessionManagerListener<CastSession> mSessionManagerListener = new SessionManagerListener<CastSession>() {

        @Override
        public void onSessionEnded(CastSession session, int error) {
            mediaPlayer.onCastSessionEnd(session);
        }

        @Override
        public void onSessionResumed(CastSession session, boolean wasSuspended) {
            mCastSession = session;
            mediaPlayer.setCastSession(session);
//            mediaPlayer.onCastSessionStarted(session);
        }

        @Override
        public void onSessionResumeFailed(CastSession session, int error) {
            mediaPlayer.onCastSessionEnd(session);
        }

        @Override
        public void onSessionStarted(CastSession session, String sessionId) {
            mCastSession = session;
            mediaPlayer.onCastSessionStarted(session);

            JSONObject jobj = new JSONObject();
            try {
                jobj.put("user", Preference.Companion.getInstance().getCustomerId());
                jobj.put("pass", Preference.Companion.getInstance().getPassword());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            session.sendMessage("urn:x-cast:com.viewersent.cast.sampleplayer", jobj.toString());
        }

        @Override
        public void onSessionStartFailed(CastSession session, int error) {
            mediaPlayer.onCastSessionEnd(session);
        }

        @Override
        public void onSessionStarting(CastSession session) {
            mediaPlayer.onCastSessionStarting(session);
        }

        @Override
        public void onSessionEnding(CastSession session) {
        }

        @Override
        public void onSessionResuming(CastSession session, String sessionId) {
        }

        @Override
        public void onSessionSuspended(CastSession session, int reason) {
            mediaPlayer.onCastSessionEnd(session);
        }

    };

    @Override
    public boolean onPlayButtonPressed(int currentState) {
        return false;
    }

    @Override
    public boolean onFullScreenButtonPressed(boolean currentState) {
        return false;// true will override fullscreen button behaviour
    }

    @Override
    public boolean onDrawerButtonPressed() {
        return false;
    }

    @Override
    public boolean onMinimizeButtonPressed() {
        return false;
    }

    @Override
    public boolean onOptionMenuPressed() {
        return false;
    }

    @Override
    public boolean onSeekPosition(int position) {
        return false;
    }
}
