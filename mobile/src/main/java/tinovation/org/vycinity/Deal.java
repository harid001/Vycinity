package tinovation.org.vycinity;

/**
 * Created by Hari on 1/25/15.
 */
public class Deal {

    private String title,description;

    public Deal(String title,String description){
        this.title = title;
        this.description = description;
    }

    public String getTitle(){
        return title;
    }

    public String getDescription(){
        return description;
    }
}
