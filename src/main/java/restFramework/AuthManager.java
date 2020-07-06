/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package restFramework;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author Ivo
 */
public class AuthManager implements Runnable {
    // Validade dos ids de autenticação, em ms (Default: 7 dias)
    public static long VALIDADE_AUTHIDS = 7 * TimeConstants.DAY;
    // Validade dos ids de autenticação, em ms (Default: 180 dias)
    public static long VALIDADE_AUTHIDS_MOBILE = 180 * TimeConstants.DAY;

    public AuthManager() {
        try (Connection connect = Database.getNormalConnection()) {
            connect.createStatement().executeUpdate("CREATE TABLE IF NOT EXISTS framework_authControl ("
                    + "idAuthId integer IDENTITY,"
                    + "authId varchar(50), "
                    + "idUsuario integer, "
                    + "ipCriador varchar(30), "
                    + "validade bigint,"
                    + "expiracao bigint, "
                    + "extraParams varchar(1000), "
                    + "mobile tinyint, "
                    + "CONSTRAINT PK_AuthControl_Framework PRIMARY KEY (idAuthId));");
            // Cria os indices que são mais frequentemente buscados
            connect.createStatement().executeUpdate("CREATE INDEX IF NOT EXISTS IDX_AuthId_AuthControl_Framework ON framework_authControl (authId);");
            connect.createStatement().executeUpdate("CREATE INDEX IF NOT EXISTS IDX_Usuario_AuthControl_Framework ON framework_authControl (idUsuario);");
        } catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException("Não consegui acessar/criar a tabela de controle.");
        }
        
    }
    
    AuthInfo authIdValido(String verificar, HttpServletRequest req) {
        try(Connection connection = Database.getNormalConnection()){
            PreparedStatement preparedStatement = connection.prepareStatement(
                        "SELECT * FROM framework_authControl WHERE (authId = ?) AND (validade = 0 OR expiracao > ?);");
            preparedStatement.setString(1, verificar);
            preparedStatement.setLong(2, System.currentTimeMillis());
            ResultSet rs = preparedStatement.executeQuery();
            List<AuthId> retorno = new ArrayList<>();
            while(rs.next()){
                retorno.add(new AuthId(rs));
            }
            if(retorno.isEmpty()) return null;
            AuthId check = retorno.get(0);
            if(!check.equals(verificar, req)) return null;
            long now = System.currentTimeMillis();
            
            if((check.getExpiracao() - now) < (check.getValidade() / 7)){
                preparedStatement = connection.prepareStatement(
                            "UPDATE framework_authControl SET expiracao = ? + validade WHERE authId = ?;");
                preparedStatement.setLong(1, now);
                preparedStatement.setString(2, verificar);
                preparedStatement.executeUpdate();
            }
            return new AuthInfo(verificar, check.getIdUsuario(), check.getParams());
        } catch(Exception ex){
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * Cria e vincula um novo authId(não mobile) ao usuário.
     * 
     * @param idUsuario ID do usuário à vincular
     * @param req O HttpServletRequest recebido na input
     * @return O authId gerado
     */
    public String novoAuthId(Integer idUsuario, HttpServletRequest req) {
        return novoAuthId(new AuthInfo(idUsuario), req, false);
    }

    /**
     * Cria e vincula um novo authId(não mobile) ao usuário.
     * 
     * @param authInfo authInfo a gravar
     * @param req O HttpServletRequest recebido na input
     * @return O authId gerado
     */
    public String novoAuthId(AuthInfo authInfo, HttpServletRequest req) {
        return novoAuthId(authInfo, req, false);
    }

    /**
     * Cria e vincula um novo authInfo ao usuário.
     * 
     * @param authInfo authInfo a gravar
     * @param req O HttpServletRequest recebido na input
     * @param mobile Indica se esse authInfo é mobile, fazendo com que a validação de IP seja ignorado
     * @return O authInfo gerado
     */
    public String novoAuthId(AuthInfo authInfo, HttpServletRequest req, boolean mobile) {
        
        AuthId authId = new AuthId(AuthManager.geraAuthId(), authInfo.idUsuario, req.getRemoteAddr(), mobile ? VALIDADE_AUTHIDS_MOBILE : VALIDADE_AUTHIDS, mobile);
        try(Connection connection = Database.getNormalConnection()){
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO framework_authControl(authId, idUsuario, ipCriador, validade, expiracao, extraParams, mobile) VALUES ("
                            + "?, "
                            + "?, "
                            + "?, "
                            + "?, "
                            + "?, "
                            + "?, "
                            + "?);");
            preparedStatement.setString(1, authId.getAuthId());
            preparedStatement.setInt(2, authId.getIdUsuario());
            preparedStatement.setString(3, req.getRemoteAddr());
            preparedStatement.setLong(4, mobile ? VALIDADE_AUTHIDS_MOBILE : VALIDADE_AUTHIDS);
            preparedStatement.setLong(5, authId.getExpiracao());
            preparedStatement.setString(6, authInfo.params == null ? "{}" : authInfo.params.toString());
            preparedStatement.setInt(7, mobile ? 1 : 0);
            preparedStatement.executeUpdate();
        } catch(Exception ex){
            ex.printStackTrace();
            return null;
        }
        return authId.getAuthId();
    }

    /**
     * Força a invalidação de um authId
     * 
     * @param authId authId a invalidar
     */
    public void invalidaAuthId(String authId) {
        try(Connection connection = Database.getNormalConnection()){
            PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM framework_authControl where authId = ?;");
            preparedStatement.setString(1, authId);
            preparedStatement.executeUpdate();
        } catch(Exception ex){
            ex.printStackTrace();
        }
    }
    
    private static String geraAuthId(){
        return UUID.randomUUID().toString();
    }

    /**
     * Define a validade dos authIds, em millisegundos
     * 
     * @param validade validade em millisegundos(0 = ilimitado)
     */
    public static void setValidadeAuthIds(long validade) {
        AuthManager.VALIDADE_AUTHIDS = validade;
    }
    
    // O método run do AuthManager serve para limpar authIds que não são mais válidos
    @Override
    public void run() {
        try(Connection connection = Database.getNormalConnection()){
            PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM framework_authControl where (validade > 0 AND expiracao < ?);");
            preparedStatement.setLong(1, System.currentTimeMillis());
            preparedStatement.executeUpdate();
        } catch(Exception ex){
            ex.printStackTrace();
        }
    }
    
}
