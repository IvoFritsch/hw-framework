/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package restFramework.response;

import com.google.gson.annotations.Expose;

/**
 * Representa um erro no servidor na HwResponse a ser retornada.
 *
 * @author Ivo
 */
public class ErroInterno extends Erro{
    
    @Expose
    private String mensagem;
    
    public ErroInterno(String mensagem) {
        super(TipoErro.INTERNO);
        this.mensagem = mensagem;
    }
    public ErroInterno() {
        super(TipoErro.INTERNO);
        this.mensagem = "Ocorreu um erro interno no servidor ao processar o seu request, nenhuma alteração será executada";
    }
}
