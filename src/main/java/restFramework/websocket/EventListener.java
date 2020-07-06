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
 * Indica que o método é um escutador de eventos enviados ao websocket.<br>
 * O parametro dessa anotação é uma {@link String} que indica o nome do do evento escutado.<br>
 * Um evento pode ser disparado pelos métodos REST da api, causando assim respostas dos websockets abertos.<br>
 * Para disparar um evento usa-se a função {@link WebsocketManager}.triggerEvent.<br>
 * O websocket só estará sujeito a eventos no momento que ele definir pelo menos um usuário interessado.<br>
 * 1. Um {@link WebsocketClient}, que representa o cliente que chamou o método<br>
 * 2. Uma {@link WebsocketMessage}, que contém a mensagem enviada<br>
 * 
 * @author Ivo
 */
@Target(value = {ElementType.TYPE, ElementType.METHOD})
@Retention(value = RetentionPolicy.RUNTIME)
public @interface EventListener {

    public String value();
}
