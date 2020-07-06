/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package restFramework.tableUpdates.updateOperations;

import restFramework.tableUpdates.TipoOperacao;
import restFramework.tableUpdates.UpdateCampos;

/**
 * Faz uma operação de UPDATE em uma coluna numérica.
 * 
 * @author Ivo
 * @param <T>
 */
public class NumberUpdateOperation <T extends UpdateCampos> extends UpdateOperation{

    private final T pai;
    
    public NumberUpdateOperation(String nomeCampo, T pai) {
        super(nomeCampo);
        this.pai = pai;
    }

    /**
     * Aplica a soma do valor indicado no campo
     * @param v Valor a somar
     * @return O UpdateCampos para alterar outros campos
     */
    public T add(Number v){
        setTipoOperacao(TipoOperacao.ADD);
        this.v1 = v;
        return pai;
    }

    /**
     * Aplica a subtracao do valor indicado no campo
     * @param v Valor a somar
     * @return O UpdateCampos para alterar outros campos
     */
    public T subtract(Number v){
        setTipoOperacao(TipoOperacao.SUBTRACT);
        this.v1 = v;
        return pai;
    }

    /**
     * Aplica a multiplicação do valor indicado no campo
     * @param v Valor a multiplicar
     * @return O UpdateCampos para alterar outros campos
     */
    public T multiply(Number v){
        setTipoOperacao(TipoOperacao.MULTIPLY);
        this.v1 = v;
        return pai;
    }

    /**
     * Aplica a divisão do campo pelo valor indicado
     * @param v Valor divisor
     * @return O UpdateCampos para alterar outros campos
     */
    public T divide(Number v){
        setTipoOperacao(TipoOperacao.DIVIDE);
        this.v1 = v;
        return pai;
    }

    /**
     * Define o valor do campo
     * @param v Valor a definir
     * @return O UpdateCampos para alterar outros campos
     */
    public T set(Number v){
        if(v != null){
            setTipoOperacao(TipoOperacao.SET);
        } else {
            setTipoOperacao(TipoOperacao.SET_NULL);
        }
        this.v1 = v;
        return pai;
    }
    
}
