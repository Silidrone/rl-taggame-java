package chapter2.taggame;

import chapter2.*;
import math.geom2d.Point2D;
import org.json.JSONObject;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.StateBasedGame;
import org.newdawn.slick.Color;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TagArena extends MovementPanel {
    private static final int PLAYER_COUNT = 5;

    private List<TagPlayer> players;
    private TagPlayer tagPlayer;
    private TagPlayer oldTagPlayer;
    private Communicator communicator;
    private int RL_PLAYER_INDEX = 0;
    private String RL_PLAYER_NAME = "Sili";

    public TagArena() {
        super();
        this.players = new ArrayList<>();
        this.oldTagPlayer = null;
    }

    private void resetGame(GameContainer gameContainer, StateBasedGame stateBasedGame, int playerCount) throws SlickException {
        players.clear();
        removeAllEntities();
        Random rand = new Random();
        Color[] colors = {Color.blue, Color.green, Color.yellow, Color.magenta, Color.cyan, Color.orange, Color.pink};

        for (int i = 0; i < playerCount; i++) {
            TagPlayer player = new TagPlayer(
                    i == RL_PLAYER_INDEX ? RL_PLAYER_NAME : ("P" + (i + 1)),
                    new StaticInfo(new Point2D(rand.nextDouble() * (TagGame.DemoWidth - 200), rand.nextDouble() * (TagGame.DemoHeight - 200))),
                    colors[i % colors.length],
                    this
            );
            player.setSteeringBehavior(new DumbTagSteering(TagGame.DemoWidth, TagGame.DemoHeight, MovingEntity.MAX_VELOCITY, player));
            players.add(player);
            add(player);
        }
        TagPlayer taggedPlayer = players.get(Utils.randomInt(players.size()));
        setTag(taggedPlayer, taggedPlayer);
        super.init(gameContainer, stateBasedGame);
    }

    @Override
    public void init(GameContainer gameContainer, StateBasedGame stateBasedGame) throws SlickException {
        resetGame(gameContainer, stateBasedGame, PLAYER_COUNT);
        try {
            communicator = new Communicator();
        } catch (IOException e) {
            throw new SlickException("Failed to initialize communicator.", e);
        }
        gameContainer.setTargetFrameRate(20);
    }

    @Override
    public void update(GameContainer gameContainer, StateBasedGame stateBasedGame, int time) throws SlickException {
        try {
            String action = communicator.receiveAction();
            if (action == null || action.equalsIgnoreCase(Communicator.EXIT)) return;

            if (action.equals(Communicator.RESET)) {
                resetGame(gameContainer, stateBasedGame, PLAYER_COUNT);
            } else {
                TagGameAction gameAction = deserializeAction(action);
                applyAction(players.get(RL_PLAYER_INDEX), gameAction);
            }

            super.update(gameContainer, stateBasedGame, time);
            boolean tagChanged = handleTaggingLogic();
            sendSerializedGameState(players.get(RL_PLAYER_INDEX));

            if(tagChanged) {
                resetGame(gameContainer, stateBasedGame, PLAYER_COUNT);
            }
        } catch (IOException e) {
            try {
                communicator.close();
                gameContainer.exit();
            } catch (IOException ex) {
                throw new RuntimeException(e);
            }
        }
    }

    public List<TagPlayer> getPlayers() {
        return players;
    }

    private void applyAction(TagPlayer player, TagGameAction action) {
        System.out.println("Applying action: " + action);
//        player.applyAction(action.rot, action.speed);
    }

    private void sendSerializedGameState(TagPlayer me) {
        JSONObject gameState = new JSONObject();

        gameState.put("mv", Utils.toIntArray(Utils.toPoint(me.getVelocity())));
        gameState.put("tv", Utils.toIntArray(Utils.toPoint(tagPlayer.getVelocity())));

        Point2D myPosition = me.getStaticInfo().getPos();
        Point2D taggedPosition = tagPlayer.getStaticInfo().getPos();
        int distance = (int) taggedPosition.distance(myPosition);

        gameState.put("d", distance);
        gameState.put("tc", tagPlayer != oldTagPlayer);

        String newState = gameState.toString();
        System.out.println("Sending new state: " + newState);
        communicator.sendState(newState);
    }

    private TagGameAction deserializeAction(String s) {
        JSONObject json = new JSONObject(s);
        int angleDiff = json.getInt("a");
        int speed = json.getInt("s");
        return new TagGameAction(angleDiff, speed);
    }

    private boolean handleTaggingLogic() {
        for (TagPlayer player : players) {
            if (player != tagPlayer && tagPlayer.isTagging(player)) {
                setTag(tagPlayer, player);
                return true;
            }
        }

        return false;
    }

    private void setTag(TagPlayer oldPlayer, TagPlayer newPlayer) {
        oldTagPlayer = oldPlayer;
        tagPlayer = newPlayer;
        players.forEach((p) -> p.setIsTagged(p == newPlayer));
    }
}
