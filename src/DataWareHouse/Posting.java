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

class PagePosting extends Posting{

    public PagePosting(String ID) {
        super(ID);
    }

}

class TermPosting extends Posting{

    public TermPosting(String ID) {
        super(ID);
    }
}

