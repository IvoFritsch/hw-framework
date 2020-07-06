/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package restFramework.response;

import com.google.gson.annotations.Expose;
import java.util.HashMap;
import java.util.Map;

/**
 * Coleciona os erros de validação em campos na HwResponse a ser retornada.
 *
 * @author Ivo
 */
class ErroValidacaoCampos extends Erro{

    @Expose
    private Map<String,ErroCampo> campos = new HashMap<>();
    
    public ErroValidacaoCampos() {
        super(TipoErro.CAMPOS_INVALIDOS);
    }
    
    public void addCampo(String campo, CodigoErroCampo codigo, String mensagem){
        if(campos.containsKey(campo)) return;
        campos.put(campo, new ErroCampo(codigo, mensagem));
    }
}
