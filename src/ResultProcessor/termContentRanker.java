package ResultProcessor;
import DataWareHouse.*;
import jdbm.htree.HTree;
import java.lang.Math;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class termContentRanker {
    private FileManager fileManager;
    private HashMap<String, Double> result = new HashMap<>();

    public termContentRanker(FileManager manager){fileManager=manager;}

    /** Return {docID: similarity score} given a query
     * @param query : the query received (STEMMED)
     */
    public HashMap<String, Double> rank(ArrayList<String> query) throws IOException {
        /**Obtain all docs that contain terms in query*/
        for(String term:query){
            System.out.println("----Searching for occurence of "+term);
            HTree invertedIndex = fileManager.getIndexFile(FileNameGenerator.getInvertedIndexFileName(term)).getFile();
            String termID = ID_Mapping.Term2ID(term);

            if(invertedIndex.get(termID)==null) {System.out.println("This term is not recored");continue;}

            ArrayList<IIPosting> docList = (ArrayList<IIPosting>)invertedIndex.get(termID);
            for(IIPosting doc:docList){
                System.out.println("--------Contained in this url: "+ID_Mapping.PageID2URL(doc.getID()));
                result.put(doc.getID(),0.0);
            }
        }

        /**for each doc, calculate similarity with query*/
        System.out.println("There are "+ result.keySet().size() + " URLs in ranking");
        for(String docID: result.keySet()){
            result.put(docID,cosineSim(query,docID));
            System.out.println("Doc: "+ID_Mapping.PageID2URL(docID)+" Cosine Sim: "+cosineSim(query,docID));
        }

        result = normalizeResult(result);

        return result;
    }

    private HashMap<String, Double> normalizeResult(HashMap<String, Double> originalResult){
        double sum = 0.0;
        for(Double score: originalResult.values()){
            sum+=score;
        }
        for(String docID: originalResult.keySet()){
            originalResult.put(docID, originalResult.get(docID)/sum);
        }
        return originalResult;
    }

    /**Get the weigh of a {term,doc} entry based on TF*IDF/max(TF)
     * Elements needed: TF(from FIPosting of doc), IDF(from IIPosting of term), maxTF(1st element of FIPosting list of doc)
     * */
    private double calcTermWeight(String term, String docID) throws IOException {
        ArrayList<FIPosting> FIPostingList = (ArrayList<FIPosting>)fileManager.getIndexFile(FileNameGenerator.getForwardIndexFileName(docID)).getFile().get(docID);
        ArrayList<IIPosting> IIPostingList = (ArrayList<IIPosting>)fileManager.getIndexFile(FileNameGenerator.getInvertedIndexFileName(term)).getFile().get(ID_Mapping.Term2ID(term));

        double TF = 0;

        for(FIPosting pos:FIPostingList){
            if(pos.getID().equals(ID_Mapping.Term2ID(term))){
                TF = pos.getOccurence();
            }
        }
        if(TF==0) return 0; // term not in doc

        double maxTF = FIPostingList.get(0).getOccurence();
        double DF = IIPostingList.size();
        double IDF = Math.log(fileManager.getNumOfDoc()/DF)/Math.log(2);

        return (TF*IDF)/maxTF;
    }

    private double cosineSim(ArrayList<String> query, String docID) throws IOException {
        double dotProduct = 0;
        double squareQueryWeights=query.size(); //assume weights in query are all 1
        double squareDocWeights=0;

        ArrayList<FIPosting> FIPostingList = (ArrayList<FIPosting>)fileManager.getIndexFile(FileNameGenerator.getForwardIndexFileName(docID)).getFile().get(docID);
        for(FIPosting posting:FIPostingList){
            String term = ID_Mapping.TermID2Term(posting.getID());
            double weight = calcTermWeight(term,docID);
            squareDocWeights+=Math.pow(weight,2);
            if(query.contains(term)){
                dotProduct+=weight;
            }
        }
        return dotProduct/(Math.sqrt(squareQueryWeights)*Math.sqrt(squareDocWeights));
    }
}
