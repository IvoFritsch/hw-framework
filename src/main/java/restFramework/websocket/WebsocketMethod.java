/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package restFramework.websocket;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indica que o método é um método websocket.<br>
 * O parametro dessa anotação é uma {@link String} que indica o nome do método websocket.<br>
 * Um método websocket é chamado pelo cliente usando o campo 'metodo' no JSON enviado.<br>
 * O método com essa anotação deve ter retorno void, e dois parâmetros:<br>
 * 1. Um {@link WebsocketClient}, que representa o cliente que chamou o método<br>
 * 2. Uma {@link WebsocketMessage}, que contém a mensagem enviada<br>
 * 
 * @author Ivo
 */
@Target(value = {ElementType.TYPE, ElementType.METHOD})
@Retention(value = RetentionPolicy.RUNTIME)
public @interface WebsocketMethod {

    public String value();
}
