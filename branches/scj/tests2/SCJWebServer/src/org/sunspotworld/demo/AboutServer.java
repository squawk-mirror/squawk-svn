/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.sunspotworld.demo;

public class AboutServer implements WebApplication {

    private String myText;

    public AboutServer(String myText) {
        this.myText = myText;
    }

    public Response serve(Request request) {
        StringBuffer res = new StringBuffer();
        res.append("<html>\n<head><title>" + myText + "</title></head>\n");
        res.append("<body>\n<font face=\"arial narrow\" color=\"000000\">\n");
        res.append("<h1><font face=\"arial\" color=\"5382a1\">About This Web Server</font></h1>");
        res
                .append("This web server is based on the very small web server <a href=\"http://elonen.iki.fi/code/nanohttpd/\">NanoHTTPD</a>, "
                        + "as re-imagined by Vipul Gupta, and back-ported from SPOT to generic Java ME code by Derek White.<p><p>");

        res.append("<ul>");
        res.append("<li> Pages Served: " + NanoHTTP.getPagesServed());
        res.append("<li> <a href=\"/stats\">VM Statistics</a>");
        res.append("</ul><p>");
        res
                .append("Portions Copyright © 2001,2005-2008 Jarno Elonen elonen@iki.fi<p><p>"
                        +

                        "Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:<p><p>"
                        + "<ol>"
                        + " <li> Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer."
                        + " <li>  Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution."
                        + " <li>  The name of the author may not be used to endorse or promote products derived from this software without specific prior written permission."
                        + "</ol>"
                        + "THIS SOFTWARE IS PROVIDED BY THE AUTHOR ''AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.");
        res.append("</font>");
        res.append("</body>\n</html>");

        return new Response(NanoHTTP.HTTP_OK, NanoHTTP.MIME_HTML, res.toString());
    }
}
