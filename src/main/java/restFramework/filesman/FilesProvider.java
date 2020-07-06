/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package restFramework.filesman;

import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.ServletContext;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import restFramework.RestApi;

/**
 * Esse é o Serlet responsavel por prover os arquivos gerenciados pelo framework aos clientes
 * 
 * @author Ivo
 */
@WebServlet(urlPatterns = "/filesman/*")
public class FilesProvider extends HttpServlet{  
  
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response){
        try{
        response.addHeader("Access-Control-Allow-Origin", "*");
        response.addHeader("Access-Control-Allow-Methods", "GET, OPTIONS");
        String requestURI = request.getRequestURI();
        String fileUrl = requestURI.substring(requestURI.lastIndexOf('/') + 1);
        FilesManager filesManager = RestApi.getFilesManager();
        if(filesManager == null){
            response.setStatus(HttpServletResponse.SC_NOT_ACCEPTABLE);
            return;
        }
        // Procura a ManagedFile referente à essa URL
        ManagedFile mFile = filesManager.getMFileFromUrl(fileUrl);
        if(mFile == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        OutputStream outStream;
        // if you want to use a relative path to context root:
        try (InputStream fileStream = mFile.getInputStream()) {
            // obtains ServletContext
            ServletContext context = getServletContext();
            // gets MIME type of the file
            String mimeType = context.getMimeType("a."+mFile.getOriginalExtension());
            if (mimeType == null) {
                // set to binary type if MIME mapping not found
                mimeType = "application/octet-stream";
            }   // modifies response
            response.setContentType(mimeType);
            response.setContentLength(mFile.getSize());
            response.setHeader("Cache-Control", "public, max-age=31536000");
            String disposition = request.getParameter("d");
            // forces download
            String headerKey = "Content-Disposition";
            String headerValue;
            if("INLINE".equals(disposition)){
                headerValue = "inline";
            } else {
                headerValue = String.format("attachment; filename=\"%s\"", mFile.getOriginalName()+"."+mFile.getOriginalExtension());
            }   response.setHeader(headerKey, headerValue);
            // obtains response's output stream
            outStream = response.getOutputStream();
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fileStream.read(buffer)) != -1) {
                outStream.write(buffer, 0, bytesRead);
            }
        }
        outStream.close();
        } catch(Exception e){}
    }

}
