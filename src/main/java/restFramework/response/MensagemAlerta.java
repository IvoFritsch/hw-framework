/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package restFramework.response;

import com.google.gson.annotations.Expose;
import java.util.Date;

/**
 * Representa uma mensagem de alerta na HwResponse a ser retornada.
 *
 * @author Ivo
 */
public class MensagemAlerta {
    @Expose
    String titulo = "";
    @Expose
    String mensagem = "";
    @Expose
    String bgColor= "black";
    @Expose
    String textColor = "#fff";
    @Expose
    String icon = "";
    @Expose
    String msgId;
    @Expose
    Boolean safe;
    
    
    
    Date criacao;
    
    public MensagemAlerta titulo(String str){
        titulo = str;
        return this;
    }
    public MensagemAlerta mensagem(String str){
        mensagem = str;
        return this;
    }

    /**
     * Indica que a mensagem é segura e pode ter o html exibido literalmente, se precisar fazer escape dos caracteres
     * 
     * @return
     */
    public MensagemAlerta safe(){
        safe = true;
        return this;
    }
    public MensagemAlerta bgColor(String str){
        bgColor = str;
        return this;
    }
    public MensagemAlerta bgColor(CssColor color){
        bgColor = color.toString();
        return this;
    }
    public MensagemAlerta textColor(String str){
        textColor = str;
        return this;
    }
    public MensagemAlerta textColor(CssColor color){
        textColor = color.toString();
        return this;
    }
    public MensagemAlerta icon(String str){
        icon = str;
        return this;
    }
    public MensagemAlerta msgId(String str){
        msgId = str;
        return this;
    }
    
    MensagemAlerta withDate(Date criacao){
        this.criacao = criacao;
        return this;
    }
}
