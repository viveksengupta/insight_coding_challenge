/**
 * Created by Vivek on 4/2/16.
 */
import java.io.*;

public class AverageDegree {
    public static void main(String[] args) {
        // try obtaining a FileReader for tweets.txt, catch FileNotFoundException @catch(fnfe)
        try {
            Reader reader = new FileReader("tweet_input/tweets.txt");

            // try-with-resources(BufferedReader), catch IOExeption @catch(ioe)
            try (BufferedReader bufferedReader =
                         new BufferedReader(reader)) {

                String line = bufferedReader.readLine();
                while (line != null) {
                    // line has data for one tweet as a JSON object


                    // read next line and continue in the while loop
                    line = bufferedReader.readLine();
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
