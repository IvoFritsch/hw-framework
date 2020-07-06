/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package restFramework;

import restFramework.response.HwResponse;

/**
 * Classe auxiliar para servir de Input de métodos que não recebem nada como entrada.
 * 
 * @author Ivo
 */
public class InputVazia extends MethodInput{

    @Override
    protected void filtraCampos() {
    }

    @Override
    protected void validaCampos(HwResponse res) {
    }

    @Override
    protected String getMethodNamespace() {
        return null;
    }
    
}
