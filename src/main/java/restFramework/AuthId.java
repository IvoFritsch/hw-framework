/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package restFramework;

import java.sql.ResultSet;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author Ivo
 */
public class AuthId {
    private String authId;
    private Integer idUsuario;
    private String ipCriador;
    private long validade;
    private long expiracao;
    private String params;
    private boolean mobile;
    

    public AuthId(String authId, Integer idUsuario, String ipCriador, long validade, boolean mobile) {
        this.authId = authId;
        this.idUsuario = idUsuario;
        this.ipCriador = ipCriador;
        this.validade = validade;
        expiracao = System.currentTimeMillis() + validade;
    }

    AuthId(ResultSet rs) {
        try{
            this.idUsuario = rs.getInt("idUsuario");
            this.authId = rs.getString("authId");
            this.ipCriador = rs.getString("ipCriador");
            this.validade = rs.getLong("validade");
            this.expiracao = rs.getLong("expiracao");
            this.params = rs.getString("extraParams");
            this.mobile = rs.getInt("mobile") != 0;
        }catch(Exception ex){
        }
    }
    
    public boolean equals(String authId, HttpServletRequest req) {
        if(authId == null) return false;
        if (!authId.equals(this.authId)) return false;
        if(!mobile && !ipCriador.equals(req.getRemoteAddr())) return false;
        if(validade < 1) return true;
        return true;
    }

    public String getAuthId() {
        return authId;
    }

    public Integer getIdUsuario() {
        return idUsuario;
    }

    public long getExpiracao() {
        return expiracao;
    }

    public long getValidade() {
        return validade;
    }
    
    public String getParams() {
        return params;
    }
}
