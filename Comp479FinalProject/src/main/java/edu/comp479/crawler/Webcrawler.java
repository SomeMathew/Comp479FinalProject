package edu.comp479.crawler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

/**
 *
 *
 */
public class Webcrawler {

    public String getUrlContents(String urlPath) {

        URL url;
        InputStream is;
        BufferedReader dis;
        String line;

        StringBuilder sb = new StringBuilder();

        try {
            url = new URL(urlPath);
            is = url.openStream();
            dis = new BufferedReader(new InputStreamReader(is));

            while ((line = dis.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }
        } catch (MalformedURLException mue) {
            System.err.println(mue.getMessage());
        } catch (IOException ioe) {
            System.err.println(ioe.getMessage());
        }

        return sb.toString();

    }
}
