/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package restFramework.filesman;

import java.io.InputStream;
import java.util.Date;
import java.sql.ResultSet;
import java.util.UUID;
import restFramework.RestApi;

/**
 * Representa um arquivo gerenciado pelo Haftware REST Framework.
 * 
 * @author Ivo
 */
public final class ManagedFile {
    private Integer mainId;
    private String idPrimario;
    private String idSecundario;
    private Boolean valid;
    private Integer size;
    private String serverFileName;
    private String fileUrl;
    private String originalName;
    private String originalExtension;
    private Date uploadDate;
    
    private boolean deleted;

    ManagedFile(ResultSet rs) {
        try{
            this.mainId = rs.getInt("mainId");
            this.idPrimario = rs.getString("idPrimario");
            this.idSecundario = rs.getString("idSecundario");
            this.valid = rs.getInt("valid") != 0;
            this.size = rs.getInt("size");
            this.serverFileName = rs.getString("serverFileName");
            this.fileUrl = rs.getString("fileUrl");
            this.originalName = rs.getString("originalName");
            this.originalExtension = rs.getString("originalExtension");
            this.uploadDate = rs.getDate("uploadDate");
        }catch(Exception ex){}
    }

    InputStream getInputStream(){
        return FilesManager.getMFileInputStream(this);
    }
    
    public ManagedFile(Integer mainId, String idPrimario, String idSecundario, String nomeArquivo) {
        this.mainId = mainId;
        this.idPrimario = idPrimario;
        this.idSecundario = idSecundario;
        this.valid = true;
        this.serverFileName = UUID.randomUUID().toString();
        this.fileUrl = UUID.randomUUID().toString();
        setOriginalName(nomeArquivo);
        this.uploadDate = new Date();
    }
    
    public void preparaRegravacao(){
        this.serverFileName = UUID.randomUUID().toString();
        this.fileUrl = UUID.randomUUID().toString();
        this.uploadDate = new Date();
        this.valid = true;
    }

    public void setOriginalName(String nomeArquivo){
        String[] splitNome = nomeArquivo.split("[.]");
        this.originalExtension = splitNome[splitNome.length - 1];
        this.originalName = nomeArquivo.replace("."+originalExtension, "");
    }
    
    /**
     * Retorna a URL de acesso externo à esse arquivo gerenciado.
     *
     * @param disposition A disposition do arquivo, basicamente, se vai ser baixado(DOWNLOAD) ou inline(INLINE)
     * @return URL para acesso ao arquivo
     */
    public String getFileUrl(FileDisposition disposition) {
        return RestApi.backendBaseUrl+"filesman/"+this.fileUrl+"?d="+disposition;
    }
    
    String getFileUrlInternal() {
        return this.fileUrl;
    }
    
    
    
    public String getIdPrimario() {
        return idPrimario;
    }

    public void setValid(Boolean valid) {
        this.valid = valid;
    }
    
    public String getIdSecundario() {
        return idSecundario;
    }

    public Integer getMainId() {
        return mainId;
    }

    public Boolean getValid() {
        return valid;
    }

    public void setServerFileName(String serverFileName) {
        this.serverFileName = serverFileName;
    }
    
    public String getOriginalExtension() {
        return originalExtension;
    }

    public String getOriginalName() {
        return originalName;
    }

    public Integer getSize() {
        return size;
    }

    public Date getUploadDate() {
        return uploadDate;
    }

    public String getServerFileName() {
        return serverFileName;
    }

    public void setUploadDate(Date uploadDate) {
        this.uploadDate = uploadDate;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public boolean isDeleted() {
        return deleted;
    }
}
