/*
   For step-by-step instructions on connecting your Android application to this backend module,
   see "App Engine Java Servlet Module" template documentation at
   https://github.com/GoogleCloudPlatform/gradle-appengine-templates/tree/master/HelloWorld
*/

package com.autoplay.backend;

import com.google.appengine.tools.cloudstorage.GcsFileOptions;
import com.google.appengine.tools.cloudstorage.GcsFilename;
import com.google.appengine.tools.cloudstorage.GcsInputChannel;
import com.google.appengine.tools.cloudstorage.GcsService;
import com.google.appengine.tools.cloudstorage.GcsServiceFactory;
import com.google.appengine.tools.cloudstorage.RetryParams;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.sql.Timestamp;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * A simple servlet that proxies reads and writes to its Google Cloud Storage bucket.
 */
@SuppressWarnings("serial")
public class PostServer extends HttpServlet {

    /**
     * This is where backoff parameters are configured. Here it is aggressively retrying with
     * backoff, up to 10 times but taking no more that 15 seconds total to do so.
     */
    private final GcsService gcsService = GcsServiceFactory.createGcsService(new RetryParams.Builder()
            .initialRetryDelayMillis(10)
            .retryMaxAttempts(10)
            .totalRetryPeriodMillis(15000)
            .build());

    /**
     * Writes the log to the file- logging.txt
     * This method gets a list of completed songs and uncompleted songs from
     * App and appends the information to the file.
     */
    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String CompletedSongs=req.getParameter("CompletedSongs");
        String UnCompletedSongs=req.getParameter("UnCompletedSongs");        // getting parameters from app
        String android_id=req.getParameter("android_id");
        java.util.Date date= new java.util.Date();
        Timestamp ts = new Timestamp(date.getTime());

        GcsFilename gcsFilename=new GcsFilename("autoplay-logging","logging.txt");
        int fileSize = (int) gcsService.getMetadata(gcsFilename).getLength();
        ByteBuffer previous = ByteBuffer.allocate(fileSize);

        try (GcsInputChannel readChannel = gcsService.openReadChannel(gcsFilename, 0)) {
            readChannel.read(previous);
        }
        String v = new String(previous.array());
        Charset charset = Charset.forName("UTF-8");
        CharsetEncoder encoder = charset.newEncoder();
        ByteBuffer updated =encoder.encode(CharBuffer.wrap(v+"\n"+ts.toString()+"\nandroid id:"+android_id+"\n\tUncompleted Songs:"+UnCompletedSongs+"\n\tCompleted Songs:"+CompletedSongs+"#"));
        GcsFileOptions.Builder gcsfileoptions=new GcsFileOptions.Builder();
        GcsFileOptions gcsfileoptions1=gcsfileoptions.build();
        gcsService.createOrReplace(gcsFilename,  gcsfileoptions1,  updated);
    }

}