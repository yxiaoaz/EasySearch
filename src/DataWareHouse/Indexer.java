package DataWareHouse;
import IRUtilities.StopStem;
import org.jsoup.Jsoup;
import jdbm.htree.HTree;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.StringTokenizer;

public class Indexer {
    //The database the indexer is serving
    private FileManager fileManager;
    private static StopStem stopStem;


    public Indexer(FileManager fileManager) throws IOException {
        this.fileManager = fileManager;
        if(stopStem == null)
            stopStem = new StopStem("src/IRUtilities/stopwords.txt");
    }

    /**
     * The driving method of the whole processing action
     @param url: where to fetch all the info from
      * */
    public void processContent(URL url) throws IOException {
        /**Step 1: save the URL to the docRecords*/
        // When we reach this stage, the URL is already valid to record
        System.out.println("Processing new url: "+url);
        String docID = ID_Mapping.URL2ID(url);

        HTree docRecords = fileManager.getIndexFile(FileNameGenerator.DOCRECORDS).getFile();
        Date lastModified = url.openConnection().getLastModified()!=0
                ?new Date(url.openConnection().getLastModified())
                :new Date(url.openConnection().getDate());
        System.out.println(lastModified);
        String title = Jsoup.connect(url.toString()).get().title();
        int size = url.openConnection().getContentLength()!=-1
                ?url.openConnection().getContentLength()
                :Jsoup.connect(url.toString()).get().body().text().length();

        DocProfile profile = new DocProfile(ID_Mapping.URL2ID(url),lastModified,title,size);
        docRecords.put(docID,profile);

        DocProfile test = (DocProfile) docRecords.get(docID);
        System.out.println("Test: just added webpage title: "+test.getTitle());

        /**Step 2: fetch terms and update inverted index & forward index
         *         fetch links and update webgraph+
         **/
        ArrayList<String> terms = extractTerms(url);
        for(int i=0;i< terms.size();i++){
            String t = terms.get(i);
            indexTerm(t,i,url);
            indexDoc(t,url);
        }

    }

    /**
     * @param url: the link from which terms are extracted
     * @return a list of stemmed terms extracted from the url
     * */
    public ArrayList<String> extractTerms(URL url) throws IOException {
        ArrayList<String> res = new ArrayList<>();

        Document doc = Jsoup.connect(url.toString()).get();
        String text = doc.body().text();
        StringTokenizer st = new StringTokenizer(text);
        while (st.hasMoreTokens()) {
            String t = st.nextToken();
            if(!stopStem.isStopWord(t.toLowerCase())&&!stopStem.stem(t).isBlank())
                res.add(stopStem.stem(t));
        }
        return res;
    }

    /** Extract all child links of a given url
     * @param url : the link from which child links are extracted
     * @return : the child links of the input url
     * */
    public ArrayList<URL> extractLinks(URL url) throws IOException {
        ArrayList<URL> v_link = new ArrayList<URL>();
        //Get Document object after parsing the html from given url.
        Document document = Jsoup.connect(url.toString()).get();
        if(document==null) return new ArrayList<URL>();
        /**
        Document document = null;
        try {
            document = Jsoup.connect(url.toString()).get();
        } catch (IOException e) {
            System.out.println("Exception when calling Jsoup.connect.get()");
            System.out.println("jsoup.connect is null? "+ (Jsoup.connect(url.toString())==null));
            System.out.println("jsoup.connect.get is null? "+ (document==null));
        }*/

        //Get links from document object.
        Elements links = document.select("a[href]");
        System.out.println("This URL has "+ links.size()+ " childlinks");
        for(int i=0;i< links.size();i++){
            //System.out.println(links.get(i).attr("abs:href"));
            try{
                URL extracted = new URL(links.get(i).attr("abs:href"));
                //System.out.println("New child link: "+url);
                if(!extracted.equals(url)){
                    updateLinkGraph(url,extracted);
                    v_link.add(extracted);
                    System.out.println("New child link: "+extracted);
                }
            }catch(IOException e){
                System.out.println("Exception when updating web graph");
            }
        }
        return v_link;
    }

    /**Update the parent-child link relation graph (in the Web Graph File)
     * @param parent : the parent link
     * @param child : the child link
     * */
    public void updateLinkGraph(URL parent, URL child) throws IOException {
        //create file on demand
        String supposedParent2ChildGraphName = FileNameGenerator.getWebGraphName_parent2child(parent);
        String supposedChild2ParentGraphName = FileNameGenerator.getWebGraphName_child2parent(child);
        if(!fileManager.fileExists(supposedParent2ChildGraphName))
            fileManager.createIndexFile(supposedParent2ChildGraphName);
        if(!fileManager.fileExists(supposedChild2ParentGraphName))
            fileManager.createIndexFile(supposedChild2ParentGraphName);

        while(fileManager.getIndexFile(supposedParent2ChildGraphName).getFile()==null && fileManager.getIndexFile(supposedChild2ParentGraphName).getFile()==null){

        }
        HTree parent2child = fileManager.getIndexFile(supposedParent2ChildGraphName).getFile();
        HTree child2parent = fileManager.getIndexFile(supposedChild2ParentGraphName).getFile();
        String parentID = ID_Mapping.URL2ID(parent);
        String childID = ID_Mapping.URL2ID(child);

        ArrayList<String> childrenList;
        /**if there is no entry for this parent yet*/
        if(parent2child.get(parentID)==null){
            childrenList = new ArrayList<>();
            childrenList.add(childID);
        }
        else{
            childrenList = (ArrayList<String>) parent2child.get(parentID);
            if(!childrenList.contains(childID))
                childrenList.add(childID);
        }
        parent2child.put(parentID,childrenList);

        ArrayList<String> parentList;
        /**if there is no entry for this parent yet*/
        if(child2parent.get(childID)==null){
            parentList = new ArrayList<>();
            parentList.add(parentID);
        }
        else{
            parentList = (ArrayList<String>) child2parent.get(childID);
            if(!parentList.contains(parentID))
                parentList.add(parentID);
        }
        child2parent.put(childID,parentList);
    }

    /**Test if a URL is valid for fetching: either it is not recorded, or it is recorded but has a new lastModifiedDate
     * @param url: the target URL
     * @return : true if url is good to process, false otherwise
     * */
    public boolean validURL(URL url) throws IOException {
        HTree docRecords = fileManager.getIndexFile(FileNameGenerator.DOCRECORDS).getFile();
        while(fileManager.getIndexFile(FileNameGenerator.DOCRECORDS).getFile()==null){

        }
        if(docRecords.get(ID_Mapping.URL2ID(url))!=null){
            DocProfile docProfile = (DocProfile) docRecords.get(ID_Mapping.URL2ID(url));
            Date recordedModifiedDate = docProfile.getLastModified();


            if(url.openConnection()==null||Jsoup.connect(url.toString())==null ||Jsoup.connect(url.toString()).get()==null ) //
                return false;

            //the last modified date of this page
            // if ==0, use the "date" field instead
            Date lastModified = url.openConnection().getLastModified()!=0?
                    new Date(url.openConnection().getLastModified())
                    :new Date(url.openConnection().getDate());
            if(!lastModified.after(recordedModifiedDate))
                return false;
        }
        return true;
    }

    /**Index a Single Term
     * @param term: the term to index
     * @param url: the page that the input term appears in
     * */
    public void indexTerm(String term, int position, URL url) throws IOException {
        String supposedFileName = FileNameGenerator.getInvertedIndexFileName(term);
        if(!fileManager.fileExists(supposedFileName))
            fileManager.createIndexFile(supposedFileName);
        while(fileManager.getIndexFile(supposedFileName).getFile()==null){

        }
        HTree invertedIndex = fileManager.getIndexFile(supposedFileName).getFile();
        String termID = ID_Mapping.Term2ID(term);
        String docID = ID_Mapping.URL2ID(url);

        // acquire(construct new) corresponding posting list
        ArrayList<IIPosting> postingList;
        if(invertedIndex.get(termID)==null){
            postingList = new ArrayList<>();

            //to be included in a IIPosting: which document, where in the document
            IIPosting newPosting = new IIPosting(docID);
            newPosting.addPosition(position);
            postingList.add(newPosting);

            fileManager.numTermAddOne();
        }
        else{
            postingList = (ArrayList<IIPosting>) invertedIndex.get(termID);
            boolean foundDoc = false;
            for(int j = 0;j<postingList.size();j++){
                if (postingList.get(j).getID().equals(docID)){
                    postingList.get(j).addPosition(position);
                    foundDoc = true;
                    break;
                }
            }
            if(!foundDoc){
                IIPosting newPosting = new IIPosting(docID);
                newPosting.addPosition(position);
            }
        }

        invertedIndex.put(termID,postingList);
    }

    /**
     * @param url : the key for which the FORWARD INDEX posting is updated
     * @param term : the occurence of term in the url to update
     * */
    public void indexDoc(String term, URL url) throws IOException {
        String supposedFileName = FileNameGenerator.getForwardIndexFileName(url);
        if(!fileManager.fileExists(supposedFileName))
            fileManager.createIndexFile(supposedFileName);
        while(fileManager.getIndexFile(supposedFileName).getFile()==null){

        }
        HTree forwardIndex = fileManager.getIndexFile(supposedFileName).getFile();
        String termID = ID_Mapping.Term2ID(term);
        String docID = ID_Mapping.URL2ID(url);

        ArrayList<FIPosting> postingList;
        if(forwardIndex.get(docID)==null){// first time index this doc into the forward index
            postingList = new ArrayList<>();
            FIPosting newPosting = new FIPosting(termID);
            newPosting.addOccurence();
            postingList.add(newPosting);
            fileManager.numDocAddOne();
        }
        else{//URL already has key in forward index
            postingList = (ArrayList<FIPosting>) forwardIndex.get(docID);
            boolean foundTerm=false;
            for(int j = 0;j<postingList.size();j++){
                if (postingList.get(j).getID().equals(termID)){
                    postingList.get(j).addOccurence();
                    foundTerm=true;
                    break;
                }
            }
            if(!foundTerm){
                FIPosting newPosting = new FIPosting(termID);
                newPosting.addOccurence();
                postingList.add(newPosting);
            }
        }
        Collections.sort(postingList);
        forwardIndex.put(docID,postingList);

    }

}
