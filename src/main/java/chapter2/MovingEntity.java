package chapter2;


import math.geom2d.Vector2D;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.state.StateBasedGame;

import java.awt.geom.Rectangle2D;

public class MovingEntity extends BasicGameEntity {
    public final static double MAX_VELOCITY = 30;
    private final static float TIME_COEFFICIENT = 0.03f;
    public final static Vector2D noVelocity = new Vector2D(0, 0);
    public final static SteeringBehaviour idleSteering = (staticInfo, currentVelocity) -> noVelocity;

    Vector2D velocity = noVelocity;
    Rectangle2D boundary;
    SteeringBehaviour steeringBehaviour = idleSteering;

    public MovingEntity(StaticInfo staticInfo, Renderable body) {
        super(staticInfo, body);
    }

    public void setVelocity(Vector2D velocity) {
        if (velocity.norm() > MAX_VELOCITY) {
            velocity = velocity.normalize().times(MAX_VELOCITY);
        }

        this.velocity = velocity;
    }

    public Vector2D getVelocity() {
        return velocity;
    }

    public void setSteeringBehavior(SteeringBehaviour _steeringBehavior) {
        this.steeringBehaviour = _steeringBehavior;
    }

    @Override
    public void init(GameContainer gameContainer, StateBasedGame stateBasedGame, Rectangle2D boundary) {
        super.init(gameContainer, stateBasedGame, boundary);
        this.boundary = boundary;
    }

    @Override
    public void update(GameContainer gameContainer, StateBasedGame stateBasedGame, float time) {
        super.update(gameContainer, stateBasedGame, time);

        if (steeringBehaviour != null) {
            setVelocity(steeringBehaviour.getVelocity(staticInfo, velocity));
        }

        handleBoundaryCollisions();

        time = time * TIME_COEFFICIENT;

        staticInfo.update(velocity, time);
    }

    private void handleBoundaryCollisions() {
        if ((staticInfo.getPos().x() + body.getRadius() >= boundary.getMaxX() && velocity.x() > 0)
                || (staticInfo.getPos().x() - body.getRadius() <= boundary.getMinX() && velocity.x() < 0)) {
            velocity = new Vector2D(velocity.x() * -1, velocity.y());
        }

        if ((staticInfo.getPos().y() + body.getRadius() >= boundary.getMaxY() && velocity.y() > 0)
                || (staticInfo.getPos().y() - body.getRadius() <= boundary.getMinY() && velocity.y() < 0)) {
            velocity = new Vector2D(velocity.x(), velocity.y() * -1);
        }
    }
}
