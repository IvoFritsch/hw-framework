/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package restFramework.tableUpdates.updateOperations;

import restFramework.tableUpdates.TipoOperacao;
import restFramework.tableUpdates.UpdateCampos;

/**
 * Faz uma operação de UPDATE em uma coluna booleana.
 * 
 * @author Ivo
 * @param <T>
 */
public class BooleanUpdateOperation <T extends UpdateCampos> extends UpdateOperation{

    private final T pai;
    
    public BooleanUpdateOperation(String nomeCampo, T pai) {
        super(nomeCampo);
        this.pai = pai;
    }

    /**
     * Inverte o valor booleano
     * @return O UpdateCampos para alterar outros campos
     */
    public T invert(){
        setTipoOperacao(TipoOperacao.INVERT);
        return pai;
    }

    /**
     * Define o valor do campo
     * @param v Valor a definir
     * @return O UpdateCampos para alterar outros campos
     */
    public T set(Boolean v){
        if(v != null){
            setTipoOperacao(TipoOperacao.SET);
        } else {
            setTipoOperacao(TipoOperacao.SET_NULL);
        }
        this.v1 = v;
        return pai;
    }
    
}
