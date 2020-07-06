package restFramework.onesignal;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class OnesignalMessage {

    private String jsonBody;
    private static ExecutorService asyncExecutor = Executors.newCachedThreadPool();

    public String getJsonBody() {
        return jsonBody;
    }

    @Override
    public String toString() {
        return "OneSignal message: " + jsonBody;
    }

    public boolean send() {
        if(!OnesignalConstants.isFinished())
            throw new RuntimeException("Tentando enviar notificação com o Onesignal não configurado.");
        try {
            String jsonResponse;

            URL url = new URL("https://onesignal.com/api/v1/notifications");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setUseCaches(false);
            con.setDoOutput(true);
            con.setDoInput(true);

            con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            con.setRequestProperty("Authorization", "Basic " + OnesignalConstants.getAuthorization());

            con.setRequestMethod("POST");

            String strJsonBody = jsonBody;

            byte[] sendBytes = strJsonBody.getBytes("UTF-8");
            con.setFixedLengthStreamingMode(sendBytes.length);

            OutputStream outputStream = con.getOutputStream();
            outputStream.write(sendBytes);

            int httpResponse = con.getResponseCode();
            //System.out.println("httpResponse: " + httpResponse);

            if (httpResponse >= HttpURLConnection.HTTP_OK && httpResponse < HttpURLConnection.HTTP_BAD_REQUEST) {
                try (Scanner scanner = new Scanner(con.getInputStream(), "UTF-8")) {
                    jsonResponse = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
                }
            } else {
                try (Scanner scanner = new Scanner(con.getErrorStream(), "UTF-8")) {
                    jsonResponse = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
                }
            }

            System.out.println("jsonResponse:\n" + jsonResponse);
            return true;
        } catch (Exception t) {
            t.printStackTrace();
            return false;
        }
    }

    public static class Builder {

        private String title;
        private String message;
        private Date dtSchedule;
        private String accentColor;
        private String messageGroup;
        private String groupDescription = "$[notif_count] novas notificações.";
        private final ArrayList<String> playerIds;
        private final Map<String, String> dataToSend;
        private JSONObject jsonBodyBuilder;

        public Builder() {
            playerIds = new ArrayList<>();
            dataToSend = new HashMap<>();
        }

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder setMessage(String message) {
            this.message = message;
            return this;
        }

        public Builder addReceiver(String playerID) {

            playerIds.add(playerID);
            return this;
        }

        public Builder addReceivers(List<String> playerIDs) {

            playerIds.addAll(playerIDs);
            return this;
        }

        public Builder setSendAfter(Date dtSchedule) {

            this.dtSchedule = dtSchedule;
            return this;
        }

        public Builder addData(String key, String value) {
            dataToSend.put(key, value);
            return this;
        }
        
        public Builder accentColor(String color){
            accentColor = color;
            return this;
        }
        
        public Builder messageGroup(String group){
            this.messageGroup = group;
            return this;
        }
        
        public Builder messageGroup(String group, String groupDescription){
            this.messageGroup = group;
            this.groupDescription = groupDescription;
            return this;
        }

        public void send() {
            OnesignalMessage.asyncExecutor.submit( () -> {
                try {
                    OnesignalMessage result = new OnesignalMessage();

                    jsonBodyBuilder = new JSONObject();

                    addRequiredField("app_id", OnesignalConstants.getAppID());
                    addRequiredField("contents", "en", message);
                    addOptionalField("headings", "en", title);
                    addRequiredField("include_player_ids", getPlayerIds());
                    if(dtSchedule != null)
                        addOptionalField( "send_after", dtSchedule );
                    if(accentColor != null)
                        addOptionalField("android_accent_color", accentColor);
                    if(messageGroup != null){
                        addOptionalField("android_group", messageGroup);
                        addOptionalField("thread_id", messageGroup);
                        addOptionalField("android_group_message", "en", groupDescription);
                    }

                    if (dataToSend.size() > 0) {

                        JSONObject data = new JSONObject();
                        dataToSend.forEach((k,v) -> {
                            data.put(k, v);
                        });

                        addOptionalField("data", data);
                    }

                    result.jsonBody = jsonBodyBuilder.toString();
                    result.send();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        private void addRequiredField(String key, JSONArray playerIDs) throws JSONException {

            jsonBodyBuilder.put(key, playerIDs);
        }

        private void addOptionalField(String key, String data) throws JSONException {

            if (data != null) {
                jsonBodyBuilder.put(key, data);
            }
        }

        private void addOptionalField(String key, JSONObject data) throws JSONException {

            if (data != null) {
                jsonBodyBuilder.put(key, data);
            }
        }

        private void addOptionalField( String key, Date dt ) throws JSONException {

                if( dt != null ) {
                        addOptionalField( key, new JSONObject(dt.toString() + " GMT-0300") );
                }
        }

        private void addRequiredField(String key, String language, String value) throws JSONException {

            if (value != null) {
                JSONObject values = new JSONObject();
                values.put(language, value);
                addOptionalField(key, values);
            } else {
                throw new RuntimeException( String.format( "Invalid required field %s", key ) );
            }
        }

        private JSONArray getPlayerIds() {

            JSONArray result = new JSONArray();
            playerIds.forEach((plr) -> {
                result.put(plr);
            });
            return result;
        }

        private void addOptionalField(String key, String language, String value) throws JSONException {

            if (value != null) {
                JSONObject values = new JSONObject();
                values.put(language, value);
                addOptionalField(key, values);
            }
        }

        private void addRequiredField(String key, String value) {

            if (value != null) {
                jsonBodyBuilder.put(key, value);
            } else {
            }
        }
    }
}
