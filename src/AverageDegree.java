/**
 * Created by Vivek on 4/2/16.
 */
import java.io.*;

public class AverageDegree {
    public static void main(String[] args) {
        try {
            Reader reader = new FileReader("tweet_input/tweets.txt");

            try (BufferedReader bufferedReader =
                         new BufferedReader(reader)) {

                String line = bufferedReader.readLine();
                while (line != null) {
                    System.out.println(line);

                    line = bufferedReader.readLine();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
