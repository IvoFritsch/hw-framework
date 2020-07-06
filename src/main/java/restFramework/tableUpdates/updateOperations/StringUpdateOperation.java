/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package restFramework.tableUpdates.updateOperations;

import restFramework.tableUpdates.TipoOperacao;
import restFramework.tableUpdates.UpdateCampos;

/**
 * Faz uma operação de UPDATE em uma coluna varchar.
 * 
 * @author Ivo
 * @param <T>
 */
public class StringUpdateOperation <T extends UpdateCampos> extends UpdateOperation{

    private final T pai;
    
    public StringUpdateOperation(String nomeCampo, T pai) {
        super(nomeCampo);
        this.pai = pai;
    }

    /**
     * Concatena a string passada no final do conteúdo da coluna
     * @param v String a concatenar
     * @return O UpdateCampos para alterar outros campos
     */
    public T concat(String v){
        setTipoOperacao(TipoOperacao.CONCAT_FIM);
        this.v1 = v;
        return pai;
    }

    /**
     * Concatena a string passada no inicio do conteúdo da coluna
     * @param v String a concatenar
     * @return O UpdateCampos para alterar outros campos
     */
    public T concatInicio(String v){
        setTipoOperacao(TipoOperacao.CONCAT_INICIO);
        this.v1 = v;
        return pai;
    }

    /**
     * Define o valor do campo
     * @param v Valor a definir
     * @return O UpdateCampos para alterar outros campos
     */
    public T set(String v){
        if(v != null){
            setTipoOperacao(TipoOperacao.SET);
        } else {
            setTipoOperacao(TipoOperacao.SET_NULL);
        }
        this.v1 = v;
        return pai;
    }
    
}
