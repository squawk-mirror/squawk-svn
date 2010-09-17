/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.sunspotworld.demo;

import com.sun.squawk.VM;

/**
 *
 * @author dw29446
 */
public class VMStatsServer implements WebApplication {

    private String myText;

    public VMStatsServer(String myText) {
        this.myText = myText;

        if (VM.Stats.NUM_STAT_VALUES != LABELS.length) {
            throw new RuntimeException("Num labels is off: " + LABELS.length);
        }
    }
        
    public static final String[] LABELS = {
        "wall time",
        "wait time",
        "gc time",
        "full gc time",
        "partial gc time",
        "last gc time",
        "max full gc time",
        "max partial gc time",
        null,
        "full gc count",
        "partial gc count",
        "bytes last freed",
        "bytes total freed",
        "bytes total allocated",
        "objects total allocated",
        "threads allocated",
        "thread switch count",
        "contended monitor count",
        "monitors allocated",
        "stacks allocated",
        "max stack size",
        "throw count",
        "branch count",
        "heap free",
        "heap total"
    };

    public Response serve(Request request) {
        StringBuffer res = new StringBuffer();
        res.append("<html>\n<head><title>" + myText + "</title></head>\n");
        res.append("<body>\n<font face=\"arial narrow\" color=\"000000\">\n");
        res.append("<h1><font face=\"arial\" color=\"5382a1\">About This VM</font></h1>");
        res.append("<table><tr><td>");
        res.append("This is running the <a href=\"http://squawk.dev.java.net\"> Squawk Java VM</a>, " +
                "An open-source CLDC JVM written in Java for very small devices.<p><p>");
        res.append("<td>&nbsp;&nbsp;&nbsp;<img src=\"http://squawk.dev.java.net/figures/duke-squeak-transparent-anti-aliased.gif\"></table>");
        
        res.append("<h2><font face=\"arial\" color=\"5382a1\">VM Stats</font></h2>");
        int len = VM.Stats.NUM_STAT_VALUES;
        long[] values = new long[len];
        VM.Stats.readAllValues(values);
        
        res.append("<table>");
        res.append("<tr><th>Statistic<th>Value");
        for (int i = 0; i < len; i++) {
            if (LABELS[i] != null) {
                res.append("<tr><td>" + LABELS[i] + "<td ALIGN=\"RIGHT\">" + values[i]);
            }

        }
        res.append("</table>");
        res.append("</font>");
        res.append("</body>\n</html>");
        
        return new Response(NanoHTTP.HTTP_OK, NanoHTTP.MIME_HTML, res.toString());
    }
}
