/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package restFramework.selecaoPersonalizada.sqlComparisions;

import restFramework.NamedPreparedStatement;
import restFramework.selecaoPersonalizada.ContadorPassavel;
import restFramework.selecaoPersonalizada.OperadorSql;
import restFramework.selecaoPersonalizada.Seletor;

/**
 * Classe auxiliar utilizada internamente pelo Framework.
 *
 * @author ivoaf
 * @param <T>
 */
public class BooleanSqlComparision<T extends Seletor> extends SqlComparision{

    private final String nomeCampo;
    private Boolean b;
    private final T pai;
    
    public BooleanSqlComparision(String nomeCampo, T pai) {
        this.nomeCampo = nomeCampo;
        this.pai = pai;
    }
    
    public T equals(Boolean b){
        if(b == null) 
            operador = OperadorSql.EQUALS_NULL;
        else
            operador = OperadorSql.EQUALS;
        this.b = b;
        return pai;
    }
    
    public T notEquals(Boolean b){
        if(b == null) 
            operador = OperadorSql.NOT_EQUALS_NULL;
        else
            operador = OperadorSql.NOT_EQUALS;
        this.b = b;
        return pai;
    }
    
    @Override
    public String getClause(ContadorPassavel index) {
        String retorno = operador.toString().replace("<c>", nomeCampo).replace("<v>", nomeCampo+index.cont);
        index.cont++;
        return retorno;
    }

    @Override
    public void addParameters(NamedPreparedStatement query,ContadorPassavel index) {
        if(b != null)
            query.setParameter(nomeCampo+index.cont+"1", b ? 1 : 0);
        index.cont++;
    }
}
