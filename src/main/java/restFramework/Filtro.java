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
 * Indica que o método é um filtro que o framework deve executar antes de chamar o Metodo da API
 * 
 * Os métodos de filtro devem ter, como parâmetro de entrada, um 'MethodInput' que será uma cópia da input passada ao método da API e, opcionalmente, uma String para indicar o nome do método da API
 * 
 * @author Ivo
 */
@Target(value = {ElementType.TYPE, ElementType.METHOD})
@Retention(value = RetentionPolicy.RUNTIME)
public @interface Filtro {

    public String value();
}
