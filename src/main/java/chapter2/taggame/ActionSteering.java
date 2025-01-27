package chapter2.taggame;

import chapter2.StaticInfo;
import chapter2.SteeringBehaviour;
import math.geom2d.Vector2D;

public class ActionSteering implements SteeringBehaviour {
    TagGameAction action;
    public ActionSteering(TagGameAction a) {
        this.action = a;
    }

    @Override
    public Vector2D getVelocity(StaticInfo staticInfo, Vector2D currentVelocity) {
        return currentVelocity.rotate(Math.toRadians(action.rot)).normalize().times(action.speed);
    }
}
