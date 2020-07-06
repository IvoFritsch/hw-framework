/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package restFramework.selecaoPersonalizada;

/**
 * Enumerado que representa as possíveis comparações SQL que podem ser feitas.
 *
 * @author Ivo
 */
public enum OperadorSql {
    EQUALS ("(<c> = :<v>1)"),
    // Adiciona um teste para NULL tbm, pq por padrão o SQL não inclui colunas NULL em queryes "<>"
    NOT_EQUALS ("(<c> <> :<v>1 OR <c> IS NULL)"),
    EQUALS_NULL ("(<c> IS NULL)"),
    NOT_EQUALS_NULL ("(<c> IS NOT NULL)"),
    GREATER ("(<c> > :<v>1)"),
    LESS ("(<c> < :<v>1)"),
    NOT_LESS ("(<c> >= :<v>1)"),
    NOT_GREATER ("(<c> <= :<v>1)"),
    BETWEEN_INCLUSIVE ("(<c> >= :<v>1 AND <c> <= :<v>2)"),
    BETWEEN_EXCLUSIVE ("(<c> > :<v>1 AND <c> < :<v>2)"),
    NOT_BETWEEN_INCLUSIVE ("(<c> <= :<v>1 AND <c> >= :<v>)"),
    NOT_BETWEEN_EXCLUSIVE ("(<c> <= :<v>1 AND <c> >= :<v>2)"),
    LIKE ("(LOWER(<c>) LIKE LOWER(:<v>1))"),
    IN ("(<c> IN (<many_clause>))"),
    NOT_IN ("(<c> NOT IN (<many_clause>))"),
    EQUALS_INSENSITIVE ("(LOWER(<c>) = LOWER(:<v>1))"),
    SAME_DAY ("(CAST(<c> AS DATE) = CAST(:<v>1 AS DATE))");

    private final String name;       

    private OperadorSql(String s) {
        name = s;
    }

    public boolean equalsName(String otherName) {
        return name.equals(otherName);
    }

    @Override
    public String toString() {
       return this.name;
    }
}
