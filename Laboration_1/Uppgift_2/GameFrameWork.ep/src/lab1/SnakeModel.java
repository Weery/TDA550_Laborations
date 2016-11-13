package lab1;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Simple snake game for Laboration 1 - assignment 2 in the course TDA550
 * 
 * <p>
 * A snake is created at the center of the screen with one body part just below it.
 * Also a food for  the snake is placed in the arena which makes the snake grow in 
 * size if eaten. The game is over if the snake eats itself or it goes out of the arena.
 * The final score is determined by the number of foods the snake has eaten over the course
 * of the game
 */
public class SnakeModel extends GameModel {
	public enum Directions {
		EAST(1, 0),
		WEST(-1, 0),
		NORTH(0, -1),
		SOUTH(0, 1),
		NONE(0, 0);

		private final int xDelta;
		private final int yDelta;

		Directions(final int xDelta, final int yDelta) {
			this.xDelta = xDelta;
			this.yDelta = yDelta;
		}

		public int getXDelta() {
			return this.xDelta;
		}

		public int getYDelta() {
			return this.yDelta;
		}
		
		// Checks if two Directions are the opposites of each other
		public int opposite(final Object obj) {
			if (this == obj) {
				return -1;
			}
			if (obj == null || getClass() != obj.getClass()) {
				return -1;
			}
			Directions other = (Directions) obj;
			if (other.getXDelta() == -this.getXDelta())
				return 1;
			if (other.getYDelta() == -this.getYDelta())
				return 1; 
			return 0;
		}
	}

	

	/*
	 * The following GameTile objects are used only
	 * to describe how to paint the specified item.
	 * 
	 * This means that they should only be used in
	 * conjunction with the get/setGameboardState()
	 * methods.
	 */

	/** Graphical representation of the food. */
	private static final GameTile FOOD_TILE = new RoundTile(new Color(255, 215,
			0),
			new Color(255, 255, 0), 2.0);

	/** Graphical representation of the snake head */
	private static final GameTile HEAD_TILE = new DiamondTile(Color.BLACK, Color.GREEN,5.0);
	
	/** Graphical representation of the snake fat body */
	private static final GameTile DOUBLE_TILE = new RoundTile(Color.BLACK,
			Color.BLUE, 2.0,1.2);

	/** Graphical representation of the snake body */
	private static final GameTile BODY_TILE = new RoundTile(Color.BLACK,
			Color.RED, 2.0);

	/** Graphical representation of a blank tile. */
	private static final GameTile BLANK_TILE = new GameTile();

	/** A position of the food. */
	private Position foodPos;

	/** The list containing the positions of the whole snake. */
	private final Deque<Position> snake = new LinkedList<Position>();

	/** The direction and last direction of the head. */
	private Directions direction = Directions.NORTH;
	private Directions last_direction = Directions.EAST;

	/** The number of food found. */
	private int score;

	/**
	 * Create a new model for the gold game.
	 */
	public SnakeModel() {
		Dimension size = getGameboardSize();

		// Blank out the whole gameboard
		for (int i = 0; i < size.width; i++) {
			for (int j = 0; j < size.height; j++) {
				setGameboardState(i, j, BLANK_TILE);
			}
		}

		// Insert the snake head at the center of the arena
		this.snake.add(new Position(size.width / 2, size.height / 2));
		setGameboardState(this.snake.getFirst(), HEAD_TILE);

		// Inser one snake body south of the head
		this.snake.add(new Position(size.width / 2, size.height / 2-1));
		setGameboardState(this.snake.getFirst(), BODY_TILE);

		// Insert n food into the gameboard.
		foodPos=newFood();
	}

	/**
	 * Insert another food into the gameboard.
	 */
	private Position newFood() {
		Position newFoodPos;
		Dimension size = getGameboardSize();
		// Loop until a blank position is found and ...
		do {
			newFoodPos = new Position((int) (Math.random() * size.width),
										(int) (Math.random() * size.height));
		} while (!isPositionEmpty(newFoodPos));

		// ... add a new coin to the empty tile.
		setGameboardState(newFoodPos, FOOD_TILE);
		return newFoodPos;
	}

	/**
	 * Return whether the specified position is empty.
	 * 
	 * @param pos
	 *            The position to test.
	 * @return true if position is empty.
	 */
	private boolean isPositionEmpty(final Position pos) {
		return (getGameboardState(pos) == BLANK_TILE);
	}
	
	/**
	 * Update the direction of the collector
	 * according to the user's keypress.
	 */
	private void updateDirection(final int key) {
		switch (key) {
			case KeyEvent.VK_LEFT:
				this.direction = Directions.WEST;
				break;
			case KeyEvent.VK_UP:
				this.direction = Directions.NORTH;
				break;
			case KeyEvent.VK_RIGHT:
				this.direction = Directions.EAST;
				break;
			case KeyEvent.VK_DOWN:
				this.direction = Directions.SOUTH;
				break;
			default:
				// Don't change direction if another key is pressed
				break;
		}
	}

	/**
	 * Get next position of the collector.
	 */
	private Position getNextCollectorPos() {
		return new Position(
				this.snake.getFirst().getX() + this.direction.getXDelta(),
				this.snake.getFirst().getY() + this.direction.getYDelta());
	}

	/**
	 * This method is called repeatedly so that the
	 * game can update its state.
	 * 
	 * @param lastKey
	 *            The most recent keystroke.
	 */
	@Override
	public void gameUpdate(final int lastKey) throws GameOverException {
		updateDirection(lastKey);
		// If current direction is the opposite of the last direction, don't change direction
		if (this.last_direction.opposite(this.direction)==1)
			this.direction = this.last_direction;
		
		// Remove the last position in of the snake
		setGameboardState(this.snake.getLast(), BLANK_TILE);
		this.snake.removeLast();
		
		// The last HEAD_TILE should now be a BODY_TILE
		setGameboardState(this.snake.getFirst(), BODY_TILE);

		// Add New Head Position
		this.snake.addFirst(getNextCollectorPos());

		// Game over if new position illegal
		if (isOutOfBounds(this.snake.getFirst())) {
			throw new GameOverException(this.score);
		}
		
		// Draw head at new position.
		setGameboardState(this.snake.getFirst(), HEAD_TILE);
		
		// Create iteraton to check both if the snake eats itself as well as fat tiles
		Iterator<Position> iterator = this.snake.iterator();
		Position iteratorPosition =iterator.next();
		while(iterator.hasNext()){
			Position iteratorPos =iterator.next();
			if (this.snake.getFirst().equals(iteratorPos))
				throw new GameOverException(this.score);
			if(iteratorPosition.equals(iteratorPos))
				setGameboardState(iteratorPosition, DOUBLE_TILE);
		    iteratorPosition=iteratorPos;
		}

		// Remove food if snake eats it, place new food and make the snake grow
		if (this.snake.contains(this.foodPos)) {
			this.score++;
			this.foodPos=newFood();
			this.snake.addFirst(this.snake.getFirst());
		}
		
		// update direction to see which move is illegal next update
		last_direction = direction;

	}

	/**
	 * 
	 * @param pos The position to test.
	 * @return <code>false</code> if the position is outside the playing field, <code>true</code> otherwise.
	 */
	private boolean isOutOfBounds(Position pos) {
		return pos.getX() < 0 || pos.getX() >= getGameboardSize().width
				|| pos.getY() < 0 || pos.getY() >= getGameboardSize().height;
	}

}
