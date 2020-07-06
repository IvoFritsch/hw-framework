/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package restFramework.response;

import com.google.gson.annotations.Expose;

/**
 * Classe pai dos erro na HwResponse a ser retornada.
 *
 * @author Ivo
 */
public abstract class Erro {
    
    @Expose
    private TipoErro tipo;

    public Erro(TipoErro tipo) {
        this.tipo = tipo;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj.getClass() != Erro.class) return false;
        if(((Erro)obj).tipo == this.tipo) return true;
        return super.equals(obj);
    }
}
