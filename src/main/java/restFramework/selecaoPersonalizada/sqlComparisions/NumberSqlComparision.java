/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package restFramework.selecaoPersonalizada.sqlComparisions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
public class NumberSqlComparision<T extends Seletor> extends SqlComparision{

    private final String nomeCampo;
    private Number n1;
    private Number n2;
    private List<Number> many1;
    private boolean none = false;
    private boolean all = false;
    private final T pai;
    
    public NumberSqlComparision(String nomeCampo, T pai) {
        this.nomeCampo = nomeCampo;
        this.pai = pai;
    }
    
    public T equals(Number n1){
        if(n1 == null) 
            operador = OperadorSql.EQUALS_NULL;
        else
            operador = OperadorSql.EQUALS;
        this.n1 = n1;
        return pai;
    }
    
    public T notEquals(Number n1){
        if(n1 == null) 
            operador = OperadorSql.NOT_EQUALS_NULL;
        else
            operador = OperadorSql.NOT_EQUALS;
        this.n1 = n1;
        return pai;
    }
    
    public T lessThan(Number n1){
        operador = OperadorSql.LESS;
        this.n1 = n1;
        return pai;
    }
    
    public T greaterThan(Number n1){
        operador = OperadorSql.GREATER;
        this.n1 = n1;
        return pai;
    }
    
    public T notLessThan(Number n1){
        operador = OperadorSql.NOT_LESS;
        this.n1 = n1;
        return pai;
    }
    
    public T notGreaterThan(Number n1){
        operador = OperadorSql.NOT_GREATER;
        this.n1 = n1;
        return pai;
    }
    
    public T betweenExclusive(Number n1,Number n2){
        operador = OperadorSql.BETWEEN_EXCLUSIVE;
        this.n1 = n1;
        this.n2 = n2;
        return pai;
    }
    
    public T notBetweenExclusive(Number n1,Number n2){
        operador = OperadorSql.NOT_BETWEEN_EXCLUSIVE;
        this.n1 = n1;
        this.n2 = n2;
        return pai;
    }
    
    public T betweenInclusive(Number n1,Number n2){
        operador = OperadorSql.BETWEEN_INCLUSIVE;
        this.n1 = n1;
        this.n2 = n2;
        return pai;
    }
    
    public T notBetweenInclusive(Number n1,Number n2){
        operador = OperadorSql.NOT_BETWEEN_INCLUSIVE;
        this.n1 = n1;
        this.n2 = n2;
        return pai;
    }
    
    public T in(List<Number> many){
        if(many == null) many = new ArrayList<>();
        if(many.isEmpty()){
            none = true;
            return pai;
        }
        operador = OperadorSql.IN;
        this.many1 = many;
        return pai;
    }
    
    public T in(Number... many){
        if(many.length == 0) throw new RuntimeException("Tentando filtrar via \"in\" passando um array vazio");
        operador = OperadorSql.IN;
        many1 = Arrays.asList(many);
        return pai;
    }
    
    public T notIn(List<Number> many){
        if(many == null) many = new ArrayList<>();
        if(many.isEmpty()){
            all = true;
            return pai;
        }
        operador = OperadorSql.NOT_IN;
        this.many1 = many;
        return pai;
    }
    
    public T notIn(Number... many){
        if(many.length == 0) throw new RuntimeException("Tentando filtrar via \"notIn\" passando um array vazio");
        operador = OperadorSql.NOT_IN;
        many1 = Arrays.asList(many);
        return pai;
    }

    @Override
    public String getClause(ContadorPassavel index) {
        if(none) return "(1 <> 1)";
        if(all) return "(1 = 1)";
        String retorno;
        if(operador == OperadorSql.IN || operador == OperadorSql.NOT_IN){
            StringBuilder manyClause = new StringBuilder();
            int manySize = many1.size();
            for (int i = 0; i < manySize; i++) {
                manyClause.append(":mv");
                manyClause.append(nomeCampo);
                manyClause.append(index.cont);
                manyClause.append(i);
                if(i < manySize-1) manyClause.append(",");
            }
            retorno = operador.toString().replace("<c>", nomeCampo).replace("<many_clause>", manyClause);
        } else {
            retorno = operador.toString().replace("<c>", nomeCampo).replace("<v>", nomeCampo+index.cont);
        }
        index.cont++;
        return retorno;
    }

    @Override
    public void addParameters(NamedPreparedStatement query,ContadorPassavel index) {
        if(none || all) return;
        if(operador == OperadorSql.IN || operador == OperadorSql.NOT_IN){
            int i = 0;
            for (Number manyValue : many1) {
                query.setParameter(("mv"+nomeCampo+index.cont)+i, manyValue);
                i++;
            }
        } else {
            if(n1 != null)
                query.setParameter(nomeCampo+index.cont+"1", n1);
            if(n2 != null)
                query.setParameter(nomeCampo+index.cont+"2", n2);
        }
        index.cont++;
    }
    
    
    
}
