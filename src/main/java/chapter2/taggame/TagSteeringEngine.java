package chapter2.taggame;

import chapter2.SteeringBehaviour;

@FunctionalInterface
public interface TagSteeringEngine {
    SteeringBehaviour getSteeringBehavior(TagPlayer player);
}