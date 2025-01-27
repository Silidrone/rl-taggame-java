package chapter2.taggame;

public class TagGameAction {
    public int rot;
    public int speed;

    public TagGameAction(int rot, int speed) {
        this.rot = rot;
        this.speed = speed;
    }

    @Override
    public String toString() {
        return "(rotation: " + rot + ", speed: " + speed + ")";
    }
}