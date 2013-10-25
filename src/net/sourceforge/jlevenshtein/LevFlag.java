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

/**
 * Enumeration representing possible flags for the {@link Levenshtein} computation.
 * @author Rafael W.
 * @version 0.1
 * @see Levenshtein
 */
public enum LevFlag {
	
	/**
	 * Treat the input as one word
	 */
	TREAT_AS_ONE_WORD,				// (0)

	/**
	 * Treat the input as a sentence, possibly containing several words
	 */
	TREAT_AS_SENTANCE,				// (0)		
	
	/**
	 * Treat the input as phonetic (i.e. XSAMPA) symbols
	 */
	PHONETIC,						// (1)
	
	/**
	 * Treat the input as plain text
	 */
	PLAIN,							// (1)
	
	/**
	 * Ignore the difference of upper and lower case letters
	 */
	IGNORE_CASE,					// (2)
	
	/**
	 * Do not ignore the difference of upper and lower case letters
	 */
	USE_CASE,						// (2)
	
	/**
	 * Take special characters into account
	 */
	KEEP_SPECIAL,					// (3)
	
	/**
	 * Ignore special characters
	 */
	STRIP_SPECIAL,					// (3) Ignore special characters
	
	/**
	 * Allow the swapping of characters for transformation
	 */
	ALLOW_SWAP,					 	// (4)
	
	/**
	 * Disallow the swapping of characters for transformation
	 */
	DISALLOW_SWAP,					// (4)

	/**
	 * Compute the absolute Levenshtein distance
	 */
	ABSOLUTE,						// (5)
	
	/**
	 * Compute the relative Levenshtein distance
	 */
	RELATIVE;						// (5) Return index value

}
