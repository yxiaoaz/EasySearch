import DataWareHouse.FileManager;
import DataWareHouse.FileNameGenerator;
import DataWareHouse.ID_Mapping;
import IRUtilities.StopStem;
import ResultProcessor.VectorSpaceRanker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class Test {
    public static ArrayList<String> processQuery(ArrayList<String> query){
        ArrayList<String> res = new ArrayList<>();
        StopStem stopStem = new StopStem("src/IRUtilities/stopwords.txt");
        for(String t: query){
            if(!stopStem.isStopWord(t.toLowerCase())&&!stopStem.stem(t).isBlank())
                res.add(stopStem.stem(t));
        }
        return res;
    }
    public static ArrayList<String> splitQuery(String[] rawQuery){
        ArrayList<String> query = new ArrayList<>();
        int parser = 0;
        boolean constructingPhrase = false;
        String phraseInConstruction = "";
        while(parser< rawQuery.length){
            if(!rawQuery[parser].startsWith("\"")){
                if(!constructingPhrase){ /**[yes no "hong kong is good" crazy], we are at "no"*/
                    query.add(rawQuery[parser]);
                }
                else{ /**[yes no "hong kong is good" crazy], we are at "kong", phraseInConstruction = "hong"*/
                    if(!rawQuery[parser].endsWith("\"")){
                        phraseInConstruction=phraseInConstruction.concat(" "+rawQuery[parser]);
                    }
                    else{/**[yes no "hong kong is good" crazy], we are at "good", phraseInConstruction = "hong kong is"*/
                        phraseInConstruction=phraseInConstruction.concat(" "+rawQuery[parser].substring(0,rawQuery[parser].length()-1));
                        constructingPhrase = false;
                        System.out.println("Phrase in construction: "+phraseInConstruction);
                        query.add(phraseInConstruction);
                    }
                }
            }
            /**Phrase detected, mark the start of the phrase*/
            else{
                constructingPhrase = true;
                phraseInConstruction = rawQuery[parser].substring(1,rawQuery[parser].length());
            }
            parser++;
        }
        return query;
    }
    public static void main(String[] args) throws IOException {
        FileManager manager = new FileManager();
        manager.createIndexFile(FileNameGenerator.DOCRECORDS);
        CrawlingManager cm = new CrawlingManager(manager);
        //cm.crawlOnce();

        String[] querylist = new BufferedReader(new InputStreamReader(System.in)).readLine().split(" ");//{"research","graduate","HKUST","science","education"};
        ArrayList<String> query = splitQuery(querylist);
        for(String s:query){System.out.println(s);}
        VectorSpaceRanker vectorSpaceRanker = new VectorSpaceRanker(manager);
        query = processQuery(query);


        HashMap<String,Double> term_based_result = vectorSpaceRanker.rank(query);
        ArrayList<String> term_based_result_rank = new ArrayList<>(term_based_result.keySet());
        term_based_result_rank.sort(Comparator.comparing(term_based_result::get));
        for(String docID:term_based_result_rank){
            System.out.print(ID_Mapping.PageID2URL(docID));
            System.out.println("    ->Cosine Similarity: "+term_based_result.get(docID));
        }
    }
}