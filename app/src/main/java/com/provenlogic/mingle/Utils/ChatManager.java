package com.provenlogic.mingle.Utils;

import android.app.Activity;
import android.util.Log;

import org.json.JSONObject;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

/**
 * Created by Anurag on 12/4/17.
 */

public class ChatManager {
    private static Socket socket;
    private static Activity _Activity;

    /**
     *
     */
    private static Emitter.Listener onNewMessage = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.d("API", "On New Message");
            try{

                JSONObject data = (JSONObject) args[0];
                int msgType = data.optInt("type");

                //Text message
                if (msgType == 0) {
                    String message = data.optString("text");
                    if (!message.isEmpty()) {
                        //addToReceiveMessage(message, -1);
                    }
                }else if(msgType == 1){
                    //Image message

                }else{
                    //gift message

                }
            }catch(Exception e){
                Log.d("SPI", e.toString());
            }
        }

    };

    /**
     *
     */
    private static Emitter.Listener onConnected = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];
            String message = data.optString("socket_id");
            Log.d("Chat Manager", "Inside onConnected");
            if (!message.isEmpty()) {
                // addToReceiveMessage(message, -1);
                //mapSocketIdToUser(message);
            }
        }

    };

    /**
     *
     */
    private static Emitter.Listener onUserOnline = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];
            //onlineStatus = "online";

        }

    };

    /**
     *
     */
    private static Emitter.Listener onUserOffline = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];

        }

    };

    /**
     *
     */
    private static Emitter.Listener onTypingStop = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];

        }

    };

    /**
     *
     */
    private static Emitter.Listener onNewMessageSent = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];
        }

    };

    /**
     *
     */
    private static Emitter.Listener onTyping = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];

        }

    };

    /**
     *
     * @param actvity
     */
    public static void Init(Activity actvity){
        if(socket == null){
            _Activity = actvity;
            _Activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try{
                        socket = IO.socket("https://staging.datingframework.com:14836");
                        socket.on("new_message_received", onNewMessage)
                                .on("connected", onConnected)
                                .on("user_online", onUserOnline)
                                .on("user_offline", onUserOffline)
                                .on("typing", onTyping)
                                .on("typing_stop", onTypingStop)
                                .on("new_message_sent", onNewMessageSent);
                        socket.connect();
                    }catch(URISyntaxException e){
                        Log.d("ChatManager", e.toString());
                    }
                }
            });
        }
    }

    /**
     *
     */
    public static void Destroy(){
        socket.disconnect();
        socket.off("new_message_received", onNewMessage)
                .off("connected", onConnected)
                .off("user_online", onUserOnline)
                .off("user_offline", onUserOffline)
                .off("typing", onTyping)
                .off("typing_stop", onTypingStop)
                .off("new_message_sent", onNewMessageSent);
    }

}
