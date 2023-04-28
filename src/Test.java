import DataWareHouse.FileManager;
import DataWareHouse.FileNameGenerator;
import DataWareHouse.ID_Mapping;
import IRUtilities.StopStem;
import ResultProcessor.VectorSpaceRanker;
import java.io.IOException;
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
    public static void main(String[] args) throws IOException {
        FileManager manager = new FileManager();
        CrawlingManager cm = new CrawlingManager(manager);
        cm.crawlOnce();
        String[] querylist = {"research","graduate","HKUST","science","education"};
        ArrayList<String> query = new ArrayList<>(List.of(querylist));

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
