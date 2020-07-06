/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package restFramework.filesman;

/**
 * Serve para indicar ao FilesManager onde o arquivo deve ser salvo
 *
 * @author Ivo
 */
public enum FilesHost {
    LOCAL(0), CLOUDINARY(1);

    private final int id;
    FilesHost(int id) { this.id = id; }
    public int getValue() { return id; }
    
}
