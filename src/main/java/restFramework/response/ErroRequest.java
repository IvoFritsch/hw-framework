/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package restFramework.response;

import com.google.gson.annotations.Expose;

/**
 * Representa um erro de request na HwResponse a ser retornada.
 *
 * @author Ivo
 */
public class ErroRequest extends Erro{
    
    @Expose
    private String mensagem;
    
    public ErroRequest(String mensagem) {
        super(TipoErro.REQUEST);
        this.mensagem = mensagem;
    }
}
