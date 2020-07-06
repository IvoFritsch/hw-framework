/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package restFramework;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONObject;

/**
 *
 * @author Pedrivo
 */
public class JsonManager {
    public static String toJson(Object obj){
        return new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create().toJson(obj);
    }
    
    public static String toJsonOnlyExpose(Object obj){
        return new GsonBuilder().excludeFieldsWithoutExposeAnnotation()
                .setDateFormat("yyyy-MM-dd HH:mm:ss").create().toJson(obj);
    }
    
    public static Gson getGson(){
        return new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
    }
    
    public static Gson getGsonWithoutExpose(){
        return new GsonBuilder().excludeFieldsWithoutExposeAnnotation().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
    }
    

    public static String identaJson(String json){
        return new JSONObject(json).toString(2);
    }
    
    
}
