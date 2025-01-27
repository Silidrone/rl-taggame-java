package chapter2.taggame;


import chapter2.*;
import math.geom2d.Point2D;
import math.geom2d.Vector2D;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.state.StateBasedGame;

public class TagPlayer {
    private static final float PLAYER_RADIUS = 20;
    private static final Color TAGCOLOR = Color.red;
    private static final int TAG_PLAYER_RADIUS = 23;
    public final static Vector2D noVelocity = new Vector2D(0, 0);
    public final static SteeringBehaviour idleSteering = (staticInfo, currentVelocity) -> noVelocity;

    StaticInfo staticInfo;
    Vector2D velocity = noVelocity;
    SteeringBehaviour steeringBehaviour = idleSteering;

    protected String name;
    protected Color color;
    protected boolean isTagged;

    public TagPlayer(String name, StaticInfo staticInfo, Color color) {
        this.name = name;
        this.color = color;
        this.staticInfo = staticInfo;
    }

    public StaticInfo getStaticInfo() { return staticInfo; }

    public void setVelocity(Vector2D velocity) {
        if (velocity.norm() > TagGame.MAX_VELOCITY) {
            velocity = velocity.normalize().times(TagGame.MAX_VELOCITY);
        }

        this.velocity = velocity;
    }

    public Vector2D getVelocity() {
        return velocity;
    }

    public void setSteeringBehavior(SteeringBehaviour _steeringBehavior) {
        this.steeringBehaviour = _steeringBehavior;
    }

    public void update(GameContainer gameContainer, StateBasedGame stateBasedGame, float time) {
        if (steeringBehaviour != null) {
            setVelocity(steeringBehaviour.getVelocity(staticInfo, velocity));
        }

        if ((staticInfo.getPos().x() + PLAYER_RADIUS >= TagGame.DemoWidth && velocity.x() > 0)
                || (staticInfo.getPos().x() - PLAYER_RADIUS <= 0 && velocity.x() < 0)) {
            velocity = new Vector2D(velocity.x() * -1, velocity.y());
        }

        if ((staticInfo.getPos().y() + PLAYER_RADIUS >= TagGame.DemoHeight && velocity.y() > 0)
                || (staticInfo.getPos().y() - PLAYER_RADIUS <= 0 && velocity.y() < 0)) {
            velocity = new Vector2D(velocity.x(), velocity.y() * -1);
        }

        time = time * TagGame.TIME_COEFFICIENT;

        staticInfo.update(velocity, time);
    }

    public void render(Graphics graphics) {
        Color c = graphics.getColor();
        var pos = staticInfo.getPos();
        double orientation = staticInfo.getOrientation();

        graphics.setColor(color);
        graphics.fillOval( (float)pos.x()-PLAYER_RADIUS, (float) pos.y()-PLAYER_RADIUS, 2*PLAYER_RADIUS,2*PLAYER_RADIUS);
        float x = (float)pos.x();
        float y = (float) pos.y();
        float ex = (float) (x + PLAYER_RADIUS*Math.cos(orientation));
        float ey = (float) (y + PLAYER_RADIUS*Math.sin(orientation));
        graphics.setColor(Color.black);
        graphics.drawLine(x,y,ex,ey);
        graphics.setColor(c);

        if (isTagged) {
            Point2D center = staticInfo.getPos();
            graphics.setColor(TAGCOLOR);
            graphics.setLineWidth(20);
            graphics.drawOval((float) center.x() - TAG_PLAYER_RADIUS, (float) center.y() - TAG_PLAYER_RADIUS, 2 * TAG_PLAYER_RADIUS, 2 * TAG_PLAYER_RADIUS);
            graphics.setLineWidth(1);
        }

        graphics.setColor(c);
    }

    public boolean isTagging(TagPlayer player) {
        return (PLAYER_RADIUS + PLAYER_RADIUS >= staticInfo.getPos().distance(player.staticInfo.getPos()));
    }

    public void setIsTagged(boolean v) {
        this.isTagged = v;
    }

    boolean isTagged() {
        return isTagged;
    }
}
