package collectionClasses;

import java.io.Serializable;

public class Coordinates implements Serializable {
    private static final long serialVersionUID = 6529685098267757690L;
    private Long x; //Поле не может быть null
    private Long y; //Поле не может быть null

    public Coordinates(Long xv, Long yv) {
        x=xv;
        y=yv;
    }
    String getCoordinates(){
        return x+";"+y;
    }
    public void setX(Long xW){
        x=xW;
    }

    public void setY(Long yW){
        y=yW;
    }
}
