/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package restFramework;

/**
 * Representa, numa input da API, um arquivo enviado pelo cliente
 *
 * @author Ivo
 */
public class FileInput {
    private String nome;
    private String base64;

    public String getBase64() {
        return base64;
    }

    public String getNome() {
        return nome;
    }
    
    public boolean isValid(){
        if(nome == null || nome.isEmpty() || nome.length() > 100) return false;
        if(base64 == null || base64.isEmpty()) return false;
        return true;
    }
    
    public boolean checkExtension(String... extensions){
        if(nome == null) return false;
        for (String ext : extensions) {
            if(nome.endsWith(ext)) return true;
        }
        return false;
    }
    
    /**
     * Retorna o tamanho aproximado do arquivo final em bytes.
     * 
     * @return tamanho aproximado do arquivo
     */
    public int getAproxFileSize(){
        if(!isValid()) return -1;
        return (int)(base64.length() * (3.0/4.0));
    }
}
