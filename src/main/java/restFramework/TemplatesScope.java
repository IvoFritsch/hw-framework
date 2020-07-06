/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package restFramework;

import java.util.Map;

/**
 *
 * @author Ivo
 */
public class TemplatesScope {
    public String baseUrl(){
        return RestApi.frontendBaseUrl;
    }
    public String backendBaseUrl(){
        return RestApi.backendBaseUrl;
    }
    public String nomeProjeto(){
        return RestApi.nomeProjeto;
    }
    
    public Map<String,String> templates(){
        return RestApi.getResourceFiles();
    }
}
