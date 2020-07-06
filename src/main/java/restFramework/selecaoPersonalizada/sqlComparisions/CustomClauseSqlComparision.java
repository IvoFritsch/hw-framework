/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package restFramework.selecaoPersonalizada.sqlComparisions;

import restFramework.NamedPreparedStatement;
import restFramework.selecaoPersonalizada.ContadorPassavel;
import restFramework.selecaoPersonalizada.Seletor;

/**
 * Classe auxiliar utilizada internamente pelo Framework.
 *
 * @author ivoaf
 * @param <T>
 */
public class CustomClauseSqlComparision<T extends Seletor> extends SqlComparision{
    
    private final String clause;
    private final T pai;
    
    public CustomClauseSqlComparision(String clause, T pai) {
        this.clause = clause;
        this.pai = pai;
    }
    
    @Override
    public String getClause(ContadorPassavel index) {
        return "("+clause+")";
    }

    @Override
    public void addParameters(NamedPreparedStatement query,ContadorPassavel index) {
    }

    @Override
    public String toString() {
        return "CustomClauseSqlComparision [clause=" + clause + ", pai=" + pai + "]";
    }

    
}
