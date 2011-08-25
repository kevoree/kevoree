package org.kevoree.library.javase.motion;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 24/08/11
 * Time: 10:02
 */
public enum Movement {
	UNDEFINED (0),
	MOVE_TO_LEFT (1),
	MOVE_UP (2),
	MOVE_TO_RIGHT(3),
	MOVE_DOWN (4);

	private int movement;

	Movement (int movement) {
		this.movement = movement;
	}

	public int getMovement () {
		return movement;
	}
}
