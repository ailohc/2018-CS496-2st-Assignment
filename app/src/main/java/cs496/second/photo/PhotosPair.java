package cs496.second.photo;

public class PhotosPair {
    String baseString, time;

    public PhotosPair(String baseString, String time){
        this.baseString = baseString;
        this.time = time;
    }

    public String getBaseString(){
        return baseString;
    }

    public String getTime(){
        return time;
    }

    public void setBaseString(String baseString) {
        this.baseString = baseString;
    }

    public void setTime(String time) {
        this.time = time;
    }
}


