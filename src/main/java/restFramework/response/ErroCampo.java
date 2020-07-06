/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package restFramework.response;

import com.google.gson.annotations.Expose;

/**
 * Representa um erro em um campo específico na HwResponse a ser retornada.
 *
 * @author Ivo
 */
class ErroCampo {
    @Expose
    private final CodigoErroCampo codigo;
    @Expose
    private final String mensagem;

    public ErroCampo(CodigoErroCampo codigo, String mensagem) {
        this.codigo = codigo;
        this.mensagem = mensagem;
    }

    public CodigoErroCampo getCodigo() {
        return codigo;
    }

    public String getMensagem() {
        return mensagem;
    }
    
    
}
