package com.provenlogic.mingle.Activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.async.http.body.FilePart;
import com.koushikdutta.async.http.body.Part;
import com.koushikdutta.ion.Ion;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection;
import com.provenlogic.mingle.Adapters.ChatMessageAdapter;
import com.provenlogic.mingle.Applications.ApplicationSingleTon;
import com.provenlogic.mingle.Models.ChatMessage;
import com.provenlogic.mingle.Networking.Endpoints;
import com.provenlogic.mingle.R;
import com.provenlogic.mingle.Utils.AndroidSslContext;
import com.provenlogic.mingle.Utils.Const;
import com.provenlogic.mingle.Utils.HelperFunctions;
import com.provenlogic.mingle.Utils.SharedPreferencesUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class ChatActivity extends AppCompatActivity implements View.OnClickListener {

    private int START = 0, incrementRate = 20;
    private boolean isMoreDataAvailable = false, isLoadingData = false;

    private ArrayList<ChatMessage> messages;
    private ListView mListView;
    private FloatingActionButton mButtonSend;
    private EditText mEditTextMessage;
    private SwipyRefreshLayout swipeContainer;
    private TextView userName, userStatus;
    private ImageView userImage;

    private String onlineStatus;
    private boolean isTyping = false;
    private String lastMessage_Id = "";


    private ChatMessageAdapter mAdapter;

    private String session_token, userId, receiverId, receiverImageUrl, receiverName, contactId;

    private Socket mSocket;


    private Emitter.Listener onNewMessage = new Emitter.Listener() {
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
                        addToReceiveMessage(message, -1);
                    }
                }
                //Image message
                else if(msgType == 2){
                    final String url = data.optString("meta");
                    Log.d("IMAGE MESSAGE", url + "");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            addToReceivedImage(url, -1);
                        }
                    });
                }
            }catch(Exception e){
                Log.d("SPI", e.toString());
            }
        }

    };

    private Emitter.Listener onConnected = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            try{
                JSONObject data = (JSONObject) args[0];
                String message = data.optString("socket_id");
                Log.d("API", "Inside onConnected");
                if (!message.isEmpty()) {
                    // addToReceiveMessage(message, -1);
                    mapSocketIdToUser(message);
                }
            }catch(Exception e){

            }
        }

    };
    private Emitter.Listener onUserOnline = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            try{
                JSONObject data = (JSONObject) args[0];
                onlineStatus = "online";
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        userStatus.setText(onlineStatus);
                    }
                });
            }catch(Exception e){

            }
        }

    };
    private Emitter.Listener onUserOffline = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            try{
                JSONObject data = (JSONObject) args[0];
                onlineStatus = "online";
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        userStatus.setText(onlineStatus);
                    }
                });
            }catch(Exception e){

            }
        }

    };
    private Emitter.Listener onTypingStop = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            try{
                JSONObject data = (JSONObject) args[0];
                if (data.optString("from_user").equals(receiverId)) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            userStatus.setText(onlineStatus);
                        }
                    });

                }
            }catch(Exception e){

            }
        }

    };
    private Emitter.Listener onNewMessageSent = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            try{
                JSONObject data = (JSONObject) args[0];
                Log.d("MSG SEND", "MESG SEND");
            }catch(Exception e){

            }
        }

    };
    private Emitter.Listener onTyping = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];
            if (data.optString("from_user").equals(receiverId)) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        userStatus.setText("typing..");
                    }
                });

            }
        }

    };

    private Emitter.Listener onNotification = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            try{
                JSONObject obj = (JSONObject) args[0];
                Log.d("NOTI DATA", obj.toString());
                JSONObject notification = obj.getJSONObject("notification");
                if(notification.getString("notification_hook_type").equalsIgnoreCase("central")){
                    if(notification.getString("type").equalsIgnoreCase("user_gift_sent")){
                        if(notification.getString("to_user").equalsIgnoreCase(userId)){
                            GetGift();
                        }
                    }
                }
            }catch(Exception e){

            }
        }

    };



    //Request code for the gift.
    private int GIFT_REQUEST = 101;

    //Request code for image picking.
    private int PICK_IMAGE = 105;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        InitSockets();

        mListView = (ListView) findViewById(R.id.listView);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        LayoutInflater inflator = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View custom_view = inflator.inflate(R.layout.custom_actionbar_layout, null);
        ActionBar.LayoutParams layout = new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT);
        getSupportActionBar().setCustomView(custom_view, layout);

        userImage = (ImageView) custom_view.findViewById(R.id.user_image);
        userName = (TextView) custom_view.findViewById(R.id.user_name);
        userStatus = (TextView) custom_view.findViewById(R.id.user_status);

        userId = (String) SharedPreferencesUtils.getParam(this, SharedPreferencesUtils.USER_ID, "");
        //session_token = (String) SharedPreferencesUtils.getParam(this, SharedPreferencesUtils.SESSION_TOKEN, "");

        receiverId = getIntent().getExtras().getString("receiverId");
        receiverImageUrl = getIntent().getExtras().getString("receiverImageUrl");
        receiverName = getIntent().getExtras().getString("receiverName");
        contactId = getIntent().getExtras().getString("contact_id");

        userName.setText(receiverName);
        Glide.with(this).load(receiverImageUrl).into(userImage);

        swipeContainer = (SwipyRefreshLayout) findViewById(R.id.swipeContainer);
        session_token = (String) SharedPreferencesUtils.getParam(this, SharedPreferencesUtils.SESSION_TOKEN, "");

        mButtonSend = (FloatingActionButton) findViewById(R.id.btn_send);
        mEditTextMessage = (EditText) findViewById(R.id.et_message);
        mAdapter = new ChatMessageAdapter(this, new ArrayList<ChatMessage>());
        mListView.setAdapter(mAdapter);

        mSocket.on("new_message_received", onNewMessage)
                .on("connected", onConnected)
                .on("user_online", onUserOnline)
                .on("user_offline", onUserOffline)
                .on("typing", onTyping)
                .on("typing_stop", onTypingStop)
                .on("new_message_sent", onNewMessageSent)
                .on("notifications", onNotification);



        mButtonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = mEditTextMessage.getText().toString();
                if (TextUtils.isEmpty(message)) {
                    return;
                }
                sendTextMessage(message);
                mEditTextMessage.setText("");
            }
        });

        swipeContainer.setOnRefreshListener(new SwipyRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh(SwipyRefreshLayoutDirection direction) {
                // getUnreadChatHistory();
                START = mListView.getAdapter().getCount();
                getChatHistory(lastMessage_Id);
            }
        });
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        mEditTextMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!isTyping)
                    emitTyping();
                isTyping = true;

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (isTyping)
                    emitStopTyping();
                isTyping = false;
            }
        });


        getChatHistory("");
    }

    private void InitSockets(){
        {
            try {
                IO.Options opts = new IO.Options();
                opts.forceNew = true;
                opts.reconnection = true;
                opts.secure = true;
                opts.sslContext = AndroidSslContext.GetSslContext(ChatActivity.this, "cert.crt");
                mSocket = IO.socket("https://staging.datingframework.com:14836", opts);
                mSocket.connect();
                //mSocket = IO.socket("https://staging.datingframework.com:14836");
            } catch (Exception e) {
                Log.d("API", "SOCKET ERROR" + e.toString());
            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.gift_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
        }

        //Send Gift
        if (id == R.id.action_send_gift){
            startActivityForResult(new Intent(ChatActivity.this, GiftActivity.class), GIFT_REQUEST);
        }

        //Send Image
        if(id == R.id.action_send_image){
            getImageFromGallery();
        }
        return super.onOptionsItemSelected(item);

    }


    /**
     * Fires an Intent to pick image from the gallery
     */
    private void getImageFromGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int result, Intent data){
        super.onActivityResult(requestCode, result, data);
        if(requestCode == GIFT_REQUEST){
            if(result == RESULT_OK){
                //User selected to send some gift to the other person.
                String gift_id = data.getStringExtra("gift_id");
                String gift_name = data.getStringExtra("gift_name");
                String gift_url = data.getStringExtra("gift_url");
                String gift_icon_name = data.getStringExtra("gift_icon_name");
                int price = data.getIntExtra("gift_price", 200);
                sendGift(gift_id, gift_name, gift_url, gift_icon_name, price);
            }
        }

        if(requestCode == PICK_IMAGE){
            if(result == RESULT_OK){
                //File file = new File(getPath(data.getData()));
                try{
                    InputStream selectedImage = getContentResolver().openInputStream(data.getData());
                    Bitmap bitmap = BitmapFactory.decodeStream(selectedImage);
                    File mainFile = persistImage(bitmap, String.valueOf(System.currentTimeMillis()));
                    ChatMessage chatMessage = new ChatMessage("", true, true, false);
                    chatMessage.setImageBitmao(bitmap);
                    mAdapter.add(chatMessage);
                    uploadImage(mainFile);
                }catch(Exception e){

                }
            }
        }
    }

    /**
     *
     * @param bitmap
     * @param name
     * @return
     */
    private File persistImage(Bitmap bitmap, String name) {
        File filesDir = getApplicationContext().getFilesDir();
        File imageFile = new File(filesDir, name + ".jpg");

        OutputStream os;
        try {
            os = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
            os.flush();
            os.close();
        } catch (Exception e) {
            Log.e(getClass().getSimpleName(), "Error writing bitmap", e);
        }

        return imageFile;
    }

    /**
     *
     * @param file
     */
    private void uploadImage(File file){
        String id = (String) SharedPreferencesUtils.getParam(this, SharedPreferencesUtils.USER_ID, "");
        String token = (String) SharedPreferencesUtils.getParam(this, SharedPreferencesUtils.SESSION_TOKEN, "");

        List<Part> files = new ArrayList();
        files.add(new FilePart("image", file));
        Ion.with(this)
                .load(Endpoints.uploadChatImage)
                .setMultipartParameter(Const.Params.ID, id)
                .setMultipartParameter(Const.Params.TOKEN, token)
                .addMultipartParts(files)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        if(result == null){
                            Log.e("RESULT IS NULL", e.toString());
                            return;
                        }
                        Log.d("IMAGE RESULT",  result.toString());
                        try{
                            JSONObject obj = new JSONObject(result.toString());

                            /**
                             * Logging out user if authentication fails, if user has logged in his/her account
                             * on some other device as well.
                             */
                            if(HelperFunctions.IsUserAuthenticated(obj, ChatActivity.this)){
                                return;
                            }

                            if(obj.getString("status").equalsIgnoreCase("success")){
                                String imageUrl = obj.getString("image_url");
                                Log.d("IMAGE URL", imageUrl);
                                sendImageMessage(imageUrl);
                                //sendTextMessage(imageUrl);
                            }
                        }catch(Exception e2){

                        }
                    }
                });
    }

    /**
     *
     * @param imageUrl
     */
    private void sendImageMessage(String imageUrl){
        try {
            JSONObject object = new JSONObject();
            object.put("from_user", userId);
            object.put("to_user", receiverId);
            object.put("message_text", imageUrl);
            object.put("contact_id", contactId);
            object.put("message_type", 2);
            mSocket.emit("new_message", object);

        } catch (Exception e) {
            Log.d("SPI", e.toString());
            e.printStackTrace();
        }
    }

    private void emitTyping() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("from_user", userId);
            jsonObject.put("to_user", receiverId);
            mSocket.emit("typing", jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void emitStopTyping() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("from_user", userId);
            jsonObject.put("to_user", receiverId);
            mSocket.emit("typing_stop", jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void mapSocketIdToUser(String socket_id) {
        JsonObject json = new JsonObject();
        json.addProperty(Const.Params.ID, userId);
        json.addProperty(Const.Params.TOKEN, session_token);
        json.addProperty("socket_id", socket_id);
        Log.d("API", "Map socket");
        Ion.with(this)
                .load(Endpoints.mapUserSocket)
                .setJsonObjectBody(json)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        // do stuff with the result or error
                        if (result == null) {
                            Log.d("API", "Result is null");
                            return;
                        }
                        try {
                            JSONObject jsonObject = new JSONObject(result.toString());

                            /**
                             * Logging out user if authentication fails, if user has logged in his/her account
                             * on some other device as well.
                             */
                            if(HelperFunctions.IsUserAuthenticated(jsonObject, ChatActivity.this)){
                                return;
                            }
                            Log.d("API", jsonObject.toString());
                            if (jsonObject.optString(Const.Params.STATUS).equals("success")) {
                                String base_image_chat_url = jsonObject.optString("base_chat_images_url");
                                mSocket.emit("user_socket_mapped", new JsonObject());
                            }

                        } catch (JSONException e1) {
                            e1.printStackTrace();
                        }
                    }
                });
    }

    private void getChatHistory(String id) {
        JsonObject json = new JsonObject();
        json.addProperty(Const.Params.ID, userId);
        json.addProperty(Const.Params.TOKEN, session_token);
        json.addProperty("other_user_id", receiverId);

        if (!id.equals("")) {
            json.addProperty("last_message_id", id);
        }


        Ion.with(this)
                .load(Endpoints.messageHistoryUrl)
                .setJsonObjectBody(json)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        // do stuff with the result or error
                        Log.d("Chat messages Response", result.toString());
                        swipeContainer.setRefreshing(false);
                        if (result == null) {
                            return;
                        }
                        try {
                            JSONObject jsonObject = new JSONObject(result.toString());

                            /**
                             * Logging out user if authentication fails, if user has logged in his/her account
                             * on some other device as well.
                             */
                            if(HelperFunctions.IsUserAuthenticated(jsonObject, ChatActivity.this)){
                                return;
                            }

                            if (jsonObject.optString(Const.Params.STATUS).equals("success")) {
                                JSONArray messageArray = jsonObject.getJSONArray("messages");

                                for (int i = messageArray.length() - 1; i >= 0; i--) {
                                    JSONObject object = messageArray.optJSONObject(i);
                                    //Log.d("SINGLE MESSAGES", object.toString());
                                    lastMessage_Id = object.optString("id");

                                    //text message type
                                    if (object.optString("type").equalsIgnoreCase("0")) {
                                        if (object.optString("from_user").equalsIgnoreCase(userId)) {
                                            addToSendMessage(object.optString("text"), 0);
                                        } else {
                                            addToReceiveMessage(object.optString("text"), 0);
                                        }
                                    }
                                    //Gift message type
                                    else if(object.optString("type").equalsIgnoreCase("4")){
                                        if(object.optString("from_user").equalsIgnoreCase(userId)){
                                            //my sent gift
                                            Log.d("GIFT SENT", object.getString("meta"));
                                            addToSentGifts("", "", object.getString("meta"), 0);
                                        }else{
                                            //my received gifts
                                            Log.d("GIFT RECEIVED ", object.getString("meta"));
                                            addToReceivedGifts("", "", object.getString("meta"), 0);
                                        }
                                    }
                                    //image
                                    else{
                                        Log.d("MSG", object.toString());
                                        if(object.optString("from_user").equalsIgnoreCase(userId)){
                                            //my sent image
                                            addToSentImage(object.optString("meta"), 0);
                                        }else{
                                            //my received image
                                            addToReceivedImage(object.optString("meta"), 0);
                                        }
                                    }

                                }
                            }

                        } catch (JSONException e1) {
                            e1.printStackTrace();
                        }
                    }
                });
    }

    /**
     * This function resolves the received gift from other user.
     */
    private void GetGift(){
        JsonObject json = new JsonObject();
        json.addProperty(Const.Params.ID, userId);
        json.addProperty(Const.Params.TOKEN, session_token);
        json.addProperty("other_user_id", receiverId);

        Ion.with(this)
                .load(Endpoints.messageHistoryUrl)
                .setJsonObjectBody(json)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        // do stuff with the result or error
                        Log.d("Chat messages Response", result.toString());
                        swipeContainer.setRefreshing(false);
                        if (result == null) {
                            return;
                        }
                        try {
                            JSONObject jsonObject = new JSONObject(result.toString());

                            /**
                             * Logging out user if authentication fails, if user has logged in his/her account
                             * on some other device as well.
                             */
                            if(HelperFunctions.IsUserAuthenticated(jsonObject, ChatActivity.this)){
                                return;
                            }

                            if (jsonObject.optString(Const.Params.STATUS).equals("success")) {
                                JSONArray messageArray = jsonObject.getJSONArray("messages");

                                //for (int i = messageArray.length() - 1; i >= 0; i--)
                                {
                                    JSONObject object = messageArray.optJSONObject(messageArray.length() - 1);
                                    //Log.d("SINGLE MESSAGES", object.toString());
                                    //lastMessage_Id = object.optString("id");

                                    if(object.optString("type").equalsIgnoreCase("4")) {
                                        if (object.optString("from_user").equalsIgnoreCase(userId)) {
                                            //my sent gift
                                            Log.d("GIFT SENT", object.getString("meta"));
                                            addToSentGifts("", "", object.getString("meta"), 10);
                                        } else {
                                            //my received gifts
                                            Log.d("GIFT RECEIVED ", object.getString("meta"));
                                            addToReceivedGifts("", "", object.getString("meta"), 10);
                                        }
                                    }
                                }
                            }
                        } catch (JSONException e1) {

                        }

                    }
                });

    }



    /**
     *
     * @param message
     */
    private void sendTextMessage(String message) {
        ChatMessage chatMessage = new ChatMessage(message, true, false, false);
        mAdapter.add(chatMessage);
        try {
            JSONObject object = new JSONObject();
            object.put("from_user", userId);
            object.put("to_user", receiverId);
            object.put("message_text", message);
            object.put("contact_id", contactId);
            object.put("message_type", 0);
            mSocket.emit("new_message", object);

        } catch (Exception e) {
            Log.d("SPI", e.toString());
            e.printStackTrace();
        }
    }

    /**
     *
     * @param imageUrl
     * @param pos
     */
    private void addToSentImage(String imageUrl, int pos){
        ChatMessage chatMessage = new ChatMessage("", true, true, false);
        chatMessage.setImageUrl(imageUrl);
        if (pos >= 0)
            mAdapter.insert(chatMessage, pos);
        else mAdapter.add(chatMessage);
    }

    /**
     *
     * @param imageUrl
     * @param pos
     */
    private void addToReceivedImage(String imageUrl, int pos){
        ChatMessage chatMessage = new ChatMessage("", false, true, false);
        chatMessage.setImageUrl(imageUrl);
        if (pos >= 0)
            mAdapter.insert(chatMessage, pos);
        else mAdapter.add(chatMessage);
    }



    /**
     *
     * @param message
     * @param pos
     */
    private void addToSendMessage(String message, int pos) {
        ChatMessage chatMessage = new ChatMessage(message, true, false, false);
        if (pos >= 0)
            mAdapter.insert(chatMessage, pos);
        else mAdapter.add(chatMessage);
    }

    /**
     *
     * @param message
     * @param pos
     */
    private void addToReceiveMessage(String message, final int pos) {
        final ChatMessage chatMessage = new ChatMessage(message, false, false, false);
        runOnUiThread(new Runnable() {
            public void run() {
                if (pos >= 0)
                    mAdapter.insert(chatMessage, pos);
                else mAdapter.add(chatMessage);
            }
        });

    }

    /**
     * This function sends the message to the other user
     * @param id of the gift to be sent
     * @param name of the gift to be sent
     * @param gift_url of the gift to be sent
     * @param gift_icon_name of the gift to be sent
     * @param price of the gift to sent
     */
    private void sendGift(String id, String name, String gift_url, String gift_icon_name, int price){
        try {
            JSONObject object = new JSONObject();
            object.put("from_user", userId);
            object.put("to_user", receiverId);
            object.put("gift_name", name);
            object.put("gift_url", gift_url);
            object.put("gift_id", id);
            object.put("gift_icon_name", gift_icon_name);
            object.put("price", price);
            object.put("contact_id", contactId);
            object.put("message_type", 4);
            //mSocket.emit("new_message", object);
            addToSentGifts(id, name, gift_url, 10);
            addGiftToReceiverProfile(id, "");
        } catch (Exception e) {
            Log.d("SPI", e.toString());
            e.printStackTrace();
        }
    }

    /**
     * This function adds this gift to the receiver's profile to view.
     * @param gift_id to add
     * @param msg to add(optional)
     */
    private void addGiftToReceiverProfile(String gift_id, String msg){
        String id = (String) SharedPreferencesUtils.getParam(this, SharedPreferencesUtils.USER_ID, "");
        String token = (String) SharedPreferencesUtils.getParam(this, SharedPreferencesUtils.SESSION_TOKEN, "");

        JsonObject json = new JsonObject();
        json.addProperty(Const.Params.ID, id);
        json.addProperty(Const.Params.TOKEN, token);
        json.addProperty("gift_id", gift_id);
        json.addProperty("gift_receiver_id", receiverId);
        Ion.with(ChatActivity.this)
                .load(Endpoints.sendGift)
                .setJsonObjectBody(json)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        if(result != null){
                            Log.d("GIFT", result.toString());
                            try{
                                JSONObject res = new JSONObject(result.toString());
                                boolean success = res.getBoolean("success");
                                if(success){
                                    int balance = res.getInt("user_credit_balance");
                                    ApplicationSingleTon.Credits = balance;
                                }
                            }catch(JSONException e1){

                            }
                        }
                    }
                });
    }

    /**
     *
     * @param id
     * @param name
     * @param gift_url
     */
    private void addToSentGifts(String id, String name, String gift_url, int pos){
        ChatMessage msg = new ChatMessage("", true, false, true);
        msg.setGiftUrl(gift_url);
        msg.setGiftId(id);
        if(pos == 0){
            mAdapter.insert(msg, 0);
        }else{
            mAdapter.add(msg);
        }
    }

    /**
     *
     * @param gift_id
     * @param gift_name
     * @param gift_url
     */
    private void addToReceivedGifts(String gift_id, String gift_name, String gift_url, int pos){
        ChatMessage msg = new ChatMessage("", false, false, true);
        msg.setGiftUrl(gift_url);
        msg.setGiftId(gift_id);
        if(pos == 0){
            Log.d("GIFT INSERTED", gift_url);
            mAdapter.insert(msg, 0);
        }else{
            mAdapter.add(msg);
        }

    }

    private void mimicOtherMessage(String message) {
        ChatMessage chatMessage = new ChatMessage(message, false, false, false);
        mAdapter.add(chatMessage);
    }




    @Override
    public void onClick(View view) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSocket.disconnect();
        mSocket.off("new_message_received", onNewMessage)
                .off("connected", onConnected)
                .off("user_online", onUserOnline)
                .off("user_offline", onUserOffline)
                .off("typing", onTyping)
                .off("typing_stop", onTypingStop)
                .off("new_message_sent", onNewMessageSent)
                .off("notifications", onNotification);
    }
}
