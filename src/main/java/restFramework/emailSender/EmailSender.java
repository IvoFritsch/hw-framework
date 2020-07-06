/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package restFramework.emailSender;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * @author pedro
 */
public class EmailSender{
    
    private static String fromName = "Haftware SI";
    private static String sendgridApiKey;
    
    private final ExecutorService asyncExecutor = Executors.newCachedThreadPool();
    
    public void send(String destino, String assunto, String message){
        asyncExecutor.submit(new EmailToSend(destino, assunto, message));
    }

    public static void setFromName(String fromName) {
        EmailSender.fromName = fromName;
    }

    public static String getFromName() {
        return fromName;
    }

    public static String getSendgridApiKey() {
        return sendgridApiKey;
    }

    public static void setSendgridApiKey(String key) {
        sendgridApiKey = key;
    }
}
