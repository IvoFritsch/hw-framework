/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package restFramework.response;

import com.google.gson.annotations.Expose;

/**
 * Representa um erro de autenticação na HwResponse a ser retornada.
 *
 * @author Ivo
 */
public class ErroAutenticacao extends Erro{
    
    @Expose
    private String mensagem;
    
    public ErroAutenticacao(String mensagem) {
        super(TipoErro.AUTH);
        this.mensagem = mensagem;
    }
}
