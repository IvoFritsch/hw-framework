/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package restFramework;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 *
 * @author Ivo
 * @param <T> Classe de modelo
 */
public interface QueryMethods<T> {
    
    /**
     * Conta quantos registros se encaixam no filtro definido
     * 
     * @return Quantidade de registros
     */
    public int count();
    
    /**
     * Retorna o primeiro registro encontrado que se encaixe no filtro definido
     * 
     * @return Registro ou null caso n encontre nada
     */
    public T findOne();
    
    /**
     * Retorna o primeiro registro encontrado que se encaixe no filtro definido 
     * ou lança um erro de request cao não encontre nada
     * 
     * @param throwError Erro a lançar caso não encontrar nada
     * @return Registro encontrado
     */
    public T findOneOrThrow(String throwError);

    /**
     * Retorna uma lista com todos os registros encontrados para o filtro definido
     * 
     * @return Lista de registros
     */
    public List<T> find();
    
    /**
     * Retorna uma lista com pagina específica dos registros encontrados para o filtro definido
     * 
     * @param pageSize O tamanho de uma página
     * @param pageNumber O numero da pagina, começando em 0
     * 
     * @return Lista de registros na página
     */
    public List<T> find(int pageSize, int pageNumber);
    
    /**
     * Executa a função passada para cada um dos registros que passarem no filtro definido,
     * lendo o banco de forma incremental, sem sobrecarregar a memória
     * 
     * @param c Função a executar para cada um dos registros, a função recebe o registro e o index
     * @return Quantidade de total de registros processados
     */
    public int findLazy(BiConsumer<T, Integer> c);
    
    /**
     * Executa a função passada para cada um dos registros que passarem no filtro definido,
     * lendo o banco de forma incremental, sem sobrecarregar a memória
     * 
     * @param c Função a executar para cada um dos registros, a função recebe o registro
     * @return Quantidade de total de registros processados
     */
    public int findLazy(Consumer<T> c);
}
