/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package restFramework;

import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.json.JSONObject;

/**
 *
 * @author pedro
 */
public class SmsSender{
    
    private static final String totalVoiceApiKey = "1f31a7f3207f7bd166f09efe9a209853";
    
    private final ExecutorService asyncExecutor = Executors.newCachedThreadPool();
    
    public void send(String numero, String message){
        asyncExecutor.submit(() -> {
            // {"numero_destino":"","mensagem":"","resposta_usuario":false,"multi_sms":false,"data_criacao":""}
            try{
            JSONObject requestBody = new JSONObject()
                    .put("numero_destino", numero.replaceAll("[^0-9]", ""))
                    .put("mensagem", message)
                    .put("multi_sms", false)
                    .put("resposta_usuario", false);
                URL url = new URL("https://api.totalvoice.com.br/sms");
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty("Content-Type", "application/json");
                con.setRequestProperty("Accept", "application/json");
                con.setRequestProperty("Access-Token", totalVoiceApiKey);
                con.setDoOutput(true);
                try (DataOutputStream wr = new DataOutputStream(con.getOutputStream()); BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(wr, "UTF-8"))) {
                    writer.write(requestBody.toString());
                }
                con.getResponseCode();
            } catch(Exception e){
                e.printStackTrace();
            }
        
        
        });
    }
}
