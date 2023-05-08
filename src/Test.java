import DataWareHouse.*;
import IRUtilities.StopStem;
import ResultProcessor.HITS;
import ResultProcessor.termTitleRanker;
import ResultProcessor.termContentRanker;
import ResultProcessor.phraseContentRanker;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.io.File;

public class Test {
    public static final double term_score_weight = 0.2;
    public static final double phrase_score_weight = 0.4;
    public static final double title_score_weight = 0.3;
    public static final double link_score_weight = 0.1;
    public static final String title_query = "title_query";
    public static final String phrase_query = "phrase_query";
    public static final String term_query = "term_query";


    public static ArrayList<String> processQuery(ArrayList<String> query){
        ArrayList<String> res = new ArrayList<>();
        StopStem stopStem = new StopStem("src/IRUtilities/stopwords.txt");
        for(String t: query){
            if(!t.contains(" ")){ // t is single term
                if(!stopStem.isStopWord(t.toLowerCase())&&!stopStem.stem(t).isBlank())
                    res.add(stopStem.stem(t));
            }
            else{// t is a phrase
                String[] tempphrase = t.split(" ");
                String phrase = "";
                for(int i=0;i< tempphrase.length;i++){
                    if(!stopStem.isStopWord(tempphrase[i].toLowerCase())&&!stopStem.stem(tempphrase[i]).isBlank()){
                        phrase = phrase.concat(stopStem.stem(tempphrase[i])+" ");
                    }
                }
                res.add(phrase);
            }
        }
        return res;
    }
    public static HashMap<String, ArrayList<String>> splitQuery(String[] rawQuery){
        HashMap<String, ArrayList<String>> processedQueries = new HashMap<>();
        ArrayList<String> termquery = new ArrayList<>();
        ArrayList<String> phrasequery = new ArrayList<>();
        ArrayList<String> titlequery = new ArrayList<>();
        int parser = 0;
        boolean constructingPhrase = false;
        String phraseInConstruction = "";
        while(parser< rawQuery.length){
            if(!rawQuery[parser].startsWith("\"")){
                if(!constructingPhrase){ /**[yes no "hong kong is good" crazy], we are at "no"*/
                    termquery.add(rawQuery[parser]);
                    titlequery.add(rawQuery[parser]);
                }
                else{ /**[yes no "hong kong is good" crazy], we are at "kong", phraseInConstruction = "hong"*/
                    if(!rawQuery[parser].endsWith("\"")){
                        phraseInConstruction=phraseInConstruction.concat(" "+rawQuery[parser]);
                    }
                    else{/**[yes no "hong kong is good" crazy], we are at "good", phraseInConstruction = "hong kong is"*/
                        phraseInConstruction=phraseInConstruction.concat(" "+rawQuery[parser].substring(0,rawQuery[parser].length()-1));
                        titlequery.add(rawQuery[parser].substring(0,rawQuery[parser].length()-1));
                        constructingPhrase = false;
                        //System.out.println("Phrase in construction: "+phraseInConstruction);
                        phrasequery.add(phraseInConstruction);
                    }
                }
            }
            /**Phrase detected, mark the start of the phrase*/
            else{
                constructingPhrase = true;
                phraseInConstruction = rawQuery[parser].substring(1,rawQuery[parser].length());
                titlequery.add(rawQuery[parser].substring(1,rawQuery[parser].length()));
            }
            parser++;
        }
        processedQueries.put(term_query,termquery);
        processedQueries.put(title_query,titlequery);
        processedQueries.put(phrase_query,phrasequery);
        return processedQueries;
    }
    public static void main(String[] args) throws IOException {
        FileManager manager;

        File f = new File("record_manager_IDs.txt");
        if(!f.exists()) {
            manager = new FileManager();
            manager.createIndexFile(FileNameGenerator.DOCRECORDS);
            CrawlingManager cm = new CrawlingManager(manager);
            long startTime = System.nanoTime();
            cm.crawlOnce();
            long endTime = System.nanoTime();
            System.out.println("Crawling took "+((endTime-startTime)/1000000000)+" seconds");
        }
        else{
            System.out.println("LOAD EXISTING DATA");
            manager = new FileManager("record_manager_IDs.txt");
        }

        while(true){
            System.out.println("Please input query");
            String[] querylist = new BufferedReader(new InputStreamReader(System.in)).readLine().split(" ");//{"research","graduate","HKUST","science","education"};
            HashMap<String, ArrayList<String>> processedQueries = splitQuery(querylist);
            //for(String s:query){System.out.println(s);}
            /**debug
            System.out.println("Query after being splitted");
            for(String q:query){
                System.out.print(q+" ---- ");
            }System.out.println();**/
            ArrayList<String> titleQuery = processQuery(processedQueries.get(title_query));
            ArrayList<String> termQuery = processQuery(processedQueries.get(term_query));
            ArrayList<String> phraseQuery = processQuery(processedQueries.get(phrase_query));
            /**debug**/
            System.out.println("Term Query after being stemmed");
            for(String q:termQuery){
                System.out.print(q+" ---- ");
            }System.out.println();

            System.out.println("Phrase Query after being stemmed");
            for(String q:phraseQuery){
                System.out.print(q+" ---- ");
            }System.out.println();

            System.out.println("Title Query after being stemmed");
            for(String q:titleQuery){
                System.out.print(q+" ---- ");
            }System.out.println();


            termContentRanker vectorSpaceRanker = new termContentRanker(manager);
            HashMap<String,Double> term_based_result = vectorSpaceRanker.rank(termQuery);

            termTitleRanker titleRanker = new termTitleRanker(manager);
            HashMap<String,Double> title_based_result = titleRanker.rank(titleQuery);

            phraseContentRanker phraseRanker = new phraseContentRanker(manager, phraseQuery);
            HashMap<String,Double> phrase_based_result = phraseRanker.rank();

            ArrayList<String> term_based_result_rank = new ArrayList<>(term_based_result.keySet());
            term_based_result_rank.sort(Comparator.comparing(term_based_result::get));
            Collections.reverse(term_based_result_rank);
            /**for(String docID:term_based_result_rank){
                System.out.print(ID_Mapping.PageID2URL(docID));
                System.out.println("    ->Normalized Similarity Score: "+term_based_result.get(docID));
            }*/

            HITS hitsranker = new HITS(manager,term_based_result_rank);
            HashMap<String,Double> hits_rank_result = hitsranker.rank(0.01);
            ArrayList<String> hits_based_result_rank = new ArrayList<>(hits_rank_result.keySet());
            hits_based_result_rank.sort(Comparator.comparing(hits_rank_result::get));
            Collections.reverse(hits_based_result_rank);
            /**for(String docID:hits_based_result_rank){
                System.out.print(ID_Mapping.PageID2URL(docID));
                System.out.println("    ->Authority Value: "+hits_rank_result.get(docID));
            }*/

            HashMap<String, Double> totalResult = new HashMap<>();
            for(String docID: term_based_result.keySet()){
                totalResult.put(docID, term_score_weight*term_based_result.get(docID));
            }
            for(String docID: phrase_based_result.keySet()){
                if(totalResult.containsKey(docID))
                    totalResult.put(docID, totalResult.get(docID)+phrase_score_weight*phrase_based_result.get(docID));
                else
                    totalResult.put(docID, phrase_score_weight*phrase_based_result.get(docID));
            }
            for(String docID: title_based_result.keySet()){
                if(totalResult.containsKey(docID))
                    totalResult.put(docID, totalResult.get(docID)+title_score_weight*title_based_result.get(docID));
                else
                    totalResult.put(docID, title_score_weight*title_based_result.get(docID));
            }
            for(String docID: hits_rank_result.keySet()){
                if(totalResult.containsKey(docID))
                    totalResult.put(docID, totalResult.get(docID)+link_score_weight*hits_rank_result.get(docID));
                else
                    totalResult.put(docID, link_score_weight*hits_rank_result.get(docID));
            }
            ArrayList<String> final_rank = new ArrayList<>(totalResult.keySet());
            final_rank.sort(Comparator.comparing(totalResult::get));
            Collections.reverse(final_rank);
            for(String docID: final_rank){
                DocProfile profile= (DocProfile)manager.getIndexFile(FileNameGenerator.DOCRECORDS).getFile().get(docID);
                if(profile==null) continue;
                System.out.println("-----------------------------------------");
                System.out.println("Title: "+profile.getTitle());
                System.out.println("Last Modified on: "+profile.getLastModified());
                System.out.println("URL: "+ID_Mapping.PageID2URL(docID));
                System.out.println("Score: "+totalResult.get(docID));
                System.out.println("-----------------------------------------");
            }
        }

    }
}