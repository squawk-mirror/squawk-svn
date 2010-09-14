package org.sunspotworld.demo;

import java.io.*;
import java.util.*;
import com.sun.squawk.util.StringTokenizer;

/**
 * A simple, tiny, nicely embeddable HTTP 1.0 server in Java
 * 
 * <p>
 * NanoHTTPD version 1.01, Copyright &copy; 2001 Jarno Elonen (elonen@iki.fi,
 * http://iki.fi/elonen/)
 * 
 * <p>
 * <b>Features & limitations: </b>
 * <ul>
 * 
 * <li>Java 1.1 compatible</li>
 * <li>Released as open source, Modified BSD licence</li>
 * <li>No fixed config files, logging, authorization etc. (Implement yourself if
 * you need them.)</li>
 * <li>Supports parameter parsing of GET and POST methods</li>
 * <li>Never caches anything</li>
 * <li>Doesn't limit bandwidth, request time or simultaneous connections</li>
 * 
 * </ul>
 * 
 * See the end of the source file for distribution license (Modified BSD
 * licence)
 */
public class NanoHTTP {
    /*
     * private String connURL;
     * 
     * public NanoHTTP(String connURL) { this.connURL = connURL; }
     */

    // ==================================================
    // API parts
    // ==================================================
    /**
     * Some HTTP response status codes
     */
    public static final String HTTP_OK = "200 OK", HTTP_REDIRECT = "301 Moved Permanently",
            HTTP_FORBIDDEN = "403 Forbidden", HTTP_NOTFOUND = "404 Not Found",
            HTTP_BADREQUEST = "400 Bad Request", HTTP_INTERNALERROR = "500 Internal Server Error",
            HTTP_NOTIMPLEMENTED = "501 Not Implemented";
    /**
     * Common mime types for dynamic content
     */
    public static final String MIME_PLAINTEXT = "text/plain", MIME_HTML = "text/html",
            MIME_DEFAULT_BINARY = "application/octet-stream";
    /**
     * Vector to store all registered Web Applications
     */
    private Vector apps = new Vector();
    private static int pagesServed;

    public static int getPagesServed() {
        return pagesServed;
    }

    /**
     * Register a new Web Application
     */
    public void addApplication(String uriPrefix, WebApplication app) {
        apps.addElement(new Application(uriPrefix, app));
    }

    private void debugPrint(String uri, Properties header, Properties parms) {
        System.out.println("URI: '" + uri + "' ");

        Enumeration e = header.propertyNames();
        while (e.hasMoreElements()) {
            String value = (String) e.nextElement();
            System.out.println("  HDR: '" + value + "' = '" + header.getProperty(value) + "'");
        }
        e = parms.propertyNames();
        while (e.hasMoreElements()) {
            String value = (String) e.nextElement();
            System.out.println("  PRM: '" + value + "' = '" + parms.getProperty(value) + "'");
        }
    }

    // ==================================================
    // Socket & server code
    // ==================================================
    private String readLine(Reader in) throws IOException {
        StringBuffer res = new StringBuffer();
        int ch;
        while ((ch = in.read()) > 0) {
            if (ch == '\n') {
                break;
            }
            if (ch != '\r') {
                res.append((char) ch);
            }
        }
        return res.toString();
    }

    public void handleRequest(InputStream ins, OutputStream outs) throws IOException {
        try {
            Reader in = new InputStreamReader(ins);

            // Read the request line
            StringTokenizer st = new StringTokenizer(readLine(in));
            if (!st.hasMoreTokens()) {
                sendError(outs, HTTP_BADREQUEST, "BAD REQUEST: Syntax error");
                return;
            }

            String method = st.nextToken();

            if (!st.hasMoreTokens()) {
                sendError(outs, HTTP_BADREQUEST, "BAD REQUEST: Missing URI");
                return;
            }

            String uri = decodePercent(st.nextToken());

            String parms = null;

            // Separate parameters from the URI
            int qmi = uri.indexOf('?');
            if (qmi >= 0) {
                parms = uri.substring(qmi + 1);
                uri = decodePercent(uri.substring(0, qmi));
            }

            Request request = new Request(method, uri);

            // Decode parameters
            if (parms != null) {
                decodeParms(parms, request.parms);
            }

            // If there's another token, it's protocol version,
            // followed by HTTP headers. Ignore version but parse headers.
            if (st.hasMoreTokens()) {
                String line = readLine(in);
                while (line.trim().length() > 0) {
                    int p = line.indexOf(':');
                    request.header.put(line.substring(0, p).trim(), line.substring(p + 1).trim());
                    line = readLine(in);
                }
            }

            // If the method is POST, there may be parameters
            // in data section, too, read another line:
            if (method.equals("POST")) {
                decodeParms(readLine(in), request.parms);
            }

            // debugPrint(uri, header, parms);

            // Ok, now do the serve()
            Application found = null;
            int score = -1;
            int size = apps.size();

            for (int i = 0; i < size; i++) {
                Application app = (Application) apps.elementAt(i);
                int myScore = app.matchScore(uri);
                // System.out.println("score for " + app + " = " + myScore +
                // " for uri" + uri);
                if (myScore > score) {
                    score = myScore;
                    found = app;
                }
            }

            if (score >= 0) {
                Response response = found.serve(request);
                sendResponse(outs, response);
            } else {
                sendError(outs, HTTP_NOTFOUND, HTTP_NOTFOUND);
            }
            pagesServed++;
        } catch (IllegalArgumentException iae) {
            // sendError(outs, HTTP_BADREQUEST, iae.toString());
        } catch (Exception e) {
            debug("Handle request!");
            e.printStackTrace();
            // sendError(outs, HTTP_INTERNALERROR,
            // "SERVER INTERNAL ERROR: Exception: " + e.toString());
        }
    }

    /**
     * Decodes the percent encoding scheme. <br/>
     * For example: "an+example%20string" -> "an example string"
     */
    private String decodePercent(String str) throws IllegalArgumentException {
        try {
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < str.length(); i++) {
                char c = str.charAt(i);
                switch (c) {
                case '+':
                    sb.append(' ');
                    break;
                case '%':
                    sb.append((char) Integer.parseInt(str.substring(i + 1, i + 3), 16));
                    i += 2;
                    break;
                default:
                    sb.append(c);
                    break;
                }
            }
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalArgumentException("BAD REQUEST: Bad percent-encoding.");
        }
    }

    /**
     * Decodes parameters in percent-encoded URI-format ( e.g.
     * "name=Jack%20Daniels&pass=Single%20Malt" ) and adds them to given
     * Properties.
     */
    private void decodeParms(String parms, Properties p) {
        if (parms == null) {
            return;
        }
        StringTokenizer st = new StringTokenizer(parms, "&");
        while (st.hasMoreTokens()) {
            String e = st.nextToken();
            int sep = e.indexOf('=');
            if (sep >= 0) {
                p.put(decodePercent(e.substring(0, sep)).trim(),
                        decodePercent(e.substring(sep + 1)));
            }
        }
    }

    /**
     * Returns an error message as a HTTP response.
     */
    private void sendError(OutputStream outs, String status, String msg) throws IOException {
        sendResponse(outs, new Response(status, MIME_PLAINTEXT, msg));
    }

    /**
     * Sends given response to the socket.
     */
    private void sendResponse(OutputStream outs, Response response) throws IOException {
        String status = response.status;
        String mime = response.mimeType;
        Properties header = response.header;
        InputStream data = response.data;
        int contentLength = response.contentLength;

        if (status == null) {
            throw new Error("sendResponse(): Status can't be null.");
        }
        Writer out = new OutputStreamWriter(outs);
        out.write("HTTP/1.0 " + status + " \r\n");

        if (contentLength >= 0) {
            out.write("Content-Length: " + contentLength + "\r\n");
        }
        if (mime != null) {
            out.write("Content-Type: " + mime + "\r\n"); // if (
            // header.getProperty(
            // "Date" ) == null )
            // pw.print( "Date: " + gmtFrmt.format( new Date()) + "\r\n");
        }
        Enumeration e = header.keys();
        while (e.hasMoreElements()) {
            String key = (String) e.nextElement();
            String value = header.getProperty(key);
            out.write(key + ": " + value + "\r\n");
        }

        out.write("\r\n");
        out.flush();

        if (data != null) {
            byte[] buff = new byte[2048];
            int read = 2048;
            while (read == 2048) {
                read = data.read(buff, 0, 2048);
                outs.write(buff, 0, read);
            }
        }
        outs.flush();
        if (data != null) {
            data.close();
        }
    }

    /**
     * Class representing a web application. Stores the URI prefix and the
     * associated handler.
     */
    private final class Application {

        private String uriPrefix;
        private WebApplication app;
        private int prefixLength;

        /**
         * Creates a new application object with the given URI prefix and
         * request handler.
         */
        public Application(String uriPrefix, WebApplication app) {
            this.app = app;
            this.uriPrefix = uriPrefix;
            this.prefixLength = uriPrefix.length();
        }

        /**
         * Checks if the given URI matches this application's prefix, and
         * returns a score. The caller can use the score to determine which
         * application to invoke.
         * 
         * @returns -1 if the prefix doesn't match or the length of the prefix.
         */
        public int matchScore(String uri) {
            int score = -1;
            if (uri.startsWith(uriPrefix)) {
                score = prefixLength;
            }
            return score;
        }

        /**
         * Calls the WebApplication's serve method after stripping the URI
         * prefix.
         */
        public Response serve(Request request) throws Exception {
            request.uriPrefix = request.uri.substring(0, prefixLength);
            request.uri = request.uri.substring(prefixLength);
            return app.serve(request);
        }
    }

    private static void debug(String s) {
        System.out.println(s);
    }
}
