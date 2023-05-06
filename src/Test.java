import DataWareHouse.FileManager;
import DataWareHouse.FileNameGenerator;
import DataWareHouse.ID_Mapping;
import IRUtilities.StopStem;
import ResultProcessor.HITS;
import ResultProcessor.termTitleRanker;
import ResultProcessor.termVectorSpaceRanker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

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
        long startTime = System.nanoTime();
        cm.crawlOnce();
        long endTime = System.nanoTime();
        System.out.println("Crawling took "+((endTime-startTime)/1000000000)+" seconds");
        while(true){
            System.out.println("Please input query");
            String[] querylist = new BufferedReader(new InputStreamReader(System.in)).readLine().split(" ");//{"research","graduate","HKUST","science","education"};
            ArrayList<String> query = splitQuery(querylist);
            //for(String s:query){System.out.println(s);}
            System.out.println("Starts searching");
            query = processQuery(query);


            /**termVectorSpaceRanker vectorSpaceRanker = new termVectorSpaceRanker(manager);
             * HashMap<String,Double> term_based_result = vectorSpaceRanker.rank(query);
             * */
            termTitleRanker titleRanker = new termTitleRanker(manager);
            HashMap<String,Double> term_based_result = titleRanker.rank(query);

            ArrayList<String> term_based_result_rank = new ArrayList<>(term_based_result.keySet());
            term_based_result_rank.sort(Comparator.comparing(term_based_result::get));
            Collections.reverse(term_based_result_rank);
            for(String docID:term_based_result_rank){
                System.out.print(ID_Mapping.PageID2URL(docID));
                System.out.println("    ->Normalized Cosine Similarity: "+term_based_result.get(docID));
            }

            HITS hitsranker = new HITS(manager,term_based_result_rank);
            HashMap<String,Double> hits_rank_result = hitsranker.rank(0.01);
            ArrayList<String> hits_based_result_rank = new ArrayList<>(hits_rank_result.keySet());
            hits_based_result_rank.sort(Comparator.comparing(hits_rank_result::get));
            Collections.reverse(hits_based_result_rank);
            for(String docID:hits_based_result_rank){
                System.out.print(ID_Mapping.PageID2URL(docID));
                System.out.println("    ->Authority Value: "+hits_rank_result.get(docID));
            }
        }

    }
}