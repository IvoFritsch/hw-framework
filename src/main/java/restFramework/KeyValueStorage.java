/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package restFramework;

import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 *
 * @author Ivo
 */
public class KeyValueStorage {

    public KeyValueStorage() {
        try (Connection connect = Database.getNormalConnection()) {
            connect.createStatement().executeUpdate("CREATE TABLE IF NOT EXISTS framework_keyValueStorage ("
                    + "key varchar(50),"
                    + "value longvarchar, "
                    + "lastUpdate bigint );");
            connect.createStatement().executeUpdate("CREATE INDEX IF NOT EXISTS IDX_Key_KeyValueStorage_Framework ON framework_keyValueStorage (key);");
        } catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException("Não consegui acessar/criar a tabela de controle.");
        }
    }

    /**
     * Adiciona/atualiza uma chave no banco
     * 
     * @param key chave a adicionar/atualizar
     * @param value valor a adicionar/atualizar
     */
    public void put(String key, Object value) {
        synchronized(this){
            try(Connection connection = Database.getNormalConnection()){
                
                boolean containKey;
                try (PreparedStatement preparedStatement = connection.prepareStatement(
                    "SELECT COUNT(*) AS existe FROM framework_keyValueStorage WHERE key = ?;");) {
                        preparedStatement.setString(1, key);
                        try (ResultSet rs = preparedStatement.executeQuery()) {
                            rs.next();
                            containKey = rs.getInt("existe") > 0;
                        }
                        if(!(value instanceof String)){
                            value = JsonManager.getGson().toJson(value);
                        }
                }

                if(!containKey){
                    try (PreparedStatement preparedStatement = connection.prepareStatement(
                                            "INSERT INTO framework_keyValueStorage (key, value, lastUpdate) values (?,?,?);")) {
                        preparedStatement.setString(1, key);
                        preparedStatement.setString(2, (String) value);
                        preparedStatement.setLong(3, System.currentTimeMillis());
                        preparedStatement.executeUpdate();
                    }
                    
                } else {
                    try (PreparedStatement preparedStatement = connection.prepareStatement(
                                            "UPDATE framework_keyValueStorage SET value = ?, lastUpdate = ? WHERE key = ?;")) {
                        preparedStatement.setString(1, (String) value);
                        preparedStatement.setLong(2, System.currentTimeMillis());
                        preparedStatement.setString(3, (String) key);
                        preparedStatement.executeUpdate();
                    }
                    
                    
                }
            } catch(Exception ex){
                throw new RuntimeException(ex);
            }
        }
    }
    
    /**
     * Retorna o valor associado à chave
     * 
     * @param key Chave
     * @param type Tipo do valor
     * @return valor associado, ou null caso não exista
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Type type){
        String getted = this.get(key);
        if(getted == null) return null;
        return (T) JsonManager.getGson().fromJson(this.get(key), type);
    }
    
    /**
     * Retorna o valor associado à chave
     * 
     * @param key Chave
     * @param type Tipo do valor
     * @return valor associado, ou null caso não exista
     */
    public <T> T get(String key, Class<T> type){
        try(Connection connection = Database.getNormalConnection()){
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "SELECT value FROM framework_keyValueStorage WHERE key = ?;");
            preparedStatement.setString(1, key);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                boolean hasNext = rs.next();
                if(!hasNext) return null;
                String value = rs.getString("value");
                if(value == null) return null;
                if(type == Integer.class)
                    return type.cast(new Integer(value));
                if(type == Long.class)
                    return type.cast(new Long(value));
                if(type == Double.class)
                    return type.cast(new Double(value));
                if(type == String.class)
                    return type.cast(value);
                if(type == Boolean.class)
                    return type.cast(value.equals("true"));
                return JsonManager.getGson().fromJson(value, type);
            }
        } catch(Exception ex){
            throw new RuntimeException(ex);
        }
    }
    
    /**
     * Retorna o valor associado à chave
     * 
     * @param key Chave
     * @param type Tipo do valor
     * @param defaultValue Valor default caso o valor no banco seja null
     * @return valor associado, ou null caso não exista
     */
    public <T> T get(String key, Class<T> type, T defaultValue){
        T get = get(key, type);
        if(get == null) return defaultValue;
        return get;
    }
    
    /**
     * Retorna o valor associado à chave
     * 
     * @param key Chave
     * @param type Tipo do valor
     * @param defaultValue Valor default caso o valor no banco seja null
     * @return valor associado, ou null caso não exista
     */
    public <T> T get(String key, Type type, T defaultValue){
        T get = get(key, type);
        if(get == null) return defaultValue;
        return get;
    }
    
    /**
     * Retorna o valor associado à chave como String
     * 
     * @param key Chave
     * @return valor associado como String, ou null caso não exista
     */
    public String get(String key){
        return get(key, String.class);
    }
    
    /**
     * Remove uma chave do storage
     * 
     * @param key chave a remover
     */
    public void remove(String key){
        synchronized(this){
            try(Connection connection = Database.getNormalConnection(); 
                PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM framework_keyValueStorage WHERE key = ?;")
                ){
                preparedStatement.setString(1, key);
                preparedStatement.executeUpdate();
            } catch(Exception ex){
                throw new RuntimeException(ex);
            }
        }
    }
}
