import java.time.ZonedDateTime;

public class History {
    private String act;
    private java.time.ZonedDateTime actDate;

    public History(ZonedDateTime actDateV, String actV) {
        act=actV;
        actDate=actDateV;
    }

    String getHistory(){
        return actDate+"   "+act;
    }

    String getToWrite(){
        return actDate+";"+act+";";
    }
}

