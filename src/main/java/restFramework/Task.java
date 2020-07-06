/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package restFramework;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indica que o método é uma task programada, 
 * O parametro dessa anotação é uma 'String' que indica o delay, em minutos, entre cada execução desse método.<br>
 * Para delay de segundos, colocar no parâmetro um número com virgulas, por exemplo, 0.5 fará com que a task seja executada a cada 30 segundos.<br>
 * As task também podem ser programadas para rodar uma hora do dia específica, usar o formato HH:mm para isso.<br>
 * 
 * @author Ivo
 */
@Target(value = {ElementType.TYPE, ElementType.METHOD})
@Retention(value = RetentionPolicy.RUNTIME)
public @interface Task {

    public String value();
}
