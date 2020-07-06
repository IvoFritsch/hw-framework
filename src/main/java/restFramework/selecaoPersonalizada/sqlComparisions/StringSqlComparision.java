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
public class StringSqlComparision<T extends Seletor> extends SqlComparision{

    private final String nomeCampo;
    private String s1;
    private String s2;
    private final T pai;
    
    public StringSqlComparision(String nomeCampo, T pai) {
        this.nomeCampo = nomeCampo;
        this.pai = pai;
    }
    
    public T equals(String s1){
        if(s1 == null) 
            operador = OperadorSql.EQUALS_NULL;
        else
            operador = OperadorSql.EQUALS;
        this.s1 = s1;
        return pai;
    }
    
    public T equalsIgnoreCase(String s1){
        operador = OperadorSql.EQUALS_INSENSITIVE;
        this.s1 = s1;
        return pai;
    }
    
    public T notEquals(String s1){
        if(s1 == null) 
            operador = OperadorSql.NOT_EQUALS_NULL;
        else
            operador = OperadorSql.NOT_EQUALS;
        this.s1 = s1;
        return pai;
    }
    
    public T lessThan(String s1){
        operador = OperadorSql.LESS;
        this.s1 = s1;
        return pai;
    }
    
    public T greaterThan(String s1){
        operador = OperadorSql.GREATER;
        this.s1 = s1;
        return pai;
    }
    
    public T like(String s1){
        operador = OperadorSql.LIKE;
        this.s1 = s1;
        return pai;
    }
    
    public T notGreaterThan(String s1){
        operador = OperadorSql.NOT_GREATER;
        this.s1 = s1;
        return pai;
    }
    
    public T notLessThan(String s1){
        operador = OperadorSql.NOT_LESS;
        this.s1 = s1;
        return pai;
    }

    @Override
    public String getClause(ContadorPassavel index) {
        String retorno = operador.toString().replace("<c>", nomeCampo).replace("<v>", nomeCampo+index.cont);
        index.cont++;
        return retorno;
    }

    @Override
    public void addParameters(NamedPreparedStatement query, ContadorPassavel index) {
        if(s1 != null)
            query.setParameter(nomeCampo+index.cont+"1", s1);
        if(s2 != null)
            query.setParameter(nomeCampo+index.cont+"2", s2);
        index.cont++;
    }

}
