package ResultProcessor;
import java.io.File;
import java.io.IOException;
import java.util.*;

import DataWareHouse.*;
import jdbm.htree.HTree;


public class termTitleRanker {
    private FileManager fileManager;
    private HashMap<String, Double> result = new HashMap<>();
    public termTitleRanker(FileManager manager){
        fileManager = manager;
    }
    public HashMap<String, Double> rank(ArrayList<String> query) throws IOException {
        /**Obtain all docs that contain terms in query*/
        for(String term:query){
            System.out.println("----Searching for occurence of "+term);
            HTree titleInvertedIndex = fileManager.getIndexFile(FileNameGenerator.getTitleInvertedIndexName(term)).getFile();
            String termID = ID_Mapping.Term2ID(term);

            if(titleInvertedIndex.get(termID)==null) {System.out.println("This term is not recorded in any titles");continue;}

            ArrayList<TitleIIPosting> docList = (ArrayList<TitleIIPosting>)titleInvertedIndex.get(termID);
            for(TitleIIPosting doc:docList){
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

    /**Get the weigh of a {term,doc_title} entry based on TF*IDF
     *
     * */
    private double calcTermWeight(String term, String docID) throws IOException {
        ArrayList<TitleIIPosting> TitleIIPostingList = (ArrayList<TitleIIPosting>)fileManager.getIndexFile(FileNameGenerator.getTitleInvertedIndexName(term)).getFile().get(ID_Mapping.Term2ID(term));

        double TF = 0;

        for(TitleIIPosting pos:TitleIIPostingList){
            if(pos.getID().equals(docID)){
                TF = pos.getPositions().size();
            }
        }
        if(TF==0) return 0; // term not in doc

        double DF = TitleIIPostingList.size();
        double IDF = Math.log(fileManager.getNumOfDoc()/DF)/Math.log(2);

        return (TF*IDF);
    }

    private double cosineSim(ArrayList<String> query, String docID) throws IOException {
        double dotProduct = 0;
        double squareQueryWeights=query.stream().distinct().count(); //assume weights in query are all 1
        double squareDocWeights=0;

        DocProfile docProfile = (DocProfile)fileManager.getIndexFile(FileNameGenerator.getDocRecordsName(docID)).getFile().get(docID);
        ArrayList<String> titleTerms = docProfile.getStemmedTitle();
        HashSet<String> distinctTitleTerms = new HashSet<>();
        for(String titleTerm: titleTerms){
            System.out.println(titleTerm);
            if(!distinctTitleTerms.add(titleTerm))
                continue;
            double weight = calcTermWeight(titleTerm,docID);
            squareDocWeights+=Math.pow(weight,2);
            if(query.contains(titleTerm)){
                dotProduct+=weight;
            }
        }
        return dotProduct/(Math.sqrt(squareQueryWeights)*Math.sqrt(squareDocWeights));
    }

}
