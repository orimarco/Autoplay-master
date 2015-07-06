/*
   For step-by-step instructions on connecting your Android application to this backend module,
   see "App Engine Java Servlet Module" template documentation at
   https://github.com/GoogleCloudPlatform/gradle-appengine-templates/tree/master/HelloWorld
*/

package com.autoplay.backend;

import com.google.appengine.tools.cloudstorage.GcsService;
import com.google.appengine.tools.cloudstorage.GcsServiceFactory;
import com.google.appengine.tools.cloudstorage.ListItem;
import com.google.appengine.tools.cloudstorage.ListOptions;
import com.google.appengine.tools.cloudstorage.ListResult;
import com.google.appengine.tools.cloudstorage.RetryParams;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * A simple servlet that proxies reads and writes to its Google Cloud Storage bucket.
 */
@SuppressWarnings("serial")
public class GetPlaylistServlet extends HttpServlet {

    public static final String INFO_FILE_URL = "http://storage.googleapis.com/<bucket>/<file>";
    public static final String AUDIO_BUCKET = "<bucket>";
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
     * Retrieves a file from GCS and returns it in the http response.
     * If the request path is /gcs/Foo/Bar this will be interpreted as
     * a request to read the GCS file named Bar in the bucket Foo.
     */
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    }

    /**
     * Writes the payload of the incoming post as the contents of a file to GCS.
     * If the request path is /gcs/Foo/Bar this will be interpreted as
     * a request to create a GCS file named Bar in bucket Foo.
     */
    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        int found=0;
        String playlistLengthS=req.getParameter("playlistLength");
        int playlistLength=Integer.parseInt(playlistLengthS);
        ListResult lr = gcsService.list(AUDIO_BUCKET, ListOptions.DEFAULT);
        List<String>lst=new ArrayList<>();
        while(lr.hasNext()){
            ListItem li = lr.next();
            lst.add(li.getName());
        }
        Collections.shuffle(lst);
        String songsName = "";
        String singerName="";
        String albumsName="";
        int index=0;
        while(index<lst.size()&&playlistLength>0){
            songsName += lst.get(index)+"@";
            URL url = new URL(INFO_FILE_URL);
            Scanner s = new Scanner(url.openStream());
            while(s.hasNextLine()){
                String string=s.nextLine();
                String[]check =string.split("@");
                String string2=check[0].split(":")[1];
                if(string2.compareTo(lst.get(index))==0){                     // if strings equal
                    playlistLength-=Integer.parseInt(check[2].split(":")[1]);
                    singerName+=check[1].split(":")[1]+"@";
                    albumsName+=check[3].split(":")[1]+"@";
                }
            }
            index++;
        }

        resp.getWriter().println(songsName+"#"+singerName+"#"+albumsName);
    }

}