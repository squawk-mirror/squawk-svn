/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.sunspotworld.demo;

import java.io.*;
import javax.microedition.io.*;

/**
 * 
 * @author dw29446
 */
public class FileServer implements WebApplication {

    public FileServer() {
    }

    public Response serve(Request request) {
        try {
            StreamConnection conn = null;
            InputStream is = null;
            StringBuffer res = new StringBuffer();
            String fileName = request.uri;
            if (fileName.indexOf("/") == 0) {
                fileName = fileName.substring(1);
            }
            try {
                System.out.println("Opening file \"" + fileName + "\"");
                conn = (StreamConnection) Connector.open("file://" + fileName, Connector.READ);

                is = conn.openInputStream();
                int ch;
                while ((ch = is.read()) != -1) {
                    res.append((char) ch);
                }

                return new Response(NanoHTTP.HTTP_OK, NanoHTTP.MIME_HTML, res.toString());
            } finally {
                try {
                    is.close();
                    conn.close();
                } catch (Exception ex) {
                    // ignore any null pointers etc for this example test
                }
            }

        } catch (IOException ex) {
            ex.printStackTrace();
            return new Response(NanoHTTP.HTTP_INTERNALERROR, NanoHTTP.MIME_PLAINTEXT, ex.toString());
        }
    }
}
