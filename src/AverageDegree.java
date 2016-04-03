/**
 * Created by Vivek on 4/2/16.
 */

import org.apache.commons.collections.BinaryHeap;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class AverageDegree {
    public static void main(String[] args) {
        // variables that are maintained and updated for every tweet processed
        long numVertices = 0;
        long totalDegree = 0;
        double avgDegree = 0.0;
        Date maxDate = null;
        // Does the current tweet have a timestamp within 60 seconds of max or greater than max
        boolean currTweetInScope = false;

        /* ========== data structures ========== */

        /* ---------- 1. BinaryHeap ----------
        /* min heap of tweet timestamps
        /* only insertions and deletions/extractions are needed
        /* using BinaryHeap implementation from org.apache.commons.collections
         */
        BinaryHeap dateHeap = new BinaryHeap();

        /* ---------- 2. HashMap<Date, ArrayList<String>> ----------
        /* Hashtable with tweet dates as keys and list of edge Strings formed by the hashtags as values
        /* example tweet: Date=>date123, Hashtags=> #a, #b, #c
        /* HashMap entry: < date123: < "a-b", "a-c", "b-c" > >
        /* using standard Java HashMap implementation
         */
        HashMap<Date, ArrayList<String>> dateEdgeMap = new HashMap<>();

        /* ---------- 3. HashMap<String, Integer> ----------
        /* Hashtable with edge Strings as keys and how many tweets contributed those edges as values
        /* example tweets: tweet1=> #a, #b, #c;   tweet2=> #a, #c
        /* respective edges: tweet1=> a-b, a-c, b-c;   tweet2=> a-c
        /* HashMap entries: <"a-b": 1>, <"a-c": 2>, <"b-c": 1>
        /* "a-c" has value 2 because 2 tweets contributed to that edge
        /* we maintain contributions only from tweets in the 60 second window
        /* using standard Java HashMap implementation
         */
        HashMap<String, Integer> edgeContributionMap = new HashMap<>();

        /* ---------- 4. HashMap<String, Integer> ----------
        /* Hashtable with vertices(hashtags) as keys and degrees as values
        /* example tweets: tweet1=> #a, #b, #c;   tweet2=> #a, #d
        /* HashMap entries: <"a": 3>, <"b": 2>, <"c": 2>, <"d": 1>
        /* "a" has 3 edges to "b", "c" and "d";   "d" has only 1 edge to "a"
        /* this HashMap lets us determine when a vertex is not connected to anyone else
         */
        HashMap<String, Integer> vertexDegreeMap = new HashMap<>();



        // try obtaining a FileReader for tweets.txt, catch FileNotFoundException @catch(fnfe)
        try {
            //Reader reader = new FileReader("tweet_input/tweets.txt");
            // delete this line later
            Reader reader = new FileReader("tweet_input/abc.txt");
            Writer writer = new FileWriter("tweet_output/output.txt");

            // try-with-resources(BufferedReader), catch IOExeption @catch(ioe)
            try (BufferedReader bufferedReader = new BufferedReader(reader);
                 BufferedWriter bufferedWriter = new BufferedWriter(writer)) {

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

                        // if current tweet is not in scope, output the last calculated average degree
                        if(!currTweetInScope) {
                            // @uncomment write()
                            //bufferedWriter.write(Double.toString(avgDegree));
                            System.out.println(Double.toString(avgDegree));
                        }

                        // if current tweet is in scope, we have to calculate the new average degree
                        else {
                            // @delete out of scope tweets
                            System.out.println("delete out of scope tweets");

                            // while loop to find min Date from heap
                            // if min is out of scope, delete/extract min Date
                            // @update HashMaps, numVertices, totalDegree, avgDegree

                            // @add the new tweet
                            System.out.println("add the new tweet");

                            // get the hashtags as an array
                            JSONArray hashtagArray = obj.getJSONObject("entities").getJSONArray("hashtags");
                            int hashtagArrayLength = hashtagArray.length();

                            // if the tweet has no hashtags or only 1 hashtag, this won't modify the graph
                            if(hashtagArrayLength == 0 || hashtagArrayLength == 1) {
                                // @uncomment write()
                                //bufferedWriter.write(Double.toString(avgDegree));
                                System.out.println(Double.toString(avgDegree));
                            }

                            else {
                                // @create ArrayList of edge Strings
                                for (int i = 0; i < hashtagArrayLength; i++) {
                                    String hashtag = hashtagArray.getJSONObject(i).getString("text");
                                    System.out.print(hashtag + "\t");
                                }

                                // @update HashMaps, numVertices, totalDegree, avgDegree
                            }
                        }

                        System.out.println();

                    // @catch(jsone)
                    } catch(JSONException jsone) {
                        jsone.printStackTrace();
                    }
                }

            // @catch(ioe)
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }

        // @catch(fnfe)
        } catch (FileNotFoundException fnfe) {
            fnfe.printStackTrace();
        }  catch (IOException ioe) {
            ioe.printStackTrace();
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
