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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * A command line class to run the basic algorithm from a console.
 * @author Rafael W.
 * @version 0.1
 * @see Levenshtein
 */
class Console {
	
	/**
	 * Empty, private constructor
	 */
	private Console() {
		/* empty */
	}
	
	/**
	 * Function for basic comparison to run on a console.
	 * @param args The String arguments and flags for the program 
	 * 		(Flags: <code>-xsampa -noswap -nocase -special -absolute -oneword</code>)
	 */
	public static void main(String[] args) {
		
		// Set basic variables
		String[] help = {
				"Please use as follows: 'string1 string2 [-flag[s]]' : -xsampa -noswap -nocase -special -absolute -oneword",
				"Explaination of the flags: -xsampa: Phonetic matching | -noswap: Do not allow to swap characters | " +
				"-case: Do not mind upper/lower case | -special: Keep special characters -absolute: Display absolute " +
				"(weighted) Levenshtein distance | -oneword: Treat whitespaces as part of the words | Enclose a \"string with more " +
				"words\" in quotes if necessary."
		};
		
		Map<String,LevFlag> flags = new HashMap<String,LevFlag>(6);
		flags.put("-xsampa", LevFlag.PHONETIC);
		flags.put("-noswap", LevFlag.DISALLOW_SWAP);
		flags.put("-nocase", LevFlag.IGNORE_CASE);
		flags.put("-special", LevFlag.KEEP_SPECIAL);
		flags.put("-absolute", LevFlag.ABSOLUTE);
		flags.put("-oneword", LevFlag.TREAT_AS_ONE_WORD);
		
		// Check if help output is wanted
		if(args.length == 1 && args[0].equals("help")) {
			System.out.println(help[0]);
			System.out.println(help[1]);
			System.exit(0);
		}
		
		// Container for user flags and String for input
		StringBuilder[] userInput = {new StringBuilder(), new StringBuilder()};
		Collection<LevFlag> userFlags = new HashSet<LevFlag>();
		
		int inputPositionCount = 0;
		boolean open = false;
		
		// Go through user input
		for(int i = 0; i < args.length; i++) {
			
			args[i] = args[i].trim();
			
			// Interpret user Strings input (check for '"' symbols)
			if(inputPositionCount == 0 || inputPositionCount == 1) {
				
				if(open == false && args[i].startsWith("\"")) {
					open = true;
					args[i] = args[i].substring(1, args[i].length());
				} else if(open == true && args[i].endsWith("\"")) {
					open = false;
					userInput[inputPositionCount].append(args[i].substring(0, args[i].length() - 1));
					inputPositionCount++;
				} else if(open == true && args[i].startsWith("\"") || open == false && args[i].endsWith("\"")) {
					System.err.println("Invalid input. Enter 'help' for further infomation.");
				} else {
					userInput[inputPositionCount].append(args[i]);
					inputPositionCount++;
				}
			
			// Check for flags
			} else {
				if(flags.containsKey(args[i])) {
					userFlags.add(flags.get(args[i]));
				} else {
					System.err.printf("Invalid flag: '%s' | Enter 'help' for further infomation.%n", args[i]);
					System.exit(-1);
				}
			}
			
		}
		
		// Return error if the output is invalid
		if(inputPositionCount != 2) {
			System.err.println("Invalid number of arguments | Enter 'help' for further infomation.");
			System.exit(-1);
		}
		
		// Compute results and print to console
		Levenshtein levenshtein = new Levenshtein(userFlags.toArray(new LevFlag[0]));
		System.out.println(levenshtein.compare(userInput[0].toString(), userInput[1].toString()));
		
	}

}
