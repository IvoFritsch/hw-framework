/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package restFramework;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import javax.servlet.ServletContext;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

/**
 *
 * @author Ivo
 */
public class IdiomasUtils {
    
    private static Map<String, Map<String, String>> mensagens;
    public static String idiomaDefault = "en";
    private static List<String> idiomas = new ArrayList<>();
    
    static void init(ServletContext ctx){
        mensagens = new HashMap<>();
        try{
            String json = IOUtils.toString(ctx.getResourceAsStream("/WEB-INF/multilanguage-texts.json"), "UTF-8");
            if(json == null) return;
            JSONObject obj = new JSONObject(json);
            obj.keySet().forEach(k -> {
                Map<String, String> toPut = new HashMap<>();
                JSONObject msg = obj.getJSONObject(k);
                msg.keySet().forEach(i -> {
                    toPut.put(i, msg.getString(i));
                    if(!idiomas.contains(i)) idiomas.add(i);
                });
                mensagens.put(k, toPut);
            });
        }catch(Exception e){
        }
    }
    
    static void forEachIdioma(Consumer<String> c){
        idiomas.forEach(c);
    }
    
    public static String getMensagem(String key, String idioma, Object... valores){
        if(idioma == null) idioma = idiomaDefault;
        try {
            return mensagens.get(key).get(idioma);
        } catch (Exception e1){
            try {
                return mensagens.get(key).get(idiomaDefault);
            } catch(Exception e2){
                return "";
            }
        } 
    }   
    
    public static boolean validaIdioma(String idioma){
        if(idioma == null) return false;
        return idioma.equals("en") 
            || idioma.equals("pt");
        
    }
}
