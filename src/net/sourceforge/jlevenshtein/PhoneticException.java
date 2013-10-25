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
 * An exception that is thrown if a problem with a phonetic pattern occurs.
 * @author Rafael W.
 * @version 0.1
 * @see Levenshtein
 */
public class PhoneticException extends RuntimeException {

	/**
	 * basic implementation since v0.1
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The error message displayed to the user.
	 */
	protected final String errorMessage;
	
	/**
	 * Basic constructor with one argument.
	 * @param errorMessage An individual error message to return to user
	 */
	PhoneticException(String errorMessage) {
		
		super(errorMessage);
		this.errorMessage = errorMessage;
		
	}
	
	/**
	 * Basic constructor without argument. (Default message)
	 */
	PhoneticException() {
		super();
		this.errorMessage = "There was a problem with a phonetic symbol.";
	}
	
	/**
	 * Returns the cause of the error.
	 * @return The error message.
	 */
	@Override
	public String getMessage() {
		return errorMessage;
	}
	
}