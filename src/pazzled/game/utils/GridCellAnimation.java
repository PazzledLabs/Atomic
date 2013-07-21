/*******************************************************************************
 * Copyright (c) 2013 venkat@pazzled.com.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     venkat@pazzled.com - Venkat
 ******************************************************************************/
package pazzled.game.utils;

import pazzled.game.play.GameActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;

public class GridCellAnimation {

	public enum Direction {
		MOVE_LEFT(0), MOVE_RIGHT(1), MOVE_UP(2), MOVE_DOWN(3), MOVE_INVALID(4);

		private int number;

		Direction(int number) {
			this.number = number;
		}

		public int getValue() {
			return number;
		}
	};

	private static int cellWidth;
	private static int cellHeight;
	static TranslateAnimation[] anims;

	public static void init(int width, int height) {
		cellWidth = width;
		cellHeight = height;
		anims = new TranslateAnimation[4];
		for (int i = 0; i < 4; i++) {
			anims[i] = null;
		}
	}

	private static TranslateAnimation getAnimator(Direction direction, int cells) {
		TranslateAnimation anim = anims[direction.getValue()];
		if (anim != null)
			return anim;

		switch (direction) {
		case MOVE_LEFT: {
			anim = new TranslateAnimation(Animation.RELATIVE_TO_PARENT,
					-cellWidth * cells, 0, 0);
			break;
		}
		case MOVE_RIGHT: {
			anim = new TranslateAnimation(Animation.RELATIVE_TO_SELF, cellWidth
					* cells, 0, 0);
			break;
		}
		case MOVE_UP: {
			anim = new TranslateAnimation(0, 0, Animation.RELATIVE_TO_SELF,
					-cellHeight * cells);
			break;
		}
		case MOVE_DOWN: {
			anim = new TranslateAnimation(0, 0, Animation.RELATIVE_TO_SELF,
					cellHeight * cells);
		}
		default:
			break;
		}
		anims[direction.getValue()] = anim;
		return anim;
	}

	public static int move(GameActivity activity, View view,
			Direction direction, int cells) {
		return move(activity, view, direction, cells, 1000);
	}

	public static int move(GameActivity activity, View view,
			Direction direction, int cells, int seconds) {

		TranslateAnimation move = getAnimator(direction, cells);
		move.reset();
		move.setDuration(seconds);
		move.setInterpolator(activity, android.R.anim.linear_interpolator);
		move.setAnimationListener(activity);
		move.setFillEnabled(false);
		view.invalidate();
		// view.setVisibility(View.INVISIBLE);

		// view.clearAnimation();
		view.setAnimation(move);
		// move.setStartTime(5000);
		// move.startNow();
		// view.invalidate();
		// move.start();
		// view.destroyDrawingCache();
		// view.forceLayout();
		// view.notifyAll();
		return 0;
	}

}
