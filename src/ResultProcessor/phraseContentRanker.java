package ResultProcessor;
import DataWareHouse.*;
import jdbm.htree.HTree;

import java.io.IOException;
import java.util.*;
public class phraseContentRanker {
    private ArrayList<String> query;
    private FileManager fileManager;
    private HashMap<String, Double> result;
    public phraseContentRanker(FileManager manager, ArrayList<String> query){
        fileManager=manager;
        this.query = query;
        result = new HashMap<>();
    }

    public HashMap<String, Double> rank() throws IOException {
        System.out.println("---------------------Phrase Ranking-------------------------");
        System.out.println("------------------------------------------------------------");
        System.out.println("------------------------------------------------------------");
        System.out.print("Phrase content ranker task received: ");
        for(String q:query){
            System.out.print(q+" ---- ");
        }System.out.println();
        for(String mergedPhrase: query){
            System.out.println("--Decomposing phrase: "+mergedPhrase);

            String[] tempphrase = mergedPhrase.split(" ");
            ArrayList<String> phrase = new ArrayList<>();
            for(int i=0;i< tempphrase.length;i++){
                phrase.add(tempphrase[i]);
            }
            HashSet<String> relatedDocs = new HashSet<>();

            for(String term:phrase){
                System.out.println("----Compositing term: "+term);
                ArrayList<IIPosting> IIPostingList = (ArrayList<IIPosting>)fileManager.getIndexFile(FileNameGenerator.getInvertedIndexFileName(term)).getFile().get(ID_Mapping.Term2ID(term));
                if(IIPostingList==null) continue;
                for(IIPosting posting: IIPostingList){
                    System.out.println("--------This term is found in : "+ID_Mapping.PageID2URL(posting.getID()));
                    relatedDocs.add(posting.getID());
                }
            }
            for(String docID: relatedDocs){
                if(result.containsKey(docID))
                    result.put(docID, result.get(docID)+calcScore(phrase, docID));
                else
                    result.put(docID, calcScore(phrase, docID));
            }
        }
        result = normalizeResult(result);
        System.out.println("------------------------------------------------------------");
        System.out.println("------------------------------------------------------------");
        System.out.println("------------------------------------------------------------");
        return result;
    }

    /** Calculate the score of a single document against a single phrase query
     * @param phrase : a list of composed terms of a phrase (some terms may occur more than once)
     * */
    public Double calcScore(ArrayList<String> phrase, String docID) throws IOException {
        HashMap<Integer, String> position2term = new HashMap<>();

        /**Fill position2term
         * Construct {Integer(position) -> String (term)} hashtable
         * */
        for(int i = 0;i<phrase.size();i++){
            if(position2term.containsValue(phrase.get(i))) continue;
            ArrayList<Integer> positions = getPositions(phrase.get(i),docID);
            if(positions==null) continue;
            for(Integer pos:positions){
                position2term.put(pos,phrase.get(i));
            }
        }

        /**Construct windows
         * ArrayList<ArrayList<String>> windows: [[term1, term2, term3], [term5, ...], ...]
         * ArrayList<ArrayList<Integer>> interval: [[position(term1), position(term2), ...],...]
         * */
        ArrayList<ArrayList<String>> windows = new ArrayList<>();
        ArrayList<ArrayList<Integer>> interval = new ArrayList<>();
        ArrayList<Integer> orderedPosition = new ArrayList<>(new TreeSet<>(position2term.keySet()));
        int max_dist = phrase.size()*2;
        ArrayList<String> tempWindow = new ArrayList<>();
        ArrayList<Integer> tempInterval = new ArrayList<>();
        for (int i=0;i< orderedPosition.size();i++) {
            // dist(term[i],term[i+1])>=max_dist
            if(tempWindow.size()>=1 && orderedPosition.get(i)-orderedPosition.get(i-1)>=max_dist){
                interval.add(new ArrayList<>(tempInterval));
                windows.add(new ArrayList<>(tempWindow));  /**Before this loop, tempWindow already contains orderedPosition.get(i-1)*/

                tempWindow = new ArrayList<>();
                tempInterval = new ArrayList<>();

                tempWindow.add(position2term.get(orderedPosition.get(i)));
                tempInterval.add(orderedPosition.get(i));
            }
            // term[i] == last record of tempWindow
            // end current window, start new window and add term[i]
            else if(tempWindow.size()>=1&&position2term.get(orderedPosition.get(i)).equals(tempWindow.get(tempWindow.size()-1))){
                interval.add(new ArrayList<>(tempInterval));
                windows.add(new ArrayList<>(tempWindow));

                tempWindow = new ArrayList<>();
                tempInterval = new ArrayList<>();

                tempWindow.add(position2term.get(orderedPosition.get(i)));
                tempInterval.add(orderedPosition.get(i));
            }
            //term[i] contained in tempWindow
            // compare dist(term[i], tempWindow(last)) & dist(pastoccurence, pastoccurence+1)
            else if(tempWindow.contains(position2term.get(orderedPosition.get(i)))){
                int duplicateOccurenceIndex = tempWindow.indexOf(position2term.get(orderedPosition.get(i)));
                // break between tempWindow(last) and term[i]
                if((orderedPosition.get(i)-orderedPosition.get(i-1)) >=
                        orderedPosition.get(duplicateOccurenceIndex+1) - orderedPosition.get(duplicateOccurenceIndex)){
                    interval.add(new ArrayList<>(tempInterval));
                    windows.add(new ArrayList<>(tempWindow));

                    tempWindow = new ArrayList<>();
                    tempInterval = new ArrayList<>();

                    tempWindow.add(position2term.get(orderedPosition.get(i)));
                    tempInterval.add(orderedPosition.get(i));
                }else{
                    ArrayList<String> lookbackWindow = new ArrayList<>();
                    ArrayList<Integer> intervallookbackWindow = new ArrayList<>();
                    for(int j=0;j<duplicateOccurenceIndex+1;j++){
                        lookbackWindow.add(tempWindow.get(j));
                        intervallookbackWindow.add(tempInterval.get(j));
                    }
                    for(int j=0;j<duplicateOccurenceIndex+1;j++){
                        tempWindow.remove(j);
                        tempInterval.remove(j);
                    }
                    windows.add(lookbackWindow);
                    interval.add(intervallookbackWindow);

                    tempWindow.add(position2term.get(orderedPosition.get(i)));
                    tempInterval.add(orderedPosition.get(i));
                }

            }
            else{
                tempWindow.add(position2term.get(orderedPosition.get(i)));
                tempInterval.add(orderedPosition.get(i));
            }

            // Wrap up when reached end
            if(i+1>=orderedPosition.size()){
                windows.add(new ArrayList<>(tempWindow));
                interval.add(new ArrayList<>(tempInterval));
            }
        }

        /**Assign weights for each window
         * Window Weight = [1+ num of terms in window/len(window)]^x, let x = 1.5    <---if num of terms>1
         *                  0  <---otherwise
         * */
        ArrayList<Double> windowWeights = new ArrayList<>();
        for(int i=0;i<windows.size();i++){
            if(windows.get(i).size()<=1){
                windowWeights.add(0.0);
                continue;
            }
            windowWeights.add(Math.pow(1+windows.get(i).size()/(interval.get(i).get(interval.get(i).size()-1)-interval.get(i).get(0)+1),1.5));
        }

        /**Calculate the weights of each term in the document*/
        HashMap<String, Double> termweights = new HashMap<>();
        for(String term: position2term.values()){
            ArrayList<IIPosting> IIPostingList = (ArrayList<IIPosting>)fileManager.getIndexFile(FileNameGenerator.getInvertedIndexFileName(term)).getFile().get(ID_Mapping.Term2ID(term));
            double DF = IIPostingList.size();
            double IDF = Math.log(fileManager.getNumOfDoc()/DF)/Math.log(2);
            double weight = 0;
            for(int i=0;i<windows.size();i++){
                if(windows.get(i).contains(term)){
                    weight+=IDF*windowWeights.get(i);
                }
            }
            if(termweights.containsKey(term)) termweights.put(term,termweights.get(term)+weight);
            else termweights.put(term,weight);
        }

        double sum = 0;
        for(String term: termweights.keySet()){
            sum+=termweights.get(term);
        }

        return sum;
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

    /**
     * Return the list of occurences of a term in a document
     * */
    private ArrayList<Integer> getPositions(String term, String docID) throws IOException {
        String termID = ID_Mapping.Term2ID(term);
        HTree invertedIndex = fileManager.getIndexFile(FileNameGenerator.getInvertedIndexFileName(term)).getFile();
        ArrayList<IIPosting> docList = (ArrayList<IIPosting>)invertedIndex.get(termID);
        // if docList==null ?
        if(docList == null) return null;

        IIPosting doc = null;
        for(IIPosting docs: docList){
            if(docs.getID().equals(docID)){
                doc = docs;
                break;
            }
        }
        if(doc==null) return null;
        return doc.getPositions();
    }
}
