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

import java.util.Map;

/**
 * This basic word comparison object is needed for internal computations of the {@link Levenshtein} class and is not
 * meant to be used by the end user.
 * @author Rafael W.
 * @version 0.1
 * @see ComparedStrings
 * @see Levenshtein
 */
class BasicCompared {
	
	/**
	 * The distance mappings of one input string's words
	 */
	final Map<Integer,Integer> firstComparedMapping, secondComparedMapping;

	/**
	 * The overall similarity index
	 */
	final float overallIndex;
	
	/**
	 * The overall similarity distance
	 */
	final float overallDistance;
	
	/**
	 * True if the input strings were swapped
	 */
	final boolean stringsSwapped;

	/**
	 * Basic constructor to set all properties.
	 * @param overallIndex The similarity index of the entire input
	 * @param overallDistance The Levenshtein distance of the entire input
	 * @param firstComparedMapping The mapping of the first sequence of words to the second (cannot be swapped)
	 * @param secondComparedMapping The mapping of the second sequence of words to the first (cannot be swapped)
	 * @param stringsSwapped Indicator if the input strings were swapped during the computation
	 */
	BasicCompared(float overallIndex, float overallDistance, Map<Integer,Integer> firstComparedMapping,
			Map<Integer,Integer> secondComparedMapping, boolean stringsSwapped) {
		
		this.overallIndex = overallIndex;
		this.overallDistance = overallDistance;
		this.firstComparedMapping = firstComparedMapping;
		this.secondComparedMapping = secondComparedMapping;
		this.stringsSwapped = stringsSwapped;
		
	}
	
	/**
	 * Constructor that clones an existing object
	 * @param basicCompared The object to clone
	 */
	BasicCompared(BasicCompared basicCompared) {
		
		this.overallIndex = basicCompared.overallIndex;
		this.overallDistance = basicCompared.overallDistance;
		this.firstComparedMapping = basicCompared.firstComparedMapping;
		this.secondComparedMapping = basicCompared.secondComparedMapping;
		this.stringsSwapped = basicCompared.stringsSwapped;
		
	}
	
	/**
	 * Function that returns the overall similarity index
	 * @return The overall index
	 */
	public float index() {
		return overallIndex;
	}
	
	/**
	 * Returns the (weighted) Levenshtein distance of the entire input
	 * @return The (weighted) Levenshtein distance (equals the Levenshtein distance for two single item inputs)
	 */
	public float distance() {
		return overallDistance;
	}
	
}