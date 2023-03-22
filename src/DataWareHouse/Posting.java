package DataWareHouse;

import java.io.Serializable;
import java.util.ArrayList;

public class Posting implements Serializable {
    protected String ID;

    public Posting(String ID){
        this.ID=ID;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Posting){
            return ID.equals(((Posting) obj).getID());
        }
        return false;
    }

    public String getID() {
        return ID;
    }
}

/**
 * Term ID-> ArrayList<IIPosting>
 *
 * */
class IIPosting extends Posting{

    // the position of terms in this document
    private ArrayList<Integer> position;

    /**ID: the id of the DOCUMENT
     * */
    public IIPosting(String ID) {
        super(ID);
        position = new ArrayList<>();
    }



}

class TermPosting extends Posting{

    public TermPosting(String ID) {
        super(ID);
    }
}

