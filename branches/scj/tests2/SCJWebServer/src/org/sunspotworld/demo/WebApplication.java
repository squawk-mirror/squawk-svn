package org.sunspotworld.demo;

/**
 * Interface defining an Application for the NanoHTTP server.
 */
public interface WebApplication {

    /**
     * Processes a HTTP request and generates a response. For convenience, the
     * web server can take care of error messages in the case of exceptions.
     * @param request not including the application specific prefix. 
     * @returns Response HTTP response
     */
    public Response serve(Request request) throws Exception;

}

