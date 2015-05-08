/*
   For step-by-step instructions on connecting your Android application to this backend module,
   see "App Engine Java Servlet Module" template documentation at
   https://github.com/GoogleCloudPlatform/gradle-appengine-templates/tree/master/HelloWorld
*/

package com.autoplay.backend;
//import com.parse.GetCallback;
//import com.parse.ParseFile;
//import com.parse.ParseObject;
//import com.parse.ParseQuery;
//
//import java.io.IOException;
//import java.text.ParseException;
//
//import javax.servlet.http.*;
//
//
//public class MyServlet extends HttpServlet {
//    private String url;
//
//    @Override
//    public void doGet(HttpServletRequest req, HttpServletResponse resp)
//            throws IOException {
//        resp.setContentType("text/plain");
//        resp.getWriter().println("Please use the form to POST to this url");
//    }
//
//    @Override
//    public void doPost(HttpServletRequest req, HttpServletResponse resp)
//            throws IOException {
//
//        String name = req.getParameter("name");
//        resp.setContentType("text/plain");
//        if (name == null) {
//            resp.getWriter().println("Please enter a name");
//        }
//
//        ParseQuery<ParseObject> query = ParseQuery.getQuery("Autoplay");
//        query.getInBackground("9pKeuDnP9y", new GetCallback<ParseObject>() {
//            public void done(ParseObject object, ParseException e) {
//                if (e == null) {
//                    ParseFile audioFile = object.getParseFile("AudioFile");
//                    url = audioFile.getUrl();
//                    try {
////                        MediaPlayer mp = new MediaPlayer();
////                        mp.setDataSource(audioFileURL);
////                        mp.prepare();
////                        mp.start();
//                        //       mediaPlayer = MediaPlayer.create(this, Uri.parse(audioFileURL));
//                        //      mediaPlayer = new MediaPlayer();
//                        //      mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
//
//                        //          mediaPlayer.setDataSource(audioFileURL);
//                        //          mediaPlayer.prepare();
//                        //          mediaPlayer.start();
//                    } catch (Exception e1) {
//                        // TODO Auto-generated catch block
//                        e1.printStackTrace();
//                    }
//                } else {
//                    // something went wrong
//                }
//            }
//
//            @Override
//            public void done(ParseObject parseObject, com.parse.ParseException e) {
//            }
//        });
//
//        resp.getWriter().println(url);
//    }
//
//}


        import com.google.appengine.tools.cloudstorage.GcsService;
import com.google.appengine.tools.cloudstorage.GcsServiceFactory;
import com.google.appengine.tools.cloudstorage.ListItem;
import com.google.appengine.tools.cloudstorage.ListOptions;
import com.google.appengine.tools.cloudstorage.ListResult;
import com.google.appengine.tools.cloudstorage.RetryParams;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * A simple servlet that proxies reads and writes to its Google Cloud Storage bucket.
 */
@SuppressWarnings("serial")
public class MyServlet extends HttpServlet {
    public static final boolean SERVE_USING_BLOBSTORE_API = false;

    /**
     * This is where backoff parameters are configured. Here it is aggressively retrying with
     * backoff, up to 10 times but taking no more that 15 seconds total to do so.
     */
    private final GcsService gcsService = GcsServiceFactory.createGcsService(new RetryParams.Builder()
            .initialRetryDelayMillis(10)
            .retryMaxAttempts(10)
            .totalRetryPeriodMillis(15000)
            .build());

    /**Used below to determine the size of chucks to read in. Should be > 1kb and < 10MB */
    private static final int BUFFER_SIZE = 2 * 1024 * 1024;

    /**
     * Retrieves a file from GCS and returns it in the http response.
     * If the request path is /gcs/Foo/Bar this will be interpreted as
     * a request to read the GCS file named Bar in the bucket Foo.
     */
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
////        GcsFilename fileName = getFileName(req);
//        if (SERVE_USING_BLOBSTORE_API) {
//            BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
//            BlobKey blobKey = blobstoreService.createGsBlobKey(
//                    "/gs/" + fileName.getBucketName() + "/" + fileName.getObjectName());
//            blobstoreService.serve(blobKey, resp);
//        } else {
//            GcsInputChannel readChannel = gcsService.openPrefetchingReadChannel(fileName, 0, BUFFER_SIZE);
//            copy(Channels.newInputStream(readChannel), resp.getOutputStream());
//        }
    }

    /**
     * Writes the payload of the incoming post as the contents of a file to GCS.
     * If the request path is /gcs/Foo/Bar this will be interpreted as
     * a request to create a GCS file named Bar in bucket Foo.
     */
    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        ListResult lr = gcsService.list("autoplay_audio", ListOptions.DEFAULT);
        String str = "";
        int i = 0;
        while(lr.hasNext()){
            ListItem li = lr.next();
                str += li.getName()+"@";
                i++;
            }
       str+="Yigdal.mp3";
//        GcsFileMetadata metaData = gcsService.getMetadata(getFileName(req));
//        GcsOutputChannel outputChannel =
//                gcsService.createOrReplace(getFileName(req), GcsFileOptions.getDefaultInstance());
//        copy(req.getInputStream(), Channels.newOutputStream(outputChannel));
        resp.getWriter().println(str);
    }

//    private GcsFilename getFileName(HttpServletRequest req) {
////        String[] splits = req.getRequestURI().split("/", 4);
////        if (!splits[0].equals("") || !splits[1].equals("gcs")) {
////            throw new IllegalArgumentException("The URL is not formed as expected. " +
////                    "Expecting /gcs/<bucket>/<object>");
////        }
//        return new GcsFilename("autoplay_audio", "Paradise.mp3");
////        return new GcsFilename(splits[2], splits[3]);
//    }

    /**
     * Transfer the data from the inputStream to the outputStream. Then close both streams.
     */
    private void copy(InputStream input, OutputStream output) throws IOException {
        try {
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead = input.read(buffer);
            while (bytesRead != -1) {
                output.write(buffer, 0, bytesRead);
                bytesRead = input.read(buffer);
            }
        } finally {
            input.close();
            output.close();
        }
    }
}