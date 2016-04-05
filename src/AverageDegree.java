/**
 * Created by Vivek on 4/2/16.
 */

import org.apache.commons.collections.BinaryHeap;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class AverageDegree {
    /* ========== member variables ========== */

    // 1. total number of vertices in the current hashtag graph
    private int numVertices;

    // 2. total degree of all vertices in the current hashtag graph
    private int totalDegree;

    // 3. current average degree in the hashtag graph (totalDegree / numVertices)
    private double avgDegree;

    // 4. the maximum timestamp of the latest tweet processed so far
    private Date maxDate;

    // 5. the timestamp of the current tweet being processed
    private Date currDate;

    /* 5. which actionType to do? add the tweet? delete other tweets? do nothing?
     * this can take 3 values to indicate what to do with the current tweet
     * -1: the tweet is out of scope, it has a timestamp of more than 60 seconds before the current max
     * 0: the tweet is out of order but in scope, it is within the last 60 seconds of the current max
     * in this case, we will add this tweet, but won't delete anything as the max doesn't change
     * 1: the tweet is in order and has a timestamp greater than the current max
     * in this case, we will add this tweet and delete all the tweets that go out of the new 60 second scope
     */
    private int actionType;


    /* ========== data structures ========== */

    /* ---------- 1. BinaryHeap ----------
     * min heap of tweet timestamps
     * only insertions and deletions/extractions are needed
     * using BinaryHeap implementation from org.apache.commons.collections
     */
    private BinaryHeap dateHeap;

    /* ---------- 2. HashMap<Date, ArrayList<String>> ----------
     * Hashtable with tweet dates as keys and list of edge Strings formed by the hashtags as values
     * example tweet: Date=>date123, Hashtags=> #a, #b, #c
     * HashMap entry: < date123: < "a-b", "a-c", "b-c" > >
     * using standard Java HashMap implementation
     */
    private HashMap<Date, ArrayList<String>> dateEdgeMap;

    /* ---------- 3. HashMap<String, Integer> ----------
     * Hashtable with edge Strings as keys and how many tweets contributed those edges as values
     * example tweets: tweet1=> #a, #b, #c;   tweet2=> #a, #c
     * respective edges: tweet1=> a-b, a-c, b-c;   tweet2=> a-c
     * HashMap entries: <"a-b": 1>, <"a-c": 2>, <"b-c": 1>
     * "a-c" has value 2 because 2 tweets contributed to that edge
     * we maintain contributions only from tweets in the 60 second window
     * using standard Java HashMap implementation
     */
    private HashMap<String, Integer> edgeContributionMap;

    /* ---------- 4. HashMap<String, Integer> ----------
     * Hashtable with vertices(hashtags) as keys and degrees as values
     * example tweets: tweet1=> #a, #b, #c;   tweet2=> #a, #d
     * HashMap entries: <"a": 3>, <"b": 2>, <"c": 2>, <"d": 1>
     * "a" has 3 edges to "b", "c" and "d";   "d" has only 1 edge to "a"
     * this HashMap lets us determine when a vertex is not connected to anyone else
     */
    private HashMap<String, Integer> vertexDegreeMap;


    public AverageDegree() {
        numVertices = 2;
        totalDegree = 7;
        avgDegree = 0.00;
        maxDate = null;
        currDate = null;
        actionType = -1;
        dateHeap = new BinaryHeap();
        dateEdgeMap = new HashMap<>();
        edgeContributionMap = new HashMap<>();
        vertexDegreeMap = new HashMap<>();
    }


    public static void main(String[] args) {
        AverageDegree avgDeg = new AverageDegree();

        // try obtaining a FileReader for tweets.txt, catch FileNotFoundException @catch(fnfe)
        try {
            //Reader reader = new FileReader("tweet_input/tweets.txt");
            // delete this line later
            Reader reader = new FileReader("../tweet_input/tweets.txt");
            Writer writer = new FileWriter("../tweet_output/output.txt");

            // try-with-resources(BufferedReader, BufferedWriter), catch IOExeption @catch(ioe)
            try (BufferedReader bufferedReader = new BufferedReader(reader);
                 BufferedWriter bufferedWriter = new BufferedWriter(writer)) {

                String line;

                // line has JSON data for one tweet as a String
                while ((line = bufferedReader.readLine()) != null) {

                    // try to get a handle on the JSONObject, catch JSONException @catch(jsone)
                    try {
                        JSONObject obj = new JSONObject(line);
                        //System.out.println("another line: " + obj);

                        // this json does not have either created_at or entities
                        // it is probably a rate limiting message so it's ignored - line.startsWith("{\"limit\":")
                        // it may rarely also be something else that can be safely ignored
                        if(!obj.has("created_at") || !obj.has("entities")) {
                            //System.out.println("not a valid tweet\n");
                            continue;
                        }

                        /* if it was not a rate limiting message, we process the line */

                        // get the timestamp
                        String timestamp = obj.getString("created_at");
                        //System.out.println("timestamp: " + timestamp);

                        // get Java Date object from Timestamp String
                        avgDeg.setCurrDateFromTweetTimestamp(timestamp);
                        //System.out.println("currDate: " + avgDeg.currDate);

                        // update value of actionType using currDate and maxDate
                        avgDeg.setActionTypeFromCurrDateAndMaxDate();
                        //System.out.println("maxDate: " + avgDeg.maxDate);
                        //System.out.println("actionType: " + avgDeg.actionType);

                        //System.out.println(currTweetInScope);

                        // if current tweet is not in scope, output the last calculated average degree
                        if(avgDeg.actionType == -1) {
                            //System.out.println(Double.toString(avgDeg.avgDegree));
                            // @write() avgDegree to output.txt
                            bufferedWriter.write(avgDeg.truncateAvgDegree().toString() + "\n");
                        }

                        // 0: if current tweet is in scope but maxDate does not change, just add this tweet
                        // 1: if current tweet is in scope and changes maxDate, delete necessary tweets and add this tweet
                        else {
                            // deletion is only performed if (actionType == 1) and not if (actionType == 0)
                            if(avgDeg.actionType == 1) {

                                /* ========== @delete out of scope tweets ========== */
                                //System.out.println("delete out of scope tweets ...");
                                avgDeg.removeFromDataStructures();

                                //System.out.println(avgDeg.dateHeap);
                                //System.out.println(avgDeg.dateEdgeMap);
                                //System.out.println(avgDeg.edgeContributionMap);
                                //System.out.println(avgDeg.vertexDegreeMap);

                                //System.out.println("numVertices: " + avgDeg.numVertices);
                                //System.out.println("totalDegree: " + avgDeg.totalDegree);
                                //System.out.printf("avg degree after deletion(not final): " + avgDeg.truncateAvgDegree());
                            }

                            // addition is performed both when (actionType == 1) and (actionType == 0)

                            /* ========== @add the new tweet ========== */
                            // get the hashtags as an array
                            JSONArray hashtagJsonArray = obj.getJSONObject("entities").getJSONArray("hashtags");
                            int hashtagJsonArrayLength = hashtagJsonArray.length();

                            //System.out.println(hashtagJsonArray);
                            //System.out.println(hashtagJsonArrayLength);

                            //System.out.println("add the new tweet ...");

                            // if the tweet has no hashtags or only 1 hashtag, this won't modify the graph
                            // if the tweet has at least 2 hashtags
                            if(hashtagJsonArrayLength > 1) {
                                avgDeg.addToDataStructures(hashtagJsonArray, hashtagJsonArrayLength);

                                //System.out.println(avgDeg.dateHeap);
                                //System.out.println(avgDeg.dateEdgeMap);
                                //System.out.println(avgDeg.edgeContributionMap);
                                //System.out.println(avgDeg.vertexDegreeMap);
                            }


                            //System.out.println("numVertices: " + avgDeg.numVertices);
                            //System.out.println("totalDegree: " + avgDeg.totalDegree);
                            //System.out.printf("avg degree after addition(final): " + avgDeg.truncateAvgDegree());

                            // @write() avgDegree to output.txt
                            bufferedWriter.write(avgDeg.truncateAvgDegree().toString() + "\n");
                        }

                        //System.out.println();

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
        // @catch(ioe)
        }  catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    // Parse tweet timestamp, return Java Date
    private void setCurrDateFromTweetTimestamp(String timestamp) {
        try {
            final String TWITTER = "EEE MMM dd HH:mm:ss zzzzz yyyy";
            SimpleDateFormat sdf = new SimpleDateFormat(TWITTER);
            sdf.setLenient(true);
            currDate = sdf.parse(timestamp);
        } catch(ParseException pe) {
            pe.printStackTrace();
        }
    }

    // set actionType to -1, 0 or 1 depending on currDate and maxDate
    private void setActionTypeFromCurrDateAndMaxDate() {

        // if this is the 1st valid tweet (might have seen rate limit messages before)
        // or this tweet has a timestamp later than the current max
        if(maxDate == null || currDate.compareTo(maxDate) > 0) {
            maxDate = currDate;
            actionType = 1;
        }
        // if this tweet has a timestamp within 60 seconds before the current max
        else if((maxDate.getTime() - currDate.getTime()) / 1000 < 60) {
            actionType = 0;
        }
        // if this tweet has a timestamp more than 60 seconds before than the current max
        else {
            actionType = -1;
        }
    }

    // for every in scope tweet, format and add relevant information to the data structures
    private void addToDataStructures(JSONArray jsonArr, int jsonArrLen) throws JSONException {

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
                String s2 = jsonArr.getJSONObject(j).getString("text");

                StringBuilder sb = new StringBuilder();
                if (s1.compareTo(s2) < 0) {
                    sb.append(s1).append("-").append(s2);
                } else {
                    sb.append(s2).append("-").append(s1);
                }

                String edgeString = new String(sb);

                /* ===== 2. add contribution to edgeContributionMap ===== */
                if(edgeContributionMap.containsKey(edgeString)) {
                    edgeContributionMap.put(edgeString, edgeContributionMap.get(edgeString)+1);
                }
                else {
                    edgeContributionMap.put(edgeString, 1);
                    totalDegree += 2;

                    /* 3. ===== add degree to vertexDegreeMap ===== */

                    // increase degree of hashtag vertex s1
                    if(vertexDegreeMap.containsKey(s1)) {
                        vertexDegreeMap.put(s1, vertexDegreeMap.get(s1)+1);
                    }
                    else {
                        vertexDegreeMap.put(s1, 1);
                        numVertices++;
                    }

                    // increase degree of hashtag vertex s2
                    if(vertexDegreeMap.containsKey(s2)) {
                        vertexDegreeMap.put(s2, vertexDegreeMap.get(s2)+1);
                    }
                    else {
                        vertexDegreeMap.put(s2, 1);
                        numVertices++;
                    }
                }

                // add this edgeString to the ArrayList
                al.add(sb.toString());
            }
        }

        /* 4. ===== add edgeStrings to dateEdgeMap ===== */
        dateEdgeMap.put(currDate, al);

        /* @update avgDegree */
        calculateAvgDegree();
    }

    // while processing a new tweet, remove information of all the tweets that go out of scope from the data structures
    private void removeFromDataStructures() {

        // while dateHeap is not empty and minDate has timestamp more than 60 seconds before maxDate
        while(!dateHeap.isEmpty()) {
            // grab the min date
            Date minDate = (Date) dateHeap.peek();

            // if the minimum Date has gone out of scope, it needs to be deleted
            if((maxDate.getTime() - minDate.getTime()) / 1000 >= 60) {

                /* ===== 1. delete Date from dateHeap ===== */
                dateHeap.pop();

                // get the ArrayList of Strings from dateEdgeMap
                ArrayList<String> al = dateEdgeMap.get(minDate);

                // tweets at timestamp minDate didn't add any edges, hence the ArrayList is empty
                if(al == null) {
                    return;
                }

                for(String edgeString : al) {
                //for(int i=0; i<al2.size(); i++) {
                    //String edgeString = al2.get(i);

                    // if there are more than 1 contribution towards this edge,
                    // i.e., if some other tweet(s) is/are contributing towards this edge as well
                    // then the edge won't be deleted, hence there would be no change to the average degree
                    int contribution = edgeContributionMap.get(edgeString);
                    if(contribution > 1) {
                        edgeContributionMap.put(edgeString, contribution-1);
                    }

                    // if the only contribution towards this edge is from a tweet having timestamp minDate
                    else {
                        // the edge was contributing 2 towards the total degree, 1 for each vertex
                        totalDegree -= 2;

                        // identifying the location of the vertex separator "-" in the edge String
                        int index = edgeString.indexOf("-");
                        // s1 will hold the 1st vertex in the edge
                        String s1 = edgeString.substring(0,index);
                        // s2 will hold the 2nd vertex in the edge
                        String s2 = edgeString.substring(index+1);

                        /* 2. ===== delete vertex from vertexDegreeMap ===== */

                        int degree = vertexDegreeMap.get(s1);
                        // if degree of the vertex is greater than 1, we will decrease the degree by 1
                        if(degree > 1) {
                            vertexDegreeMap.put(s1, degree-1);
                        }
                        // if degree of the vertex is 1, then the vertex needs to be deleted
                        else {
                            vertexDegreeMap.remove(s1);
                            numVertices--;
                        }

                        degree = vertexDegreeMap.get(s2);
                        // if degree of the vertex is greater than 1, we will decrease the degree by 1
                        if(degree > 1) {
                            vertexDegreeMap.put(s2, degree-1);
                        }
                        // if degree of the vertex is 1, then the vertex needs to be deleted
                        else {
                            vertexDegreeMap.remove(s2);
                            numVertices--;
                        }

                        /* ===== 3. delete contribution from edgeContributionMap ===== */
                        edgeContributionMap.remove(edgeString);
                    }
                }

                /* 4. ===== delete edgeStrings from dateEdgeMap ===== */
                dateEdgeMap.remove(minDate);
            }
            // if the minimum Date is still in scope, break out of the while loop
            else {
                break;
            }

            /* @update avgDegree */
            calculateAvgDegree();
        }
    }

    // calculate avgDegree from totalDegree and numVertices
    private void calculateAvgDegree() {
        if(totalDegree == 0 || numVertices == 0) {
            avgDegree = 0.00;
        }
        else {

            avgDegree = (double) totalDegree / numVertices;
        }
    }

    private BigDecimal truncateAvgDegree()
    {
        return new BigDecimal(String.valueOf(avgDegree)).setScale(2, BigDecimal.ROUND_FLOOR);
    }
}
