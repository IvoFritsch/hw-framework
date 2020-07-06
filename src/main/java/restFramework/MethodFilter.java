/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package restFramework;

import java.lang.reflect.Method;

/**
 *
 * @author pedro
 */
public class MethodFilter {
    
    public String nome;
    public String nomeCompleto;
    public Object resource;
    public Method metodo;
    public boolean prioritario = false;
    public boolean ignoravel = true;
    public boolean recebeNomeMetodo = false;

    public MethodFilter() {
    }
    
    public void aplicaFiltro(MethodInput inp, String nomeMetodo) throws Exception{
        //System.out.println("Aplicando filtro: "+nome);
        if(!recebeNomeMetodo)
            metodo.invoke(resource, inp);
        else
            metodo.invoke(resource, inp,nomeMetodo);
    }
    
}
