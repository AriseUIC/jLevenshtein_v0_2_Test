/*
 * jLevenshtein: An extended Damerau-Levenshtein distance implementation
 * 
 * Copyright (C) 2010 Rafael W.
 * 
 * This file is part of jLevenshtein on sourceforge.net
 *
 * jLevenshtein is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * jLevenshtein is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with jLevenshtein. If not, see http://www.gnu.org/licenses/.
 *
 */
package net.sourceforge.jlevenshtein;

import java.io.Serializable;

/**
 * This class speeds up the comparison process of the {@link Levenshtein} class since it requires
 * two value keys for several purposes. Also it is used as return key for the path function
 * that explains the transformation process of two words.
 * @author Rafael W.
 * @version 0.1b
 * @see Levenshtein
 * @see ComparedStrings
 */
public final class HashPoint implements Comparable<HashPoint>, Cloneable, Serializable {
	
	/**
	 * Serial version ID - since v0.1
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * The x coordinate
	 */
	public final int x;
	
	/**
	 * The y coordinate
	 */
	public final int y;
	
	/**
	 * The shift value for the hash value computation
	 */
	private static final int SHIFT = 46341;
	
	/**
	 * Constructor for given values
	 * @param x The x-coordinate
	 * @param y The y-coordinate
	 */
	public HashPoint(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	/**
	 * Constructs a point set to <code>0</code>, <code>0</code>
	 */
	public HashPoint() {
		this.x = 0;
		this.y = 0;
	}
	
	/**
	 * A cloning constructor
	 * @param hashPoint The HashPoint that should be cloned
	 */
	public HashPoint(HashPoint hashPoint) {
		this.x = hashPoint.x;
		this.y = hashPoint.y;
	}
	
	/**
	 * Two points are declared equal if both the <code>x</code> and <code>y</code> coordinates are equal
	 */
	@Override
	public boolean equals(Object identifier) {
		if(identifier != null && identifier.getClass().equals(this.getClass())) {
			return (this.x == ((HashPoint)identifier).x && this.y == ((HashPoint)identifier).y);
		} else {
			return false;
		}
	}
	
	/**
	 * Computes the hash value by their coordinates
	 */
	@Override
	public int hashCode() {
		return x * SHIFT + y;
	}
	
	/**
	 * Clones a hash point
	 * @return The cloned object
	 */
	@Override
	public HashPoint clone() {
		try {
			return (HashPoint)super.clone();
		} catch(CloneNotSupportedException e) {
			throw new InternalError("Failed to clone HashPoint object");
		}
	}
	
	/**
	 * Applies lexicographic ordering
	 */
	@Override
	public int compareTo(HashPoint comparedObject) {
			
		if(this.x > comparedObject.x) {
			return 1;
		} else if(this.x ==comparedObject.x && this.y > comparedObject.y) {
		return 1;
		} else if(this.x == comparedObject.x && this.y == comparedObject.y) {
			return 0;
		} else {
			return -1;
		}
			
	}
	
	/**
	 * Prints the point to a {@link String}
	 * @return A text describing the coordinates
	 */
	@Override
	public String toString() {
		return "HashPoint[x=" + String.valueOf(x) + ",y=" + String.valueOf(y) + "]";
	}
	
}