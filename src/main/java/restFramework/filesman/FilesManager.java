/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package restFramework.filesman;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import restFramework.Database;
import restFramework.FileInput;
import restFramework.RestApi;
import restFramework.Utils;

/**
 * Essa classe é responsável por fazer o gerenciamento, controle e compartilhamento dos arquivos dos usuários nos sistemas da Haftware.<br>
 * Sempre utilizar os métodos dessa classe para salvar os arquivos dos usuários, nunca salvar de outra maneira nos métodos da API.
 * 
 * @author Ivo
 */
public class FilesManager implements Runnable{
    private static String FILES_LOCATION;
    public FilesManager() throws Exception{
        if(RestApi.HW_PRODUCTION){
            FILES_LOCATION = "/var/lib/jetty/";
        } else {
            FILES_LOCATION = Utils.findHsqlmanDbLocation(RestApi.nomeProjeto);
        }
        if(FILES_LOCATION == null){
            throw new RuntimeException("O FilesManager não pôde inicializar pois não encontrou a localização do banco de dados.\n"
                    + "   Verifique se o HSQLDB Manager está rodando e se o banco '"+RestApi.nomeProjeto+"' está deployado.");
        }
        FILES_LOCATION += "managedFiles/";
        new File(FILES_LOCATION).mkdirs();
        System.out.println("Aguardando banco de dados...");
        try (Connection connect = Database.getNormalConnection()) {
            connect.createStatement().executeUpdate("CREATE TABLE IF NOT EXISTS framework_filesControl ("
                    + "mainId integer, "
                    + "idPrimario varchar(100), "
                    + "idSecundario varchar(100), "
                    + "valid tinyint," // Indica se o arquivo ainda é valido para ser referenciado
                    + "size integer, "
                    + "serverFileName varchar(50), "
                    + "fileUrl varchar(50),"
                    + "originalName varchar(100),"
                    + "originalExtension varchar(10),"
                    + "uploadDate timestamp(2),"
                    + "provider tinyint," //   0 - Localmente   /   1 - Cloudinary
                    + "fileId INTEGER IDENTITY,"
                    + "CONSTRAINT PK_FilesControl_Framework PRIMARY KEY (mainId, idPrimario, idSecundario, valid, fileId));");
            // Cria o indice de URL, já que ele é frequentemente buscado
            connect.createStatement().executeUpdate("CREATE INDEX IF NOT EXISTS IDX_URL_FilesControl_Framework ON framework_filesControl (fileUrl);");
        } catch (Exception e){
            throw new RuntimeException("Não consegui acessar/criar a tabela de controle.");
        }
    }
    
    /**
     * Salva um novo arquivo no banco de arquivos e retorna se a operação foi OK.<br>
     * Após essa função retornar, o arquivo já foi salvo e pode ser acessado
     * 
     * @param mainId Id principal dono do arquivo
     * @param idPrimario Um identificador primario para esse arquivo.
     * @param idSecundario Um identificador secundário para esse arquivo.
     * @param file Arquivo a gravar
     * @param host Host onde salvar o arquivo
     * @return true se salvou OK, false caso contrário.
     */
    public boolean saveFile(Integer mainId, String idPrimario, String idSecundario, FileInput file, FilesHost host){
        if(file == null || !file.isValid()) return false;
        return saveFile(mainId, idPrimario, idSecundario, file.getNome(), file.getBase64(), host);
    }
    /**
     * Salva um novo arquivo no banco de arquivos e retorna se a operação foi OK.<br>
     * Após essa função retornar, o arquivo já foi salvo e pode ser acessado
     * 
     * @param mainId Id principal dono do arquivo
     * @param idPrimario Um identificador primario para esse arquivo.
     * @param idSecundario Um identificador secundário para esse arquivo.
     * @param base64 A string base64 referente ao conteúdo do arquivo.
     * @param nomeArquivo Nome original do arquivo
     * @param host Host onde salvar o arquivo
     * @return true se salvou OK, false caso contrário.
     */
    public boolean saveFile(Integer mainId, String idPrimario, String idSecundario, String nomeArquivo, String base64, FilesHost host){
        try(Connection connection = Database.getNormalConnection()){
            
            ManagedFile managedFile = leMFileBanco(mainId, idPrimario, idSecundario, connection);
            if(managedFile == null){
                // Arquivo não existe temos que criá-lo
                managedFile = new ManagedFile(mainId, idPrimario, idSecundario, nomeArquivo);
                int bytesGravados = gravaArquivo(managedFile, base64);
                managedFile.setSize(bytesGravados);
                if(bytesGravados >= 0){
                    gravaNovaMFileBanco(managedFile, connection);
                } else {
                    return false;
                }
            }else{
                PreparedStatement preparedStatement = 
                        connection.prepareStatement("UPDATE framework_filesControl SET "
                                                        + "valid = 0 "
                                                        + "WHERE mainId = ? "
                                                        + "AND idPrimario = ? "
                                                        + "AND idSecundario = ? ;");
                preparedStatement.setInt(1, mainId);
                preparedStatement.setString(2, idPrimario);
                preparedStatement.setString(3, idSecundario);
                preparedStatement.executeUpdate();
                
                managedFile.preparaRegravacao();
                managedFile.setOriginalName(nomeArquivo);
                int bytesGravados = gravaArquivo(managedFile, base64);
                managedFile.setSize(bytesGravados);
                if(bytesGravados >= 0){
                    gravaNovaMFileBanco(managedFile, connection);
                } else {
                    return false;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }
    
    public boolean deleteFile(Integer mainId, String idPrimario, String idSecundario){
        try(Connection connection = Database.getNormalConnection()){
            PreparedStatement preparedStatement = 
                    connection.prepareStatement("UPDATE framework_filesControl SET "
                                                    + "valid = 0 "
                                                    + "WHERE mainId = ? "
                                                    + "AND idPrimario = ? "
                                                    + "AND idSecundario = ? ;");
            preparedStatement.setInt(1, mainId);
            preparedStatement.setString(2, idPrimario);
            preparedStatement.setString(3, idSecundario);
            preparedStatement.executeUpdate();
            return true;
        } catch (Exception ex) {
            return false;
        }
    }
    
    /**
     * Retorna o arquivo gerenciado(ManagedFile) a partir dos seus 3 identificadores.
     *
     * @param mainId Id principal dono do arquivo
     * @param idPrimario Um identificador primario para esse arquivo.
     * @param idSecundario Um identificador secundário para esse arquivo.
     * @return A classe representando o arquivo Gerenciado
     */
    public ManagedFile getManagedFile(Integer mainId, String idPrimario, String idSecundario){
        try(Connection connection = Database.getNormalConnection()){
            ManagedFile mFile = leMFileBanco(mainId, idPrimario, idSecundario, connection);
            return mFile;
        } catch(Exception ex){
            return null;
        }
    }

    /**
     * Retorna a lista de arquivos gerenciados(ManagedFile) de um ID principal especifico.
     *
     * @param mainId Id principal a procurar os arquivos
     * @return A lista de arquivos gerenciados desse usuário.
     */    
    public List<ManagedFile> getMainIdFiles(Integer mainId){
        try(Connection connection = Database.getNormalConnection()){
            PreparedStatement preparedStatement = connection.prepareStatement(
                        "SELECT * FROM framework_filesControl WHERE mainId = ? AND valid = 1;");
            preparedStatement.setInt(1, mainId);
            ResultSet rs = preparedStatement.executeQuery();
            List<ManagedFile> retorno = new ArrayList<>();
            while(rs.next()){
                retorno.add(new ManagedFile(rs));
            }
            return retorno;
        } catch(Exception ex){
            return null;
        }
    }
    
    ManagedFile getMFileFromUrl(String url){
        try(Connection connection = Database.getNormalConnection()){
            ManagedFile mFile = leMFileBancoViaUrl(url,connection);
            return mFile;
        } catch(Exception ex){
            return null;
        }
    }
    
    static FileInputStream getMFileInputStream(ManagedFile mFile){
        File file = new File(FILES_LOCATION+mFile.getServerFileName());
        if(!file.exists()) return null;
        try {
            FileInputStream inStream = new FileInputStream(file);
            return inStream;
        } catch (FileNotFoundException ex) {}
        return null;
    }
    
//     private boolean atualizaMFileBanco(ManagedFile mFile, Connection conn){
//         PreparedStatement preparedStatement;
// //size   |   serverFileName   |   fileUrl   |   originalName   |   originalExtension   |   uploadDate   |   timesProvided   |
//         try {
//             preparedStatement = conn.prepareStatement("UPDATE framework_filesControl SET "
//                             + "size = ?, "
//                             + "serverFileName = ?, "
//                             + "fileUrl = ?, "
//                             + "originalName = ?, "
//                             + "originalExtension = ?, "
//                             + "uploadDate = ? "
//                             + "WHERE mainId = ? "
//                             + "AND idPrimario = ? "
//                             + "AND idSecundario = ? "
//                             + "AND valid = ?;");
//             preparedStatement.setInt(1, mFile.getSize());
//             preparedStatement.setString(2, mFile.getServerFileName());
//             preparedStatement.setString(3, mFile.getFileUrlInternal());
//             preparedStatement.setString(4, mFile.getOriginalName());
//             preparedStatement.setString(5, mFile.getOriginalExtension());
//             preparedStatement.setDate(6, new java.sql.Date(mFile.getUploadDate().getTime()));
//             preparedStatement.setInt(7, mFile.getMainId());
//             preparedStatement.setString(8, mFile.getIdPrimario());
//             preparedStatement.setString(9, mFile.getIdSecundario());
//             preparedStatement.setInt(10, mFile.getValid() ? 1 : 0);
//             preparedStatement.executeUpdate();
//         } catch (SQLException ex) {
//             return false;
//         }
//         return true;
//     }
    
    private boolean gravaNovaMFileBanco(ManagedFile mFile, Connection conn){
        PreparedStatement preparedStatement;
//size   |   serverFileName   |   fileUrl   |   originalName   |   originalExtension   |   uploadDate   |   timesProvided   |
        try {
            // MAINID  IDPRIMARIO  IDSECUNDARIO  VALID  SIZE  SERVERFILENAME  FILEURL  ORIGINALNAME  ORIGINALEXTENSION  UPLOADDATE  PROVIDER
            preparedStatement = conn.prepareStatement("INSERT INTO framework_filesControl"
                     + "(mainId, " +
                        "idPrimario, " +
                        "idSecundario, " +
                        "valid, " +
                        "size, " +
                        "serverFileName, " +
                        "fileUrl, " +
                        "originalName, " +
                        "originalExtension, " +
                        "uploadDate, " +
                        "provider) values ("
                            + "?, "
                            + "?, "
                            + "?, "
                            + "?, "
                            + "?, "
                            + "?, "
                            + "?, "
                            + "?, "
                            + "?, "
                            + "?, "
                            + "?);");
            preparedStatement.setInt(1, mFile.getMainId());
            preparedStatement.setString(2, mFile.getIdPrimario());
            preparedStatement.setString(3, mFile.getIdSecundario());
            preparedStatement.setInt(4, mFile.getValid() ? 1 : 0);
            preparedStatement.setInt(5, mFile.getSize());
            preparedStatement.setString(6, mFile.getServerFileName());
            preparedStatement.setString(7, mFile.getFileUrlInternal());
            preparedStatement.setString(8, mFile.getOriginalName());
            preparedStatement.setString(9, mFile.getOriginalExtension());
            preparedStatement.setDate(10, new java.sql.Date(mFile.getUploadDate().getTime()));
            preparedStatement.setInt(11, 0);
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }
    
    private ManagedFile leMFileBanco(Integer mainId, String idPrimario, String idSecundario, Connection conn){
        try{
        PreparedStatement preparedStatement = conn.prepareStatement(
                        "SELECT * FROM framework_filesControl WHERE mainId = ? "
                                                + "AND idPrimario = ? "
                                                + "AND idSecundario = ? "
                                                + "AND valid = 1;",ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setInt(1, mainId);
            preparedStatement.setString(2, idPrimario);
            preparedStatement.setString(3, idSecundario);
            ResultSet resultado = preparedStatement.executeQuery();
            resultado.last();
            if(resultado.getRow() == 0) 
                return null;
            return new ManagedFile(resultado);
        }catch(Exception ex){
            return null;
        }
    }
    private ManagedFile leMFileBancoViaUrl(String url, Connection conn){
        try{
        PreparedStatement preparedStatement = conn.prepareStatement(
                        "SELECT * FROM framework_filesControl WHERE fileUrl = ? AND valid = 1;",ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setString(1, url);
            ResultSet resultado = preparedStatement.executeQuery();
            resultado.last();
            if(resultado.getRow() == 0) 
                return null;
            return new ManagedFile(resultado);
        }catch(Exception ex){
            return null;
        }
    }
    
    private int gravaArquivo(ManagedFile mFile, String base64){
        byte[] decode = Base64.getDecoder().decode(base64);
        try (FileOutputStream fos = new FileOutputStream(FILES_LOCATION+mFile.getServerFileName())) {
            fos.write(decode);
         }catch(Exception ex){
             ex.printStackTrace();
             return -1;
         }
        return decode.length;
    }
    
    private void deletaArquivo(ManagedFile mFile){
        mFile.setDeleted(false);
        boolean delete = new File(FILES_LOCATION+mFile.getServerFileName()).delete();
        if(delete) mFile.setDeleted(true);
    }
    
    
    private void deletaMFileBanco(ManagedFile mFile, Connection conn){
        PreparedStatement preparedStatement;
//size   |   serverFileName   |   fileUrl   |   originalName   |   originalExtension   |   uploadDate   |   timesProvided   |
        try {
            preparedStatement = conn.prepareStatement("DELETE FROM framework_filesControl WHERE "
                            + "mainId = ? "
                            + "AND idPrimario = ? "
                            + "AND idSecundario = ? "
                            + "AND valid = ?;");
            preparedStatement.setInt(1, mFile.getMainId());
            preparedStatement.setString(2, mFile.getIdPrimario());
            preparedStatement.setString(3, mFile.getIdSecundario());
            preparedStatement.setInt(4, mFile.getValid() ? 1 : 0);
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
        }
    }

    public List<ManagedFile> getInvalidFiles(Connection conn){
        try{
            PreparedStatement preparedStatement = conn.prepareStatement(
                        "SELECT * FROM framework_filesControl WHERE valid = 0;");
            ResultSet rs = preparedStatement.executeQuery();
            List<ManagedFile> retorno = new ArrayList<>();
            while(rs.next()){
                retorno.add(new ManagedFile(rs));
            }
            return retorno;
        } catch(Exception ex){
            return null;
        }
    }

    /**
     * Task automática que remove arquivos que não são mais válidos
     *
     */  
    @Override
    public void run() {
        try(Connection connection = Database.getNormalConnection()){
            List<ManagedFile> invalidFiles = getInvalidFiles(connection);
            invalidFiles.forEach(mf -> deletaArquivo(mf));
            
            invalidFiles.stream().filter(mf -> mf.isDeleted()).forEach(mf -> {
                deletaMFileBanco(mf, connection);
            });
        } catch (Exception e){
            e.printStackTrace();
        }
    }
    
}

