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
import net.sourceforge.jlevenshtein.ComparedStrings;
import net.sourceforge.jlevenshtein.LevFlag;
import net.sourceforge.jlevenshtein.Levenshtein;

/**
 * This is an example class to demonstrate the <code>jlevenshtein</code> package.
 * @author Rafael W.
 * @version 0.1
 * @see net.sourceforge.jlevenshtein.Levenshtein
 * @see net.sourceforge.jlevenshtein.LevFlag
 */
class Example {
	
	/**
	 * This class is not meant to instantiate.
	 */
	private Example() {
		/* empty */
	}

	/**
	 * The main method simply compares some Strings in different fashion.
	 * @param args The arguments are not used.
	 */
	public static void main(String[] args) {
		
		Levenshtein ls = new Levenshtein();
		
		// First example
		String firstString = "A bird in the hand is worth two in the bush"; //"soylent green is people";
		String secondString = "A börd in zhä händ is wörth tuh in thä busch"; //"people soiled our green";
		
		//String firstString = "That brute was nasty";
		//String secondString = "That root was nasty";
		
		System.out.printf("'%s' and '%s' have a similarity index of: %.2f%n", 
				firstString, secondString, ls.compare(firstString, secondString));
		
		// First example: Absolute distance
		ls.setFlag(LevFlag.ABSOLUTE);
		
		System.out.printf("'%s' and '%s' have a (weighted) edit distance index of: %.2f%n", 
				firstString, secondString, ls.compare(firstString, secondString));
		
		ls.setFlag(LevFlag.RELATIVE);
		
		// First example: Mixed words
		secondString = "bird a in hand the worth is in the bush two";
		
		System.out.printf("'%s' and '%s' have a similarity index of: %.2f%n", 
				firstString, secondString, ls.compare(firstString, secondString));
		
		// With one-word flag
		ls.setFlag(LevFlag.TREAT_AS_ONE_WORD);
		
		System.out.printf("'%s' and '%s' have a similarity index of: %.2f (compared as single word)%n", 
				firstString, secondString, ls.compare(firstString, secondString));
		
		ls.setFlag(LevFlag.TREAT_AS_SENTANCE);
		
		// With one-word flag
		secondString = "BIrD A iN HaNd tHE wORTh iS iN tHE buSH tWo";
		
		System.out.printf("'%s' and '%s' have a similarity index of: %.2f (with case)%n", 
				firstString, secondString, ls.compare(firstString, secondString));
		
		ls.setFlag(LevFlag.IGNORE_CASE);
		
		System.out.printf("'%s' and '%s' have a similarity index of: %.2f (case ignored)%n", 
				firstString, secondString, ls.compare(firstString, secondString));
		
		ls.setFlag(LevFlag.USE_CASE);
		
		// First example: Swapping of characters
		secondString = "A iddr ni eth hnad si owtrh tow ni teh ubsh";
		
		System.out.printf("'%s' and '%s' have a similarity index of: %.2f%n", 
				firstString, secondString, ls.compare(firstString, secondString));
		
		ls.setFlag(LevFlag.DISALLOW_SWAP);
		
		System.out.printf("'%s' and '%s' have a similarity index of: %.2f%n", 
				firstString, secondString, ls.compare(firstString, secondString));
		
		ls.setFlag(LevFlag.ALLOW_SWAP);
		
		// First example: Different match of same length
		secondString = "It's not over until the fat lady sings";
		
		System.out.printf("'%s' and '%s' have a similarity index of: %.2f%n", 
				firstString, secondString, ls.compare(firstString, secondString));

		// First example: Few words
		secondString = "Duh!";
		
		System.out.printf("'%s' and '%s' have a similarity index of: %.2f%n", 
				firstString, secondString, ls.compare(firstString, secondString));
		
		// First example: Special characters
		secondString = "A bird! in %the% han&&d (is worth) two in �the bu$h";
		
		ls.setFlag(LevFlag.KEEP_SPECIAL);
		
		System.out.printf("'%s' and '%s' have a similarity index of: %.2f (special characters)%n", 
				firstString, secondString, ls.compare(firstString, secondString));
		
		ls.setFlag(LevFlag.STRIP_SPECIAL);
		
		System.out.printf("'%s' and '%s' have a similarity index of: %.2f (no special characters)%n", 
				firstString, secondString, ls.compare(firstString, secondString));
		
		// Second example: XSAMPA
		ls.setFlag(LevFlag.PHONETIC);
		
		firstString = "gudmO:nIN mAn";
		secondString = "g@mOn@n ma:\\n";
		
		System.out.printf("'%s' and '%s' have a similarity index of: %.2f (XSAMPA)%n", 
				firstString, secondString, ls.compare(firstString, secondString));
		
		ls.setFlag(LevFlag.PLAIN);
		
		// Third example: Using the more detailed version of the function
		firstString = "The quill is mightier than the sword";
		secondString = "The nerd's keyboard is mightier than the quill";
		ComparedStrings cs = ls.compareDetailed(firstString, secondString);

		System.out.printf("%nWe created an object that holds information for the compared String objects:%n'%s' and '%s'%n", 
				cs.getFirst(), cs.getSecond());
		
		System.out.printf("The overall index is: %.2f%n", cs.index());
		
		System.out.printf("The weighted Levenshtein distance is: %.2f%n", cs.distance());

		System.out.printf("The 3rd identified (and possibly transformed) word of string two is: '%s'%n", cs.getSecond(2));
		System.out.printf("This word was matched to the %dth word of string one: '%s' (overall accuracy: %.2f)%n", 
				cs.matchSecond(2) + 1, cs.getFirst(cs.matchSecond(2)), cs.index(cs.matchSecond(2), 2));
		
		System.out.printf("The 2nd identified (and possibly transformed) word of string one is: '%s'%n", cs.getFirst(1));
		System.out.printf("This word was matched to the %dth word of string two: '%s' (overall accuracy: %.2f)%n", 
				cs.matchFirst(1) + 1, cs.getSecond(cs.matchFirst(1)), cs.index(1, cs.matchFirst(1)));
		
	} 

}