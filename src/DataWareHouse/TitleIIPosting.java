package DataWareHouse;


import java.util.ArrayList;

/**
 * Term -> ArrayList[TitleIIPosting(doc1), TitleIIPosting(doc2), ...]
 * */
public class TitleIIPosting extends Posting{
    private ArrayList<Integer> positions;
    public TitleIIPosting(String docID){
        super(docID);
        positions = new ArrayList<>();
    }
    public void addPosition(int position){
        positions.add(position);
    }
    public ArrayList<Integer> getPositions(){
        return positions;
    }
}
