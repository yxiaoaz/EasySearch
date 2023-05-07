package ResultProcessor;
import DataWareHouse.FileManager;
import DataWareHouse.FileNameGenerator;
import DataWareHouse.ID_Mapping;
import DataWareHouse.IIPosting;
import jdbm.htree.HTree;

import java.io.IOException;
import java.util.*;
public class phraseContentRanker {
    private ArrayList<String> query;
    private FileManager fileManager;
    private int threshold;
    private HashMap<String, Double> result;
    public phraseContentRanker(FileManager manager, ArrayList<String> query,int threshold ){
        fileManager=manager;
        this.query = query;
        this.threshold = threshold;
        result = new HashMap<>();
    }

    public HashMap<String, Double> rank() throws IOException {
        HashSet<String> allDocs = new HashSet<>();

        return result;
    }

    /** Calculate the score of a single document againts a single phrase query
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
                int duplicateOccurenceIndex = tempWindow.indexOf(position2term.get(orderedPosition.get(i+1)));
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


        return 0.0;
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

        return doc.getPositions();
    }
}
