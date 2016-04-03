/**
 * Created by Vivek on 4/2/16.
 */

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AverageDegree {
    public static void main(String[] args) {
        // variables that are maintained and updated for every tweet processed
        long numVertices = 0;
        long totalDegree = 0;
        double avgDegree = 0.0;
        Date maxDate = null;
        // Does the current tweet have a timestamp within 60 seconds of max or greater than max
        boolean currTweetInScope = false;

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

                        // Get Java Date object from Timestamp String
                        Date currDate = getTwitterDate(timestamp);

                        // if this is the 1st valid tweet (might have seen rate limit messages before)
                        // or this tweet has a timestamp later than the current max
                        if(maxDate == null || currDate.compareTo(maxDate) > 0) {
                            maxDate = currDate;
                            currTweetInScope = true;
                        }

                        // if this tweet has a timestamp less than or equal to the current max
                        else if((maxDate.getTime() - currDate.getTime()) / 1000 < 60) {
                                currTweetInScope = true;
                        }

                        // if this tweet has a timestamp more than 60 seconds less than the current max
                        else {
                            currTweetInScope = false;
                        }

                        System.out.println(currTweetInScope);

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

    // Parse tweet timestamp, return Java Date
    public static Date getTwitterDate(String timestamp) {
        Date date = null;
        try {
            final String TWITTER = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";
            SimpleDateFormat sdf = new SimpleDateFormat(TWITTER);
            sdf.setLenient(true);
            date = sdf.parse(timestamp);
        } catch(ParseException pe) {
            pe.printStackTrace();
        }
        return date;
    }
}
