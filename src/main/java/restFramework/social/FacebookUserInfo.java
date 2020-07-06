/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package restFramework.social;

import facebook4j.Facebook;
import facebook4j.FacebookFactory;
import facebook4j.Reading;
import facebook4j.User;
import facebook4j.auth.AccessToken;

/**
 * Classe auxiliar para representar um usuário do Facebook
 *
 * @author Ivo
 */
public class FacebookUserInfo {

    private static String appId;
    private static String appSecret;
    
    private facebook4j.User fbUser;
    private boolean ok = true;
    
    
    /**
     * Recebendo um accessToken, contrói o usuário carregando do servidor do Facebook
     * 
     * @param accessToken Token de acesso retornado após o método FB.login() do javascript
     */
    public FacebookUserInfo(String accessToken) {
        
        Facebook facebook = new FacebookFactory().getInstance();
        facebook.setOAuthAppId(appId, appSecret);
        facebook.setOAuthAccessToken(new AccessToken(accessToken, null));
        
        try {
            facebook4j.User user = facebook.getMe(new Reading().fields("email,name"));
            fbUser = user;
        } catch (Exception ex) {
            ex.printStackTrace();
            ok = false;
        }
        
    }
    
    /**
     * Recebendo um accessToken, contrói o usuário carregando do servidor do Facebook
     * 
     * @param accessToken Token de acesso retornado após o método FB.login() do javascript
     * @param fields Campos a pegar do facebook, padrão: name,email
     */
    public FacebookUserInfo(String accessToken, String fields) {
        
        Facebook facebook = new FacebookFactory().getInstance();
        facebook.setOAuthAppId(appId, appSecret);
        facebook.setOAuthAccessToken(new AccessToken(accessToken, null));
        
        try {    
            facebook4j.User user = facebook.getMe(new Reading().fields(fields));
            fbUser = user;
        } catch (Exception ex) {
            ok = false;
        }
        
    }
    
    public String getUserProfilePic(){
        if(!isOk() || fbUser == null) return null;
        return "http://graph.facebook.com/"+fbUser.getId()+"/picture?type=square&height=150&width=150";
    }

    public boolean isOk() {
        return ok;
    }

    public User getFbUser() {
        return fbUser;
    }
    
    public static void setAppId(String appId) {
        FacebookUserInfo.appId = appId;
    }

    public static void setAppSecret(String appSecret) {
        FacebookUserInfo.appSecret = appSecret;
    }   
}
