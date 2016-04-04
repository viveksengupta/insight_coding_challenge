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
    /* ========== member variables ========== */

    // 1. total number of vertices in the current hashtag graph
    int numVertices = 0;

    // 2. total degree of all vertices in the current hashtag graph
    int totalDegree = 0;

    // 3. current average degree in the hashtag graph (totalDegree / numVertices)
    double avgDegree = 0.0;

    // 4. the maximum timestamp of the latest tweet processed so far
    Date maxDate = null;

    /* 5. which action to do? add the tweet? delete other tweets? do nothing?
     * this can take 3 values to indicate what to do with the current tweet
     * -1: the tweet is out of scope, it has a timestamp of more than 60 seconds before the current max
     * 0: the tweet is out of order but in scope, it is within the last 60 seconds of the current max
     * in this case, we will add this tweet, but won't delete anything as the max doesn't change
     * 1: the tweet is in order and has a timestamp greater than the current max
     * in this case, we will add this tweet and delete all the tweets that go out of the new 60 second scope
     */
    int action = -1;


    /* ========== data structures ========== */

    /* ---------- 1. BinaryHeap ----------
     * min heap of tweet timestamps
     * only insertions and deletions/extractions are needed
     * using BinaryHeap implementation from org.apache.commons.collections
     */
    BinaryHeap dateHeap = new BinaryHeap();

    /* ---------- 2. HashMap<Date, ArrayList<String>> ----------
     * Hashtable with tweet dates as keys and list of edge Strings formed by the hashtags as values
     * example tweet: Date=>date123, Hashtags=> #a, #b, #c
     * HashMap entry: < date123: < "a-b", "a-c", "b-c" > >
     * using standard Java HashMap implementation
     */
    HashMap<Date, ArrayList<String>> dateEdgeMap = new HashMap<>();

    /* ---------- 3. HashMap<String, Integer> ----------
     * Hashtable with edge Strings as keys and how many tweets contributed those edges as values
     * example tweets: tweet1=> #a, #b, #c;   tweet2=> #a, #c
     * respective edges: tweet1=> a-b, a-c, b-c;   tweet2=> a-c
     * HashMap entries: <"a-b": 1>, <"a-c": 2>, <"b-c": 1>
     * "a-c" has value 2 because 2 tweets contributed to that edge
     * we maintain contributions only from tweets in the 60 second window
     * using standard Java HashMap implementation
     */
    HashMap<String, Integer> edgeContributionMap = new HashMap<>();

    /* ---------- 4. HashMap<String, Integer> ----------
     * Hashtable with vertices(hashtags) as keys and degrees as values
     * example tweets: tweet1=> #a, #b, #c;   tweet2=> #a, #d
     * HashMap entries: <"a": 3>, <"b": 2>, <"c": 2>, <"d": 1>
     * "a" has 3 edges to "b", "c" and "d";   "d" has only 1 edge to "a"
     * this HashMap lets us determine when a vertex is not connected to anyone else
     */
    HashMap<String, Integer> vertexDegreeMap = new HashMap<>();


    public static void main(String[] args) {
        AverageDegree avgDeg = new AverageDegree();

        // try obtaining a FileReader for tweets.txt, catch FileNotFoundException @catch(fnfe)
        try {
            //Reader reader = new FileReader("tweet_input/tweets.txt");
            // delete this line later
            Reader reader = new FileReader("tweet_input/abc.txt");
            Writer writer = new FileWriter("tweet_output/output.txt");

            // try-with-resources(BufferedReader, BufferedWriter), catch IOExeption @catch(ioe)
            try (BufferedReader bufferedReader = new BufferedReader(reader);
                 BufferedWriter bufferedWriter = new BufferedWriter(writer)) {

                String line;

                // line has JSON data for one tweet as a String
                while ((line = bufferedReader.readLine()) != null) {

                    // try to get a handle on the JSONObject, catch JSONException @catch(jsone)
                    try {
                        JSONObject obj = new JSONObject(line);
                        System.out.println("another line");

                        // this json does not have either created_at or entities
                        // it is probably a rate limiting message so it's ignored - line.startsWith("{\"limit\":")
                        // it may rarely also be something else that can be safely ignored
                        if(!obj.has("created_at") || !obj.has("entities")) {
                            continue;
                        }

                        /* if it was not a rate limiting message, we process the line */

                        // get the timestamp
                        String timestamp = obj.getString("created_at");
                        System.out.print(timestamp + "\t");

                        // get Java Date object from Timestamp String
                        Date currDate = avgDeg.getTwitterDate(timestamp);

                        // update value of action using currDate and maxDate
                        avgDeg.action = avgDeg.updateAction(currDate, avgDeg.maxDate);

                        //System.out.println(currTweetInScope);

                        // if current tweet is not in scope, output the last calculated average degree
                        if(avgDeg.action == -1) {
                            // @uncomment write()
                            //bufferedWriter.write(Double.toString(avgDegree));
                            System.out.println(Double.toString(avgDeg.avgDegree));
                        }

                        // 0: if current tweet is in scope but maxDate does not change, just add this tweet
                        // 1: if current tweet is in scope and changes maxDate, delete necessary tweets and add this tweet
                        else {
                            // deletion is only performed if (action == 1) and not if (action == 0)
                            if(avgDeg.action == 1) {

                                /* ========== @delete out of scope tweets ========== */
                                System.out.println("delete out of scope tweets ...");
                                avgDeg.removeFromDataStructures();

                                // @uncomment write()
                                //bufferedWriter.write(Double.toString(avgDegree));
                                System.out.println("avg degree after deletion(not final): " + Double.toString(avgDeg.avgDegree));
                            }

                            // addition is performed both when (action == 1) and (action == 0)

                            /* ========== @add the new tweet ========== */
                            // get the hashtags as an array
                            JSONArray hashtagJsonArray = obj.getJSONObject("entities").getJSONArray("hashtags");
                            int hashtagJsonArrayLength = hashtagJsonArray.length();

                            System.out.println("add the new tweet ...");

                            // if the tweet has no hashtags or only 1 hashtag, this won't modify the graph
                            // if the tweet has at least 2 hashtags
                            if(hashtagJsonArrayLength > 1) {
                                avgDeg.addToDataStructures(currDate, hashtagJsonArray, hashtagJsonArrayLength);
                            }

                            // @uncomment write()
                            //bufferedWriter.write(Double.toString(avgDegree));
                            System.out.println("avg degree after addition(final): " + Double.toString(avgDeg.avgDegree));
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
    public Date getTwitterDate(String timestamp) {
        Date date = null;
        try {
            final String TWITTER = "EEE MMM dd HH:mm:ss zzzzz yyyy";
            SimpleDateFormat sdf = new SimpleDateFormat(TWITTER);
            sdf.setLenient(true);
            date = sdf.parse(timestamp);
        } catch(ParseException pe) {
            pe.printStackTrace();
        }
        return date;
    }

    // set action to -1, 0 or 1 depending on currDate and maxDate
    public int updateAction(Date currDate, Date maxDate) {
        int act;

        // if this is the 1st valid tweet (might have seen rate limit messages before)
        // or this tweet has a timestamp later than the current max
        if(maxDate == null || currDate.compareTo(maxDate) > 0) {
            act = 1;
        }
        // if this tweet has a timestamp within 60 seconds before the current max
        else if((maxDate.getTime() - currDate.getTime()) / 1000 <= 60) {
            act = 0;
        }
        // if this tweet has a timestamp more than 60 seconds before than the current max
        else {
            act = -1;
        }

        return act;
    }

    public void addToDataStructures(Date currDate, JSONArray jsonArr, int jsonArrLen) throws JSONException {

        /* ===== 1. add Date to dateHeap ===== */
        dateHeap.insert(currDate);

        // @create ArrayList of edge Strings
        ArrayList<String> al;

        // if Date is already in dateEdgeMap, point al to the associated ArrayList<>
        if(dateEdgeMap.containsKey(currDate)) {
            al = dateEdgeMap.get(currDate);
        }
        // if Date is not in dataEdgeMap, initialize al to an empty ArrayList<>
        else {
            al = new ArrayList<>();
        }

        for (int i = 0; i < jsonArrLen; i++) {
            String s1 = jsonArr.getJSONObject(i).getString("text");

            for (int j = i + 1; j < jsonArrLen; j++) {
                String s2 = jsonArr.getJSONObject(i).getString("text");

                StringBuilder sb = new StringBuilder();
                if (s1.compareTo(s2) < 0) {
                    sb.append(s1).append("-").append(s2);
                } else {
                    sb.append(s2).append("-").append(s1);
                }
                // add this edge String to the ArrayList
                al.add(sb.toString());
            }
        }

        /* 2. ===== add edgeStrings to dateEdgeMap ===== */
        dateEdgeMap.put(currDate, al);


        // 3. add contribution to edgeContributionMap
        /* 4. add hashtags to vertexDegreeMap */
        // avgDeg.vertexDegreeMapAdd(s1);


        // @update HashMaps, numVertices, totalDegree, avgDegree
    }

    public void removeFromDataStructures() {
        // find min Date from dateHeap
        Date minDate = (Date) dateHeap.peek();

        // while dateHeap is not empty and minDate has timestamp more than 60 seconds before maxDate
        while(minDate != null && ((maxDate.getTime() - minDate.getTime()) / 1000 > 60)) {
            // delete tweet with minDate
            dateHeap.pop();

            /* @update HashMaps, numVertices, totalDegree, avgDegree */

        }
    }
}
