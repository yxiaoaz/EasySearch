package DataWareHouse;

public class Posting {
    protected String ID;

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Posting){
            return ID == ((Posting) obj).getID();
        }
        return false;
    }

    public String getID() {
        return ID;
    }
}

class PagePosting extends Posting{

}

class TermPosting extends Posting{

}

