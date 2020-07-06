/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package restFramework.tableUpdates.updateOperations;

import restFramework.tableUpdates.TipoOperacao;
import restFramework.tableUpdates.UpdateCampos;

/**
 * Faz uma operação de UPDATE customizada.
 * 
 * @author Ivo
 * @param <T>
 */
public class CustomUpdateOperation <T extends UpdateCampos> extends UpdateOperation{

    private final T pai;
    
    public CustomUpdateOperation(String clause, T pai) {
        super(clause);
        this.tipoOperacao = TipoOperacao.CUSTOM_CLAUSE;
        this.pai = pai;
    }

    @Override
    public String toString() {
        return "CustomUpdateOperation [pai=" + pai + "]";
    }
    

    
}
