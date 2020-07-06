/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package restFramework.selecaoPersonalizada.sqlComparisions;

import restFramework.NamedPreparedStatement;
import restFramework.selecaoPersonalizada.ContadorPassavel;
import restFramework.selecaoPersonalizada.OperadorSql;

/**
 * Classe auxiliar utilizada internamente pelo Framework.
 *
 * @author ivoaf
 */
public abstract class SqlComparision {
    
    protected OperadorSql operador;
    
    public abstract String getClause(ContadorPassavel index);
    
    public abstract void addParameters(NamedPreparedStatement query, ContadorPassavel index);
}
