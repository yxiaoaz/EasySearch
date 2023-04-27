package ResultProcessor;
import DataWareHouse.*;
import jdbm.htree.HTree;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class VectorSpaceCalculator {
    private FileManager fileManager;

    /** Return {docID: similarity score} given a query
     * @param query : the query received
     */
    private HashMap<String, Double> run(ArrayList<String> query) throws IOException {
        HashMap<String, Double> result = new HashMap<>();
        /**Obtain all docs that contain terms in query*/
        for(String term:query){
            HTree invertedIndex = fileManager.getIndexFile(FileNameGenerator.getInvertedIndexFileName(term)).getFile();
            String termID = ID_Mapping.Term2ID(term);
            ArrayList<IIPosting> docList = (ArrayList<IIPosting>) invertedIndex.get(termID);

            if(docList==null) continue;
            for(IIPosting doc:docList){
                result.put(doc.getID(),0.0);
            }
        }

        /**for each doc, calculate similarity with query*/
        for(String docID: result.keySet()){
            result.put(docID,cosineSim(query,docID));
        }

        return result;
    }

    /**Get the weigh of a {term,doc} entry based on TF*IDF/max(TF)
     * Elements needed: TF(from FIPosting of doc), IDF(from IIPosting of term), maxTF(1st element of FIPosting list of doc)
     * */
    private double calcTermWeight(String term, String docID) throws IOException {

        return 0.0;
    }

    private double cosineSim(ArrayList<String> query, String docID){
        return 0.0;
    }
}
