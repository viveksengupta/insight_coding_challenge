/**
 * Created by Vivek on 4/2/16.
 */
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;

public class AverageDegree {
    public static void main(String[] args) {
        // try obtaining a FileReader for tweets.txt, catch FileNotFoundException @catch(fnfe)
        try {
            //Reader reader = new FileReader("tweet_input/tweets.txt");
            // delete this line later
            Reader reader = new FileReader("tweet_input/abc.txt");

            // try-with-resources(BufferedReader), catch IOExeption @catch(ioe)
            try (BufferedReader bufferedReader =
                         new BufferedReader(reader)) {

                String line;

                // line has JSON data for one tweet as a String
                while ((line = bufferedReader.readLine()) != null) {
                    // ignore rate limiting messages
                    if(line.substring(0).equals("{\"limit\":")) {
                        continue;
                    }
                    // try to get a handle on the JSONObject, catch JSONException @catch(jsone)
                    try {
                        JSONObject obj = new JSONObject(line);
                        System.out.println("another line");
                        // get the timestamp
                        String timestamp = obj.getString("created_at");
                        System.out.print(timestamp + "\t");

                        // get the hashtags
                        JSONArray arr = obj.getJSONObject("entities").getJSONArray("hashtags");
                        for (int i = 0; i < arr.length(); i++)
                        {
                            String hashtag = arr.getJSONObject(i).getString("text");
                            System.out.print(hashtag + "\t");
                        }

                        System.out.println();

                    // @catch(jsone)
                    } catch(JSONException jsone) {
                        jsone.printStackTrace();
                    }


                    // read next line and continue in the while loop
                    //line = bufferedReader.readLine();
                }

            // @catch(ioe)
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }

        // @catch(fnfe)
        } catch (FileNotFoundException fnfe) {
            fnfe.printStackTrace();
        }
    }
}
