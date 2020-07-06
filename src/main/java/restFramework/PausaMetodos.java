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
 * Indica os métodos da api que devem ser pausados enquanto essa task/job estiver sendo executado
 *    A task vai esperar os métodos pendentes terminarem antes de rodar
 * 
 * @author Ivo
 */
@Target(value = {ElementType.TYPE, ElementType.METHOD})
@Retention(value = RetentionPolicy.RUNTIME)
public @interface PausaMetodos {

    public String[] value();
}
