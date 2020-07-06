/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package restFramework;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 * @author Ivo
 */
public class DaosProvider implements AutoCloseable {
    
    boolean transacional = false;
    private Connection connection = null;
    public boolean precisaCommitar = false;
    private Map<Class<?>, Dao> daosProvidos;
    private boolean wasConverted = false;
    
    
    // Variáveis usadas no controle de locks
    private static int maxLocksPerKey = 600;
    private static final LongAdder qtdLocks = new LongAdder();
    private static final Map<LockKey, Lock> locks = new ConcurrentHashMap<>();
    private final List<Lock> acquiredLocks = new ArrayList<>();
    
    public List<DaosProvider> convertedTo;
    
    public <T extends Dao> T getDao(Class<T> tipo){
        if(daosProvidos == null)
            daosProvidos = new HashMap<>();
        if(!daosProvidos.containsKey(tipo)){
            if(!transacional && connection == null){
                connection = Database.getTransaction();
            }
            try {
                Dao dao = tipo.newInstance();
                dao.setTransaction(this.connection);
                dao.setInstanciador(this);
                daosProvidos.put(tipo, dao);
            } catch (Exception ex) {
                throw new RuntimeException("Erro ao instanciar Dao "+tipo);
            }
        }
        return tipo.cast(daosProvidos.get(tipo));
    }
    
    public void setConnection(Connection connection) {
        this.connection = connection;
    }
    
    public Connection getConnection(){
        if (this.connection != null) {
            return this.connection;
        }
        throw new RuntimeException("Tentou obter uma conexão não aberta");
    }
    
    public boolean precisaCommitar(){
        if(precisaCommitar) return true;
        if(convertedTo == null || convertedTo.isEmpty()) return precisaCommitar;
        return convertedTo.stream().anyMatch((d) -> (d.precisaCommitar()));
    }
    
    @Override
    public void close() throws Exception {
        try {
            if(!transacional && connection != null){
                connection.rollback();
                connection.close();
                connection = null;
            }
        } catch(Exception e){
            throw e;
        } finally {
            for(int x = acquiredLocks.size() - 1; x >= 0; x--){
                acquiredLocks.get(x).unlock();
            }
        }
    }
    
    protected void putAttrsInDaosProvider(DaosProvider c){
        if(convertedTo == null) convertedTo = new ArrayList<>();
        c.transacional = transacional;
        c.connection = connection;
        c.daosProvidos = daosProvidos;
        c.wasConverted = true;
        convertedTo.add(c);
    }
    
    public boolean wasConverted(){
        return wasConverted;
    }
    
    private Lock getLock(String key, Integer id){
        return locks.computeIfAbsent(new LockKey(key, id), k -> {
            qtdLocks.increment();
            return new ReentrantLock();
        });
    }
    
    /**
     * Bloqueia um recurso generico, todos os métodos que também tentarem bloquear esse recurso ficarão esperando até que ele seja liberado
     * 
     * @param key Chave do recurso
     */
    public void lock(String key){
        lock(key, 0);
    }
    
    /**
     * Bloqueia um recurso, todos os métodos que também tentarem bloquear esse recurso ficarão esperando até que ele seja liberado
     * 
     * @param key Chave do recurso
     * @param id identificador
     */
    public void lock(String key, Integer id){
        Lock lock = getLock(key, id);
        lock.lock();
        acquiredLocks.add(lock);
    }
    
    public static long qtdLocks(){
        return qtdLocks.longValue();
    }

    public static void setMaxLocksPerKey(int maxLocksPerKey) {
        DaosProvider.maxLocksPerKey = maxLocksPerKey;
    }
    
    /**
     * Faz rollback nas alterações do banco de dados e termina a execução do método imediatamente.
     * 
     */
    public void rollback(){
        throw new RollbackException();
    }
    
    private class LockKey{
        private String keyStr;

        public LockKey(String key, Integer id) {
            keyStr = key + "|" + (id % maxLocksPerKey);
        }

        @Override
        public int hashCode() {
            return keyStr.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final LockKey other = (LockKey) obj;
            return other.keyStr.equals(this.keyStr);
        }
        
        
    }
}
