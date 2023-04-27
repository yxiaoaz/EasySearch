package ResultProcessor;
import DataWareHouse.*;
import jdbm.htree.HTree;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class VectorSpaceCalculator {
    private FileManager fileManager;

    /** Return {docID: similarity score} given a query
     *
     */
    private HashMap<String, Double> run(ArrayList<String> tokens) throws IOException {
        HashMap<String, Double> result = new HashMap<>();
        for(String term:tokens){
            HTree invertedIndex = fileManager.getIndexFile(FileNameGenerator.getInvertedIndexFileName(term)).getFile();
            String termID = ID_Mapping.Term2ID(term);
            ArrayList<IIPosting> docList = (ArrayList<IIPosting>) invertedIndex.get(termID);

            if(docList==null) continue;
            for(IIPosting doc:docList){
                result.put(doc.getID(),cosineSim(term,doc.getID()));
            }
        }
        return result;
    }

    private double calcTermWeight(String term) throws IOException {
        return 0.0;
    }

    private double cosineSim(String term, String docID){
        return 0.0;
    }
}
