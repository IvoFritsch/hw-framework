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
 * Indica que o método da Api Rest pode ser chamado se realizar a autenticacão
 * Por padrão, todos os métodos são autenticados antes de sua chamada, use essa anotação para indicar que o método
 * pode ser chamado diretamente
 * 
 * @author Ivo
 */
@Target(value = {ElementType.METHOD})
@Retention(value = RetentionPolicy.RUNTIME)
public @interface SemAuth {
}
