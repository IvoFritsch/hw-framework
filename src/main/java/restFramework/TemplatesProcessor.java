/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package restFramework;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import java.io.StringReader;
import java.io.StringWriter;

/**
 *
 * @author Ivo
 */
public class TemplatesProcessor {
    
    
    public static String processaTemplate(String nome, TemplatesScope scope){
        MustacheFactory mf = new DefaultMustacheFactory();
        
        Mustache mustache = mf.compile(new StringReader(RestApi.getResourceFile(nome)),
                nome);
        return mustache.execute(new StringWriter(), scope).toString();
    }
    public static String processaTemplate(String nome, String idioma, TemplatesScope scope){
        MustacheFactory mf = new DefaultMustacheFactory();
        
        Mustache mustache = mf.compile(new StringReader(RestApi.getResourceFile(nome, idioma)),
                nome);
        return mustache.execute(new StringWriter(), scope).toString();
    }
    
    
}
