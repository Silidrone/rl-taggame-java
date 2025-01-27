package chapter2.taggame;

import org.json.JSONObject;

public class TagGameAction {
    public int rot;
    public int speed;

    public TagGameAction(String s) { // deserialize
        JSONObject json = new JSONObject(s);
        rot = json.getInt("a");
        speed = json.getInt("s");
    }

    @Override
    public String toString() {
        return "(rotation: " + rot + ", speed: " + speed + ")";
    }
}