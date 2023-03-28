package IRUtilities;

import java.io.*;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;

public class StopStem
{
    private Porter porter;
    private HashSet<String> stopWords;
    public boolean isStopWord(String str)
    {
        return stopWords.contains(str);
    }
    public StopStem(String str)  {
        super();
        porter = new Porter();
        stopWords = new HashSet<String>();

        // use BufferedReader to extract the stopwords in stopwords.txt (path passed as parameter str)
        // add them to HashSet<String> stopWords
        // MODIFY THE BELOW CODE AND ADD YOUR CODES HERE
        BufferedReader reader;
        try{
            reader = new BufferedReader(new FileReader(str));
            try{
                String line = reader.readLine();
                while (line != null) {
                    //System.out.println(line);
                    stopWords.add(line);
                    line = reader.readLine();
                }
                reader.close();
            }
            catch(IOException I){System.out.println("x");}

        }
        catch(FileNotFoundException f){System.out.println("file not found");}




    }
    public String stem(String str)
    {
        return porter.stripAffixes(str);
    }

}

