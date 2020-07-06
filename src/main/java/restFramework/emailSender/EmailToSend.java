/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package restFramework.emailSender;

import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author Ivo
 */
public class EmailToSend implements Runnable{
    
    String to;
    String subject;
    String conteudo;

    public EmailToSend(String to, String subject, String conteudo) {
        this.to = to;
        this.subject = subject;
        this.conteudo = conteudo;
    }
    
    @Override
    public void run() {
        JSONObject requestBody = new JSONObject()
                .put("personalizations", new JSONArray().put(new JSONObject().put("to", new JSONArray().put(new JSONObject().put("email", to))).put("subject", subject)))
                .put("from", new JSONObject().put("email", "nao-responda@haftware.com.br").put("name", EmailSender.getFromName()))
                .put("content", new JSONArray().put(new JSONObject().put("type", "text/html").put("value", conteudo)));
        
        try{
            URL url = new URL("https://api.sendgrid.com/v3/mail/send");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Authorization", "Bearer "+EmailSender.getSendgridApiKey());
            con.setDoOutput(true);
            try (DataOutputStream wr = new DataOutputStream(con.getOutputStream()); BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(wr, "UTF-8"))) {
                writer.write(requestBody.toString());
            }
            con.getResponseCode();
        } catch(Exception e){
            e.printStackTrace();
        }
    }
    
}
