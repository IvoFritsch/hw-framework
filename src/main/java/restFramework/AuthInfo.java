/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package restFramework;

import org.json.JSONObject;

/**
 *
 * @author Ivo
 */
public class AuthInfo {

    String authId;
    Integer idUsuario;
    JSONObject params;
    private String unparsedParams;
    private boolean alreadySaved = false;
    
    AuthInfo(String authId, Integer idUsuario, String params) {
        this.authId = authId;
        this.idUsuario = idUsuario;
        this.unparsedParams = params;
        this.alreadySaved = true;
    }
    
    public AuthInfo(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }
    
    public AuthInfo setParametro(String nome, Object valor){
        if(alreadySaved) throw new RuntimeException("Não é possivel alterar os parâmetros de um authId que já foi gravado, você deve criar um novo.");
        if(params == null) params = new JSONObject();
        params.put(nome, valor);
        return this;
    }
    
    public <T> T getParametro(String nome, Class<T> tipo){
        if(params == null && unparsedParams == null) return null;
        if(params == null) params = new JSONObject(unparsedParams);
        try {
            return tipo.cast(params.get(nome));
        } catch(Exception e){
            return null;
        }
        
    }
    
}
