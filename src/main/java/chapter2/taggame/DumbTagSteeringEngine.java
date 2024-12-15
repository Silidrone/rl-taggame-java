package chapter2.taggame;

import chapter2.StaticInfo;
import chapter2.SteeringBehaviour;
import math.geom2d.Point2D;
import math.geom2d.Vector2D;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.AbstractMap.SimpleEntry;

public class DumbTagSteeringEngine implements TagSteeringEngine {
    // parameters to my algorithm
    double safeDistanceThreshold = 0.2;
    double chasingCornerDistanceThreshold = 0.2;
    double cornerWeightMultiplier = 0.1;
    double chasingPrioritizationDotProduct = 0.9;

    // corners
    List<Point2D> corners;
    double maxVelocity;

    public DumbTagSteeringEngine(int DemoWidth, int DemoHeight, double maxVelocity) {
        corners = new ArrayList<>() {{
            add(new Point2D(0, 0));
            add(new Point2D(0, DemoHeight));
            add(new Point2D(DemoWidth, 0));
            add(new Point2D(DemoWidth, DemoHeight));
        }};
        final double bottomLeftTopRightDistance = Math.hypot(DemoWidth, DemoHeight);
        safeDistanceThreshold *= bottomLeftTopRightDistance;
        chasingCornerDistanceThreshold *= bottomLeftTopRightDistance;
        cornerWeightMultiplier *= bottomLeftTopRightDistance;
        this.maxVelocity = maxVelocity;
    }

    @Override
    public SteeringBehaviour getSteeringBehavior(TagPlayer me) {
        List<TagPlayer> otherPlayers = me.getArena().getPlayers().stream()
                .filter(p -> p != me)
                .toList();

        return (staticInfo, currentVelocity) -> getVelocity(staticInfo, currentVelocity, me, otherPlayers);
    }

    public Vector2D getVelocity(StaticInfo staticInfo, Vector2D currentVelocity, TagPlayer me, List<TagPlayer> otherPlayers) {
        Vector2D desiredVelocity = new Vector2D(0, 0);
        boolean isTagged = me.isTagged();
        Point2D myPosition = me.getStaticInfo().getPos();

        if (isTagged) {
            // If the player is tagged, chase the closest opponent
            TagPlayer targetOpponent = null;
            double closestVisibleDistance = Double.MAX_VALUE;
            double closestDistance = Double.MAX_VALUE;

            Vector2D directionToFace = currentVelocity.normalize();

            for (TagPlayer opponent : otherPlayers) {
                Point2D opponentPosition = opponent.getStaticInfo().getPos();
                double distance = myPosition.distance(opponentPosition);

                Vector2D toOpponent = new Vector2D(myPosition, opponentPosition).normalize();
                double dotProduct = Vector2D.dot(directionToFace, toOpponent);
                boolean isInView = dotProduct > chasingPrioritizationDotProduct;

                double cornerWeight = Math.max(0, 1.0 - (opponentPosition.distance(getNearestCorner(opponent)) / chasingCornerDistanceThreshold));

                if (isInView && distance < closestVisibleDistance) {
                    closestVisibleDistance = distance;
                    targetOpponent = opponent;
                } else if (distance - cornerWeight * cornerWeightMultiplier < closestDistance) {
                    closestDistance = distance;
                    targetOpponent = opponent;
                }
            }

            if (targetOpponent != null) {
                desiredVelocity = new Vector2D(myPosition, targetOpponent.getStaticInfo().getPos()).normalize().times(maxVelocity);
            }
        } else {
            // If not tagged, flee from the tagged opponent
            TagPlayer taggedOpponent = otherPlayers.stream().filter(TagPlayer::isTagged).findFirst().orElse(null);

            if (taggedOpponent != null) {
                Point2D opponentPosition = taggedOpponent.getStaticInfo().getPos();
                List<SimpleEntry<Point2D, Double>> orderedCorners = orderCornersByDistance(taggedOpponent);
                double distanceToOpponent = myPosition.distance(opponentPosition);

                double fleeWeight = Math.max(0, (safeDistanceThreshold - distanceToOpponent) / safeDistanceThreshold);
                double seekWeight = 1.0 - fleeWeight;
                Point2D furthestCornerFromOpponent = orderedCorners.get(orderedCorners.size() - 1).getKey();

                Vector2D desiredDirection = new Vector2D(myPosition, furthestCornerFromOpponent).normalize();
                Vector2D fromMeToOpponent =  new Vector2D(myPosition, opponentPosition).normalize();
                desiredVelocity = desiredDirection.times(seekWeight).plus(fromMeToOpponent.times(-fleeWeight));

                desiredVelocity = desiredVelocity.normalize().times(maxVelocity);
            }
        }

        return desiredVelocity;
    }

    private Point2D getNearestCorner(TagPlayer player) {
        return orderCornersByDistance(player).get(0).getKey();
    }

    private List<SimpleEntry<Point2D, Double>> orderCornersByDistance(TagPlayer player) {
        List<SimpleEntry<Point2D, Double>> distanceEntries = new ArrayList<>();

        for (Point2D corner : corners) {
            double distance = new Point2D(player.getStaticInfo().getPos().x(), player.getStaticInfo().getPos().y()).distance(corner);
            distanceEntries.add(new SimpleEntry<>(corner, distance));
        }

        distanceEntries.sort(Comparator.comparingDouble(SimpleEntry::getValue));

        return distanceEntries;
    }
}
