package Galaga;

import Jama.Matrix;
import processing.core.*;

/**
 * Defines an enemy
 * 
 * @author Christopher Glasz
 */
public abstract class Enemy implements ApplicationConstants {

	/**
	 * Boolean to keep track of whether the enemy is destroyed
	 */
	protected boolean destroyed;

	/**
	 * Boolean to keep track of whether the enemy has been hit
	 */
	protected boolean hit;

	protected boolean scored;

	/**
	 * Coordinates in the world
	 */
	protected float x, y;

	/**
	 * Angle of the enemy
	 */
	protected float theta;

	/**
	 * The destination coordinates
	 */
	protected float goalX, goalY;

	/**
	 * Home coordinates
	 */
	protected float homeX, homeY;

	/**
	 * Spawn coordinates
	 */
	protected float spawnX, spawnY;

	/**
	 * Path to take upon entry
	 */
	protected FlightPath entryPath;

	/**
	 * Boolean to keep track of whether the goal has been reached
	 */
	boolean goalReached;

	/**
	 * Waypoints for the enemy to follow. each entry is {x, y, time}
	 */
	float[][] waypoints;

	/**
	 * Coefficients for the cubic interpolation
	 */
	private float[][] ax, ay;

	/**
	 * Time since the beginning of the path
	 */
	protected float ut;

	/**
	 * X and Y components of velocity
	 */
	protected float vx, vy;

	/**
	 * Radius of disk collider
	 */
	protected float r;

	/**
	 * Sprites to be rendered at each frame
	 */
	protected PImage sprite1, sprite2;

	/**
	 * How much an enemy is worth when destroyed
	 */
	protected int formationScore, attackingScore;

	/**
	 * Explosion sprites to draw
	 */
	protected PImage[] eSprites;

	/**
	 * The current state in the animation cycle
	 */
	protected AnimationState animationState;

	/**
	 * The time since the animation state last changed
	 */
	protected float animationTimer;

	/**
	 * The state of the enemy's movement
	 */
	protected EnemyState state;

	/**
	 * Constructor initializes variables
	 * 
	 * @param x
	 *            x coordinate
	 * @param y
	 *            y coordinate
	 * @param entryPath
	 *            path to take on entry
	 */
	public Enemy(float x, float y, FlightPath entryPath) {
		this.x = x;
		this.y = y;
		this.goalX = x;
		this.goalY = y;
		this.homeX = x;
		this.homeY = y;
		this.spawnX = x;
		this.spawnY = y;
		this.theta = 0;
		this.vx = 0;
		this.vy = 0;
		this.r = 7 * PIXEL_WIDTH;

		state = EnemyState.ASSUME_POSITION;

		this.entryPath = entryPath;
		createPath();
		followCubicPath();

		destroyed = false;
		hit = false;
		scored = false;

		goalReached = false;
		animationTimer = (float) Math.random() * ANIMATION_FRAME;
		animationState = AnimationState.random();
		createSprite();
	}

	/**
	 * Constructor initializes variables
	 * 
	 * @param x
	 *            x coordinate
	 * @param y
	 *            y coordinate
	 * @param goalX
	 *            starting x destination
	 * @param goalY
	 *            starting y destination
	 * @param entryPath
	 *            path to take on entry
	 */
	public Enemy(float x, float y, float goalX, float goalY,
			FlightPath entryPath) {
		this.x = x;
		this.y = y;
		this.goalX = goalX;
		this.goalY = goalY;
		this.homeX = goalX;
		this.homeY = goalY;
		this.spawnX = x;
		this.spawnY = y;
		this.theta = 0;
		this.vx = 0;
		this.vy = 0;
		this.r = 7 * PIXEL_WIDTH;

		state = EnemyState.ASSUME_POSITION;

		this.entryPath = entryPath;
		createPath();
		followCubicPath();

		destroyed = false;
		hit = false;
		scored = false;

		goalReached = false;
		animationTimer = (float) Math.random() * ANIMATION_FRAME;
		animationState = AnimationState.random();
		createSprite();
	}

	/**
	 * Constructor initializes variables
	 * 
	 * @param x
	 *            x coordinate
	 * @param y
	 *            y coordinate
	 * @param goalX
	 *            starting x destination
	 * @param goalY
	 *            starting y destination
	 * @param homeX
	 *            x coordinate of home position
	 * @param homeY
	 *            y coordinate of home position
	 * @param entryPath
	 *            path to take on entry
	 */
	public Enemy(float x, float y, float goalX, float goalY, float homeX,
			float homeY, FlightPath entryPath) {
		this.x = x;
		this.y = y;
		this.goalX = goalX;
		this.goalY = goalY;
		this.homeX = homeX;
		this.homeY = homeY;
		this.spawnX = x;
		this.spawnY = y;
		this.theta = 0;
		this.vx = 0;
		this.vy = 0;
		this.r = 7 * PIXEL_WIDTH;

		state = EnemyState.ASSUME_POSITION;

		this.entryPath = entryPath;
		createPath();
		followCubicPath();

		destroyed = false;
		hit = false;
		scored = false;

		goalReached = false;
		animationTimer = (float) Math.random() * ANIMATION_FRAME;
		animationState = AnimationState.random();
		createSprite();
	}

	/**
	 * Updates the enemy status based on time since last update
	 * 
	 * @param elapsed
	 *            time since last update
	 */
	public void update(float elapsed) {

		if (!hit) {
			animationTimer += elapsed * 0.001f;
			if (animationTimer > ANIMATION_FRAME) {
				animationTimer = 0;
				animationState = animationState.getNext();
			}
		} else {
			if (animationState == AnimationState.EXP_5)
				destroy();

			animationTimer += elapsed * 0.001f;
			if (animationTimer > EXPLOSION_FRAME) {
				animationTimer = 0;
				animationState = animationState.getNext();
			}
		}

		ut += elapsed * 0.001;

		if (!hit) {
			switch (state) {
			case ASSUME_POSITION:
				if (goalReached) {
					state = EnemyState.FORMATION_OUT;
					Galaga.syncFormation(this);
				}
				followPath();
				break;
			case DIVE:
				if (goalReached) {
					state = EnemyState.FORMATION_OUT;
					Galaga.syncFormation(this);
				}
				followPath();
				break;
			case FORMATION_OUT:
				if (goalReached) {
					state = EnemyState.FORMATION_IN;
					createPath();
				}
				followPath();
				break;
			case FORMATION_IN:
				if (goalReached) {
					state = EnemyState.FORMATION_OUT;
					createPath();
				}
				followPath();
				break;
			default:
				break;

			}
		}

	}

	/**
	 * Render the enemy to the PApplet
	 * 
	 * @param g
	 *            the PApplet to draw to
	 */
	public void render(PApplet g) {
		g.pushMatrix();
		g.translate(x, y);
		g.rotate(theta);
		g.scale(PIXEL_WIDTH, -PIXEL_WIDTH);
		g.noSmooth();
		g.imageMode(PConstants.CENTER);

		switch (animationState) {
		case UP:
			g.image(sprite1, 0, 0);
			break;
		case DOWN:
			g.image(sprite2, 0, 0);
			break;
		case EXP_1:
			g.image(eSprites[0], 0, 0);
			break;
		case EXP_2:
			g.image(eSprites[1], 0, 0);
			break;
		case EXP_3:
			g.image(eSprites[2], 0, 0);
			break;
		case EXP_4:
			g.image(eSprites[3], 0, 0);
			break;
		case EXP_5:
			g.image(eSprites[4], 0, 0);
			break;
		default:
			break;
		}

		g.popMatrix();

	}

	/**
	 * Detects if the passed in missile is colliding with the enemy. If it is,
	 * the enemy is hit, the missile is destroyed, and the method returns true.
	 * Otherwise, it returns false
	 * 
	 * @param missile
	 *            the missile to check collision with
	 * @return true if the missile and enemy are colliding
	 */
	public boolean detectCollision(Missile missile) {
		boolean h = false;
		float bx = missile.getX();
		float by = missile.getY();

		float dist2 = (bx - x) * (bx - x) + (by - y) * (by - y);
		if (dist2 < r * r) {
			h = true;
			hit();
			missile.destroy();
		}
		return h;
	}

	/**
	 * Accessor method for hit
	 * 
	 * @return hit
	 */
	public boolean isHit() {
		return hit;
	}

	/**
	 * Hits (but does not destroy) the enemy
	 */
	public void hit() {
		animationState = AnimationState.EXP_1;
		hit = true;
	}

	/**
	 * Accessor method for destroyed
	 * 
	 * @return destroyed
	 */
	public boolean isDestroyed() {
		return destroyed;
	}

	/**
	 * Destroys the enemy
	 */
	public void destroy() {
		destroyed = true;
	}

	/**
	 * Reset to starting state
	 */
	public void reset() {
		destroyed = false;
		hit = false;
		scored = false;

		this.x = spawnX;
		this.y = spawnY;
		this.goalX = homeX;
		this.goalY = homeY;
		this.theta = 0;
		this.vx = 0;
		this.vy = 0;

		goalReached = false;
		state = EnemyState.ASSUME_POSITION;
		createPath();

		animationTimer = (float) Math.random() * ANIMATION_FRAME;
		animationState = AnimationState.random();
	}

	/**
	 * Start the attack cycle
	 */
	public void dive() {
		state = EnemyState.DIVE;
		createPath();
	}

	/**
	 * Accessor method for enemy state
	 * 
	 * @return enemy state
	 */
	public EnemyState getState() {
		return state;
	}

	/**
	 * Return a missile shot from the fighter
	 * 
	 * @return missile shot from the fighter
	 */
	public Missile shoot() {
		float phi = theta;
		if (state.inFormation())
			phi -= PConstants.PI / 2;
		else
			phi += PConstants.PI / 2;

		return new EnemyMissile(x, y, phi);
	}

	/**
	 * Returns a clone of the enemy
	 * 
	 * @return a clone of the enemy
	 */
	public abstract Enemy clone();

	/**
	 * Loads the sprites to be rendered at each frame
	 */
	protected void createSprite() {
		eSprites = new PImage[5];
		eSprites[0] = (new PApplet())
				.loadImage("Sprites/enemy_explosion_1.png");
		eSprites[1] = (new PApplet())
				.loadImage("Sprites/enemy_explosion_2.png");
		eSprites[2] = (new PApplet())
				.loadImage("Sprites/enemy_explosion_3.png");
		eSprites[3] = (new PApplet())
				.loadImage("Sprites/enemy_explosion_4.png");
		eSprites[4] = (new PApplet())
				.loadImage("Sprites/enemy_explosion_5.png");
	}

	/**
	 * Returns the number of points that the enemy is worth
	 * 
	 * @return the number of points that the enemy is worth
	 */
	public int getScore() {
		int score = 0;
		if (!scored) {
			switch (state) {
			case ASSUME_POSITION:
			case FORMATION_IN:
			case FORMATION_OUT:
				score = formationScore;
				break;
			case DIVE:
				score = attackingScore;
				break;
			default:
				break;
			}
			scored = true;
		}

		return score;
	}

	/**
	 * Sync the enemy to the rest in formation
	 * 
	 * @param prototype
	 *            enemy to sync with
	 */
	public void syncFormation(Enemy prototype) {
		this.state = prototype.state;
		float timeToGoal = Math.min(FORMATION_CYCLE_TIME - prototype.ut,
				FORMATION_CYCLE_TIME);
		createFormationPath(timeToGoal);
	}

	/**
	 * Initializes the waypoints for the path
	 */
	public void createPath() {
		ut = 0;

		switch (state) {
		case ASSUME_POSITION:
			createAssumePositionPath();
			break;

		case DIVE:
			createDivePath();
			break;

		case FORMATION_IN:
			goalX = homeX * 0.8f;
			goalY = (homeY - BOSS_Y) * 0.8f + BOSS_Y;
			float[][] newpoints3 = { { x, y, 0 },
					{ goalX, goalY, FORMATION_CYCLE_TIME } };
			waypoints = newpoints3;
			break;
		case FORMATION_OUT:
			goalX = homeX * 1.25f;
			goalY = (homeY - BOSS_Y) * 1.25f + BOSS_Y;
			float[][] newpoints4 = { { x, y, 0 },
					{ goalX, goalY, FORMATION_CYCLE_TIME } };
			waypoints = newpoints4;
			break;

		default:
			break;
		}

		calculateA();
		goalReached = false;
	}

	/**
	 * Get the waypoints for the entry path
	 */
	protected void createAssumePositionPath() {
		waypoints = entryPath.getPoints(x, y, goalX, goalY);
	}

	/**
	 * Get the waypoints for the dive path
	 */
	protected void createDivePath() {
		goalX = Fighter.instance().getX();
		goalY = Fighter.instance().getY();
		waypoints = FlightPath.DIVE.getPoints(x, y, goalX, goalY);
	}

	/**
	 * Get waypoints for the entry path, given the time it should take to reach
	 * the goal
	 * 
	 * @param timeToGoal
	 *            time it should take to reach the goal
	 */
	private void createFormationPath(float timeToGoal) {
		ut = 0;

		if (state == EnemyState.FORMATION_IN) {
			goalX = homeX * 0.8f;
			goalY = (homeY - BOSS_Y) * 0.8f + BOSS_Y;
			float[][] newpoints3 = { { x, y, 0 }, { goalX, goalY, timeToGoal } };
			waypoints = newpoints3;
		} else if (state == EnemyState.FORMATION_OUT) {
			goalX = homeX * 1.25f;
			goalY = (homeY - BOSS_Y) * 1.25f + BOSS_Y;
			float[][] newpoints4 = { { x, y, 0 }, { goalX, goalY, timeToGoal } };
			waypoints = newpoints4;
		}

		calculateA();
		goalReached = false;
	}

	/**
	 * Follow the waypoints either linearly or through linear interpolation
	 * depending on the game state
	 */
	private void followPath() {

		switch (state) {
		case ASSUME_POSITION:
			followCubicPath();
			break;
		case DIVE:
			followCubicPath();
			break;
		case FORMATION_IN:
		case FORMATION_OUT:
			followLinearPath();
			this.theta = 0;
			break;
		default:
			break;

		}
	}

	/**
	 * Follow the waypoints through linear interpolation
	 */
	private void followLinearPath() {
		final int NB_WAY_PTS = waypoints.length;

		if (waypoints[waypoints.length - 1][2] <= ut) {
			goalReached = true;
		}

		// which interval do we fall into?
		for (int i = 1; i < NB_WAY_PTS; i++) {
			if (waypoints[i][2] >= ut) {
				// we fall in the interval [i-1, i]

				// Time along the interval
				float tau = (ut - waypoints[i - 1][2])
						/ (waypoints[i][2] - waypoints[i - 1][2]);

				// Set x and y
				float newx = waypoints[i - 1][0] + tau
						* (waypoints[i][0] - waypoints[i - 1][0]);
				float newy = waypoints[i - 1][1] + tau
						* (waypoints[i][1] - waypoints[i - 1][1]);

				float dx = x - newx;
				float dy = y - newy;

				// Set angle
				this.theta = PApplet.atan2(dy, dx) + PConstants.PI / 2;

				this.x = newx;
				this.y = newy;

				break;
			}
		}
	}

	/**
	 * Follow the waypoints through cubic interpolation
	 */
	private void followCubicPath() {
		final int NB_WAY_PTS = waypoints.length;

		if (waypoints[waypoints.length - 1][2] <= ut) {
			goalReached = true;
		}

		// which interval do we fall into?
		for (int i = 1; i < NB_WAY_PTS; i++) {
			if (waypoints[i][2] >= ut) {
				// we fall in the interval [i-1, i]

				// Coefficients
				// x: ax_[i-1][3] ax_[i-1][2] ax_[i-1][1] ax_[i-1][0]
				// y: ay_[i-1][3] ay_[i-1][2] ay_[i-1][1] ay_[i-1][0]

				// Time along that interval
				float tau = (ut - waypoints[i - 1][2])
						/ (waypoints[i][2] - waypoints[i - 1][2]);

				// Tau squared
				float tau2 = tau * tau;

				// Tau cubed
				float tau3 = tau2 * tau;

				// Set x and y
				float newx = ax[i - 1][3] * tau3 + ax[i - 1][2] * tau2
						+ ax[i - 1][1] * tau + ax[i - 1][0];

				float newy = ay[i - 1][3] * tau3 + ay[i - 1][2] * tau2
						+ ay[i - 1][1] * tau + ay[i - 1][0];

				float dx = x - newx;
				float dy = y - newy;

				// Set angle
				this.theta = PApplet.atan2(dy, dx) + PConstants.PI / 2;

				this.x = newx;
				this.y = newy;

				break;
			}
		}
	}

	/**
	 * Calculate the coefficients for the cubic interpolation
	 */
	public void calculateA() {

		final int NB_SEGMENTS = waypoints.length - 1;
		final int NB_WAY_PTS = waypoints.length;

		// ----------------------------------------------------
		// 1. Initialize the TNT matrices of the SLE
		// ----------------------------------------------------
		double[][] mat = new double[4 * NB_SEGMENTS][4 * NB_SEGMENTS];
		double[][] b = new double[4 * NB_SEGMENTS][3];

		// ------------------------------------------------------------
		// initialize the first two and last two rows of the matrix.
		// These correspond to the conditions at the path's endpoints.
		// ------------------------------------------------------------

		// 1 0 0 0 ... 0
		mat[0][0] = 1;

		// 0 1 0 0 0 ... 0
		mat[1][1] = 1;

		// 0 ... 0 0 0 1 1 1 1
		mat[4 * NB_SEGMENTS - 2][4 * NB_SEGMENTS - 4] = 1;
		mat[4 * NB_SEGMENTS - 2][4 * NB_SEGMENTS - 3] = 1;
		mat[4 * NB_SEGMENTS - 2][4 * NB_SEGMENTS - 2] = 1;
		mat[4 * NB_SEGMENTS - 2][4 * NB_SEGMENTS - 1] = 1;

		// 0 ... 0 0 0 1 2 3
		mat[4 * NB_SEGMENTS - 1][4 * NB_SEGMENTS - 3] = 1;
		mat[4 * NB_SEGMENTS - 1][4 * NB_SEGMENTS - 2] = 2.0;
		mat[4 * NB_SEGMENTS - 1][4 * NB_SEGMENTS - 1] = 3.0;

		// Initial location
		b[0][0] = waypoints[0][0]; // x0
		b[0][1] = waypoints[0][1]; // y0

		// Initial speed
		b[1][0] = 0; // vx0
		b[1][1] = 0; // vy0

		// End location
		b[4 * NB_SEGMENTS - 2][0] = waypoints[NB_WAY_PTS - 1][0];
		b[4 * NB_SEGMENTS - 2][1] = waypoints[NB_WAY_PTS - 1][1];

		// End speed
		b[4 * NB_SEGMENTS - 1][0] = 0;
		b[4 * NB_SEGMENTS - 1][1] = 0;

		// --------------------------------------------------------------
		// Now fill in the values for the connections at interior points
		// --------------------------------------------------------------

		// 0 ... 0 1 1 1 1 0 0 0 0 0 ... 0 < 4(i - 1) + 2
		// 0 ... 0 0 0 0 0 1 0 0 0 0 ... 0
		// 0 ... 0 0 1 2 3 0 -1 0 0 0 ... 0
		// 0 ... 0 0 0 2 6 0 0-2 0 0 ... 0
		// ^ 4(i - 1)
		for (int i = 1; i < NB_WAY_PTS - 1; i++) {
			int k = 4 * (i - 1) + 2;
			int l = 4 * (i - 1);

			// 1 1 1 1 0 0 0 0
			mat[k][l] = 1;
			mat[k][l + 1] = 1;
			mat[k][l + 2] = 1;
			mat[k][l + 3] = 1;

			// 0 0 0 0 1 0 0 0
			mat[k + 3][l + 4] = 1;

			// 0 1 2 3 0 -1 0 0
			mat[k + 1][l + 1] = 1.0;
			mat[k + 1][l + 2] = 2.0;
			mat[k + 1][l + 3] = 3.0;
			mat[k + 1][l + 5] = -1.0;

			// 0 0 2 6 0 0 -2 0
			mat[k + 2][l + 2] = 2.0;
			mat[k + 2][l + 3] = 6.0;
			mat[k + 2][l + 6] = -2.0;

			// Location
			b[k][0] = waypoints[i][0];
			b[k][1] = waypoints[i][1];

			// Continuity of first derivative for x and y
			b[k + 1][0] = 0;
			b[k + 1][1] = 0;

			// Continuity of first derivative for x and y
			b[k + 2][0] = 0;
			b[k + 2][1] = 0;

			// Location
			b[k + 3][0] = waypoints[i][0];
			b[k + 3][1] = waypoints[i][1];
		}

		// ----------------------------------------------------
		// 2. Solve the SLE
		// ----------------------------------------------------
		Matrix M = new Matrix(mat);
		Matrix B = new Matrix(b);
		Matrix Axyt = M.solve(B);

		// ----------------------------------------------------
		// 3. Copy the results in my instance variables
		// ----------------------------------------------------
		ax = new float[NB_SEGMENTS][4];
		ay = new float[NB_SEGMENTS][4];

		for (int i = 0; i < NB_SEGMENTS; i++) {
			for (int j = 0; j < 4; j++) {
				ax[i][j] = (float) Axyt.get(4 * i + j, 0);
				ay[i][j] = (float) Axyt.get(4 * i + j, 1);
			}
		}

		ut = 0;

	}

	/**
	 * Enumeration to describe the possible movement states
	 * 
	 * @author Christopher Glasz
	 */
	protected enum EnemyState {
		
		/**
		 * Assuming position in formation
		 */
		ASSUME_POSITION, 
		
		/**
		 * In formation and moving outward
		 */
		FORMATION_IN {
			@Override
			public boolean inFormation() {
				return true;
			}
		},
		
		/**
		 * In formation and moving inward
		 */
		FORMATION_OUT {
			@Override
			public boolean inFormation() {
				return true;
			}
		},
		
		/**
		 * Dive bombing
		 */
		DIVE;

		/**
		 * Returns true if the state is an explosion state
		 * 
		 * @return true if the state is an explosion state
		 */
		public boolean inFormation() {
			return false;
		}
	}

	/**
	 * Enumeration to define different flight paths for enemies
	 * 
	 * @author Christopher Glasz
	 */
	public enum FlightPath {

		/**
		 * Flight path 1 has enemies crossing twice and assuming position
		 */
		DOUBLE_CROSS {
			public float[][] getPoints(float x, float y, float goalX,
					float goalY) {
				return new float[][] { { x, y, 0 }, { 0, WORLD_HEIGHT / 2, 1 },
						{ -x, WORLD_HEIGHT / 3, 1.5f },
						{ -x, WORLD_HEIGHT / 2, 2 }, { goalX, goalY, 3 } };
			}
		},

		/**
		 * Flight path 2 has enemies looping up from the bottom
		 */
		BOTTOM_LOOP {
			public float[][] getPoints(float x, float y, float goalX,
					float goalY) {
				return new float[][] { { x, y, 0 },
						{ x / 10, WORLD_HEIGHT / 3, 1 },
						{ x / 6, 2 * WORLD_HEIGHT / 3, 1.5f },
						{ x / 7, WORLD_HEIGHT / 3, 2 }, { goalX, goalY, 3 } };
			}
		},

		/**
		 * Flight path 3 has enemies looping down from the top
		 */
		TOP_LOOP {
			public float[][] getPoints(float x, float y, float goalX,
					float goalY) {
				return new float[][] { { x, y, 0 },
						{ x / 2, WORLD_HEIGHT / 3, 1 },
						{ 0, WORLD_HEIGHT / 3, 1.5f }, { goalX, goalY, 2 } };
			}
		},

		/**
		 * Flight path 4 defines the path enemies take when dive bombing
		 */
		DIVE {
			public float[][] getPoints(float x, float y, float goalX,
					float goalY) {
				return new float[][] { { x, y, 0 },
						{ x + 0.05f, y + 0.05f, 0.5f }, { goalX, goalY, 2 },
						{ goalX, 0, 3 }, { x, y, 4 } };
			}
		};

		/**
		 * Return waypoints specific to the flight path
		 * 
		 * @param x
		 *            initial x coordinate
		 * @param y
		 *            initial y coordinate
		 * @param goalX
		 *            final x coordinate
		 * @param goalY
		 *            final y coordinate
		 * @return the waypoints for the flight path
		 */
		public float[][] getPoints(float x, float y, float goalX, float goalY) {
			return new float[][] { { 0, 1 }, { 1, 0 } };
		}
	}
}
