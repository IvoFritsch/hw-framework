/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package restFramework;

import com.google.gson.annotations.Expose;

/**
 *
 * @author Ivo
 */
public class PagamentoCriado {
    @Expose
    private final String referencia;
    @Expose
    private final String urlPagamento;

    PagamentoCriado(String referencia, String urlPagamento) {
        this.referencia = referencia;
        this.urlPagamento = urlPagamento;
    }

    public String getReferencia() {
        return referencia;
    }

    public String getUrlPagamento() {
        return urlPagamento;
    }

    @Override
    public String toString() {
        return referencia + " || " + urlPagamento;
    }
    
    
}
