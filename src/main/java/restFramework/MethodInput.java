/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package restFramework;

import restFramework.response.HwResponse;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

/**
 * Classe pai de todas as entradas de métodos da API
 * 
 * @author Ivo
 */
public abstract class MethodInput extends DaosProvider{
    
    public HttpServletRequest req;
    public HwResponse resp;
    public String authId;
    public Integer idUsuAutenticado;
    public AuthInfo authInfo;
    public Map<String,Object> parametros;
    public String nomeMetodo;
    public String rawBody;
    
    /**
     * Busca o valor de um parametro nessa input
     * Parametros são usados para que os filtros passem valores para os metodos
     * 
     * @param nome Nome do parâmetro a buscar
     * @return Valor do parâmetro, null se não existir
     */
    public Object getParametro(String nome){
        return parametros.get(nome);
    }
    
    /**
     * Busca o valor de um parametro nessa input
     * Parametros são usados para que os filtros passem valores para os metodos
     * 
     * @param nome Nome do parâmetro a buscar
     * @param tipo Tipo do retorno
     * @return Valor do parâmetro, null se não existir
     */
    public <T> T getParametro( String nome, Class<T> tipo ){
        return tipo.cast(parametros.get(nome));
    }
    
    /**
     * Define um novo parametro nessa input
     * Parametros são usados para que os filtros passem valores para os metodos
     * 
     * @param nome Nome do parâmetro a criar
     * @param obj Valor do parâmetro a criar
     */
    public void setParametro(String nome, Object obj){
        parametros.put(nome, obj);
    }
    
    public String toJson(){
        return JsonManager.toJsonOnlyExpose(this);
    }
    
    public Map<String, String[]> allUrlParams(){
        return req.getParameterMap();
    }
    
    public String getUrlParam(String nome){
        return req.getParameter(nome);
    }
    
    public String getUrlParam(String nome, String defaultValue){
        String saida = req.getParameter(nome);
        if(saida == null) return defaultValue;
        return saida;
    }

    /**
     * Retorna o parâmetro da URL já casteado no formato especificado ou valor default caso o parâmetro não exista
     *
     * @param nome nome do parâmetro
     * @param type Tipo de retorno
     * @param def Valor dafault a retornar caso o parametro não exista
     * @return Valor do parâmetro da URL já casteado
     */
    public <T> T getUrlParamOrDefault(String nome, Class<T> type, T def){
        T urlParam = getUrlParam(nome, type, false);
        return urlParam != null ? urlParam : def;
    }
    
    /**
     * Retorna o parâmetro da URL já casteado no formato especificado
     *
     * @param nome nome do parâmetro
     * @param type Tipo de retorno
     * @param disparaErroCast Indica se deve disparar erro de cast, se for false, retorna null em caso de erro
     * @return Valor do parâmetro da URL já casteado
     */
    public <T> T getUrlParam(String nome, Class<T> type, boolean disparaErroCast){
        Object retorno = getUrlParam(nome);
        if(retorno == null) return null;
        try{
            if(type == Integer.class)
                return type.cast(new Integer((String) retorno));
            if(type == Long.class)
                return type.cast(new Long((String) retorno));
            if(type == Double.class)
                return type.cast(new Double((String) retorno));
            if(type == String.class)
                return type.cast(retorno);
            if(type == Boolean.class)
                return type.cast(retorno.equals("true"));
            return type.cast(retorno);
        }catch(Exception e){
            if(disparaErroCast)
                throw e;
            else
                return null;
        }
    }
    
    public String getHeader(String nome){
        return req.getHeader(nome);
    }
    
    public <T> T getHeader(String nome, Class<T> type){
        return getHeader(nome, type, false);
    }
    
    /**
     * Retorna o parâmetro da URL já casteado no formato especificado
     *
     * @param nome nome do parâmetro
     * @param type Tipo de retorno
     * @param disparaErroCast Indica se deve disparar erro de cast, se for false, retorna null em caso de erro
     * @return Valor do parâmetro da URL já casteado
     */
    public <T> T getHeader(String nome, Class<T> type, boolean disparaErroCast){
        Object retorno = getHeader(nome);
        if(retorno == null) return null;
        try{
            if(type == Integer.class)
                return type.cast(new Integer((String) retorno));
            if(type == Double.class)
                return type.cast(new Double((String) retorno));
            if(type == String.class)
                return type.cast(retorno);
            if(type == Boolean.class)
                return type.cast(retorno.equals("true"));
            return type.cast(retorno);
        }catch(Exception e){
            if(disparaErroCast)
                throw e;
            else
                return null;
        }
    }
    
    /**
     * Converte essa input em uma input de outro tipo, com a mesma transação, response e parâmetros e etc dessa input.
     *
     * @param tipo da nova input
     * @return A nova input convertida
     */
    
    
    /*public HttpServletRequest req;
    public HwResponse resp;
    public Integer idUsuAutenticado;
    private final Map<String,Object> parametros = new HashMap<>();
    public String authId;
    public String nomeMetodo;*/
    
    @SuppressWarnings("unchecked")
    public <T extends MethodInput> T cloneTo(Class<T> tipo) {
        try {
            MethodInput retorno = tipo.newInstance();
            retorno.req = req;
            retorno.resp = resp;
            retorno.idUsuAutenticado = idUsuAutenticado;
            retorno.parametros = parametros;
            retorno.authId = authId;
            retorno.nomeMetodo = nomeMetodo;
            putAttrsInDaosProvider(retorno);
            
            return (T)retorno;
        } catch (Exception ex) {
            return null;
        }
    }
    
    public static <T extends MethodInput> T fromJson(String json, Class<T> type, HttpServletRequest req, String paramName, Object pathParam){
        if(json == null || json.isEmpty()) try {
            T ret = type.newInstance();
            ret.parametros = new HashMap<>();
            if(paramName != null) {
                ret.parametros.put(paramName, pathParam);
            }
            ret.req = req;
            return ret;
        } catch (Exception ex){
            throw new RuntimeException("Erro ao instanciar input");
        }
        T retorno;
        try{
            retorno = JsonManager.getGson().fromJson(json, type);
        } catch (Exception e){
            return null;
        }
        if(retorno == null) return null;
        retorno.req = req;
        retorno.parametros = new HashMap<>();
        if(paramName != null) retorno.parametros.put(paramName, pathParam);
        retorno.filtraCampos();
        return retorno;
    }
    
    // Nesse método fazer a filtragem dos grupos de cada um dos campos, com base na regra de negócio
    protected abstract void filtraCampos();
    // Nesse método fazer a validação dos campos da input
    protected abstract void validaCampos(HwResponse res);
    // Nesse método retornar um identificador único para o método HTTP
    protected abstract String getMethodNamespace();

    
}
