/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package restFramework.selecaoPersonalizada;

import java.util.ArrayList;
import java.util.List;

import restFramework.NamedPreparedStatement;
import restFramework.selecaoPersonalizada.sqlComparisions.SqlComparision;


// new SeletorFatura().idCliente().equals("").faturaAberta().equals

/**
 *
 * @author ivoaf
 */
public abstract class Seletor extends SqlComparision{

    private final List<SqlComparision> comparacoes;
    private final String tipoConcat;
    protected boolean isAll = false;
    
    public Seletor(TipoSeletor tipo) {
        this.comparacoes = new ArrayList<>();
        this.tipoConcat = " "+tipo.toString()+" ";
    }
    
    protected void addComparision(SqlComparision comp){
        comparacoes.add(comp);
    }
    
    public boolean hasFilter(){
        return comparacoes.size() > 0;
    }
    
    public String getClause(){
        return getClause(new ContadorPassavel());
    }
    
    @Override
    public String getClause(ContadorPassavel index){
        if(isAll) 
            return "(1 = 1)";
        if(comparacoes.isEmpty()) 
            return "(1 <> 1)";
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        int conta = 0;
        for (SqlComparision c : comparacoes) {
            if(conta > 0)
                sb.append(tipoConcat);
            sb.append(c.getClause(index));
            conta++;
        }
        sb.append(")");
        return sb.toString();
    }
    
    public void addParameters(NamedPreparedStatement query){
        addParameters(query, new ContadorPassavel());
    }
    
    @Override
    public void addParameters(NamedPreparedStatement query, ContadorPassavel index){
        comparacoes.forEach(c -> {
            c.addParameters(query, index);
        });
    }
}
