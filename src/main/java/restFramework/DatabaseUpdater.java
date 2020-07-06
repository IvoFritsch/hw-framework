/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package restFramework;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.ServletContext;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author Ivo
 */
public final class DatabaseUpdater implements Closeable {
    
    ServletContext ctx;
    Connection conn;
    final int LAST_VERSION;

    public DatabaseUpdater(ServletContext ctx) throws Exception {
        this.ctx = ctx;
        conn = Database.getNormalConnection();
        conn.createStatement().executeUpdate("SET DATABASE TRANSACTION CONTROL MVCC;");
        conn.createStatement().executeUpdate("CREATE CACHED TABLE IF NOT EXISTS framework_dbVersionsControl ("
            + "version integer,"
            + "tableName varchar(50),"
            + "descriptor varchar(1000),"
        + ");");
        LAST_VERSION = getLastVersion();
        Map<String, TableDescriptor> oldDescriptors = getVersionDescriptors(LAST_VERSION);
        Map<String, TableDescriptor> newDescriptors = getNewDescriptors();
        System.out.println("   - A versão atual do banco possui "+ oldDescriptors.size() + " tabelas...");
        if(newDescriptors.size() != oldDescriptors.size())
            System.out.println("   - A nova versão passará a possuir "+ newDescriptors.size() + "...");
        System.out.println("   - Processando diferenças em cada tabela...");
        
        
        newDescriptors.forEach((n, d) -> d.createUpdatePlan(oldDescriptors.get(n)));
        System.out.println("   - Executando SQLs de conversão...");
        for(TableDescriptor d : newDescriptors.values()){
            executaCreationPlans(d);
        }
        for(TableDescriptor d : newDescriptors.values()){
            executaFksCreationPlans(d);
        }
        System.out.println("   - Gravando nova versão no banco...");
        int NEW_VERSION = LAST_VERSION + 1;
        for(TableDescriptor d : newDescriptors.values()){
            gravaDescriptorNoBanco(NEW_VERSION, d);
        }
        System.out.println("   - Pronto...");
        
    }
    
    private void executaCreationPlans(TableDescriptor d){
        d.creationPlans.forEach(sql -> {
            try {
                System.out.println("Creation plan: \n"+sql);
                conn.createStatement().executeUpdate(sql);
            } catch (SQLException ex) {
                System.err.println("   - Erro ao executar query: \n"
                        + "			 "+sql);
                ex.printStackTrace();
            }
        });
    }
    
    private void executaFksCreationPlans(TableDescriptor d){
        d.fksCreationPlans.forEach(sql -> {
            try {
                System.out.println("FK Creation plan: \n"+sql);
                conn.createStatement().executeUpdate(sql);
            } catch (SQLException ex) {
                System.err.println("   - Erro ao executar query: \n"
                        + "			 "+sql);
                ex.printStackTrace();
            }
        });
    }
    
    private void gravaDescriptorNoBanco(int version, TableDescriptor descriptor) throws SQLException{
        try (PreparedStatement preparedStatement = conn.prepareStatement(
                "INSERT INTO framework_dbVersionsControl (version, tableName, descriptor) values (?, ?, ?);"
        )) {
            preparedStatement.setInt(1, version);
            preparedStatement.setString(2, descriptor.name);
            preparedStatement.setString(3, descriptor.json.toString());
            preparedStatement.executeUpdate();
        }
    }
    
    private Map<String, TableDescriptor> getVersionDescriptors(int version) throws SQLException{
        Map<String, TableDescriptor> ret = new HashMap<>();
        List<String> versionTables = getVersionTables(version);
        for (String t : versionTables) {
            ret.put(t, getTableDescriptorOnVersion(version, t));
        }
        return ret;
    }
    
    private List<String> getVersionTables(int version) throws SQLException{
        PreparedStatement preparedStatement = conn.prepareStatement(
            "SELECT tableName FROM framework_dbVersionsControl WHERE version = ?;"
        );
        preparedStatement.setInt(1, version);
        ResultSet rs = preparedStatement.executeQuery();
        List<String> ret = new ArrayList<>();
        while(rs.next()){
            ret.add(rs.getString("tableName"));
        }
        return ret;
    }
    
    private TableDescriptor getTableDescriptorOnVersion(int version, String tableName) throws SQLException{
        PreparedStatement preparedStatement = conn.prepareStatement(
            "SELECT descriptor FROM framework_dbVersionsControl WHERE version = ? AND tableName = ?;"
        );
        preparedStatement.setInt(1, version);
        preparedStatement.setString(2, tableName);
        ResultSet rs = preparedStatement.executeQuery();
        
        while(rs.next()){
            return new TableDescriptor(tableName, new JSONObject(rs.getString("descriptor")));
        }
        return null;
    }
    
    
    private int getLastVersion() throws SQLException{
        PreparedStatement preparedStatement = conn.prepareStatement(
            "SELECT distinct(version) FROM framework_dbVersionsControl ORDER BY version;"
        );
        ResultSet rs = preparedStatement.executeQuery();
        int lastVersion = 0;
        while(rs.next()){
            lastVersion = rs.getInt("version");
        }
        return lastVersion;
    }
    
    private Map<String, TableDescriptor> getNewDescriptors() throws Exception {
        Set<String> createTables = ctx.getResourcePaths("/WEB-INF/dbDescriptors/");
        Map<String, TableDescriptor> ret = new HashMap<>();
        createTables.forEach((String p) -> {
            try(InputStream is = ctx.getResourceAsStream(p)) {
                StringWriter writer = new StringWriter();
                IOUtils.copy(is, writer, "UTF-8");
                String json = writer.toString();
                String tableName = new File(p).getName().replace(".json", "");
                ret.put(tableName, new TableDescriptor(tableName, new JSONObject(json)));
            } catch(Exception e){
                e.printStackTrace();
            }
        });
        return ret;
    }

    @Override
    public void close() throws IOException {
        try{
            conn.close();
        } catch (SQLException ex){
            throw new IOException(ex);
        }
    }
    
    class TableDescriptor{
        JSONObject json;
        Map<String, Col> cols = new HashMap<>();
        List<Col> orderedCols = new ArrayList<>();
        String primaryKeysDeclaration;
        String name;
        List<Idx> idx = new ArrayList<>();
        List<Fk> fks = new ArrayList<>();
        
        List<String> creationPlans = new ArrayList<>();
        List<String> dropPlans = new ArrayList<>();
        List<String> fksCreationPlans = new ArrayList<>();
        
        public TableDescriptor(String name,JSONObject desc) {
            json = desc;
            this.name = name;
            JSONArray colsArr = desc.getJSONArray("cols");
            for (Object object : colsArr) {
                Col col = new Col((JSONObject) object);
                cols.put(col.name, col);
                orderedCols.add(col);
            }
            primaryKeysDeclaration = desc.getString("primaryKeysDeclaration");
            JSONArray idxArr = desc.getJSONArray("idx");
            idxArr.forEach(i -> idx.add(new Idx(name, (String) i)));
            JSONArray fksArr = desc.getJSONArray("fks");
            fksArr.forEach(i -> fks.add(new Fk(name, (JSONObject) i)));
        }

        private String getCreateTable() {
            StringBuilder sb = new StringBuilder("CREATE CACHED TABLE ");
            sb.append(name);
            sb.append(" (");
            sb.append(orderedCols.stream().map(String::valueOf).collect(Collectors.joining(", ", "", ", ")));
            sb.append(" PRIMARY KEY ( ");
            sb.append(primaryKeysDeclaration);
            sb.append(" ));");
            return sb.toString();
        }
        
        public void createUpdatePlan(TableDescriptor from){
            // Caso a tabela inteira está sendo criada
            if(from == null){
                System.out.println("      - Tabela " + name + " será criada...");
                creationPlans.add(getCreateTable());
                idx.forEach(i -> {
                    System.out.println(i.toString());
                    creationPlans.add(i.toString());
                });
                fks.forEach(f -> {
                    System.out.println(f.toString());
                    fksCreationPlans.add(f.toString());
                });
                return;
            }
            System.out.println("      - Tabela " + name + "...");
            List<Col> colsToCreate = new ArrayList<>();
            cols.forEach((n, c) -> {
                if(!from.cols.containsKey(n)) {
                    colsToCreate.add(c);
                }
            });
            List<Idx> idxsToCreate = new ArrayList<>();
            idx.forEach(i -> {
                
                if(!from.idx.contains(i)) {
                    idxsToCreate.add(i);
                }
            });
            List<Col> colsToDrop = new ArrayList<>();
            from.cols.forEach((n, c) -> {
                if(!cols.containsKey(n)) {
                    colsToDrop.add(c);
                }
            });
            
            if(colsToCreate.isEmpty() && colsToDrop.isEmpty()){
                System.out.println("            - Essa tabela não foi modificada.");
            } else {
                if(!colsToCreate.isEmpty()){
                    System.out.println("            - Serão criadas as colunas:");
                    colsToCreate.forEach(c -> System.out.println("               - "+c.toString()));
                }
                if(!colsToDrop.isEmpty()){
                    System.out.println("            - Serão dropadas as colunas:");
                    colsToDrop.forEach(c -> System.out.println("               - "+c.toString()));
                }
            }
            colsToCreate.forEach(c -> creationPlans.add("ALTER TABLE " + name + " ADD " + c.toString() + ";"));
            idxsToCreate.forEach(i -> creationPlans.add(i.toString()));
            colsToDrop.forEach(c -> dropPlans.add("ALTER TABLE " + name + " DROP COLUMN " + c.name + ";"));
        }
        
    }
    
    class Col {

        String name;
        String type;
        boolean isAutoGenerated = false;
        
        public Col(JSONObject col) {
            name = col.getString("name");
            type = col.getString("type");
            if(col.has("isAutoGenerated") && col.getBoolean("isAutoGenerated")) isAutoGenerated = true;
        }

        @Override
        public String toString() {
            return name + " " + type + (isAutoGenerated ? " GENERATED BY DEFAULT AS IDENTITY(START WITH 1, INCREMENT BY 1)" : "");
        }

        @Override
        public boolean equals(Object obj) {
            if(obj == null) return false;
            Col compare = (Col) obj;
            return Objects.equals(compare.name, name);
        }
    }
    
    class Idx{
    
        String field;
        String tableName;

        public Idx(String tableName, String field) {
            this.field = field;
            this.tableName = tableName;
        }

        @Override
        public String toString() {
            return "CREATE INDEX IF NOT EXISTS IDX_"+ tableName +"_"+ field +" ON  "+ tableName +" ("+ field +");";
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
            final Idx other = (Idx) obj;
            if (!Objects.equals(this.field, other.field)) {
                return false;
            }
            if (!Objects.equals(this.tableName, other.tableName)) {
                return false;
            }
            return true;
        }

        
    }
    
    class Fk {

        String tableName;
        String field;
        String references;
        
        public Fk(String tableName, JSONObject col) {
            this.tableName = tableName;
            field = col.getString("field");
            references = col.getString("references");
        }

        @Override
        public String toString() {
            return "ALTER TABLE "+ tableName +" ADD FOREIGN KEY (" + field + ") REFERENCES " + references + " ON DELETE CASCADE;";
        }
    }
}
