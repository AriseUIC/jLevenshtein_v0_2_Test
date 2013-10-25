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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>This Java program provides static methods to compare strings containing one or more words.
 * The comparison is based on the Damerau-Levenshtein distance. However, I included additional features based
 * on my own ideas that allow to compare sequences of words with respect to their position in the sequence. 
 * Also, instead of the absolute Levenshtein distance this program (by default) computes an index that allows to
 * rank comparisons since the absolute distance naturally increases with the number of letters.</p>
 * 
 * <p>This class was designed to compare shorter texts or sentences. It is not too useful on longer texts since it does
 * e.g. not count words but ranks them relative to their position in a sentence. User flags can be set via the enumeration
 * class {@link LevFlag}</p>
 * 
 * <p>This index is scaled from of <code>0f</code> (iff the compared strings do not share a single character) and <code>1f</code>
 * (iff both strings are identical) - The index is computed with single accuracy to make the possible storage in databases less space 
 * expensive. This index is more reliable with plain (non-phonetic) input since it simply divides the distance by its worst case 
 * scenario.</p>
 * 
 * <p>If white paces should not be taken into consideration, this feature can be (de)activated by the flags: <code>TREAT_AS_ONE_WORD</code>
 * and <code>TREAT_AS_SENTANCE</code>. If a user is interested in the absolute distance of two string sequences the flags 
 * <code>ABSOLUTE_DISTANCE</code> or <code>RELATIVE_DISTANCE</code> can be used.</p>
 * 
 * <p>To get a summary of the entire calculation process, the program can return a Java object {@link BasicCompared} that sums up 
 * all the information gained about distance and transformation process.</p>
 * 
 * <p>This program also allows to compare phonetic differences of custom symbols. The underlying (example) feature descriptions
 * was taken from: Wilbert Heeringa, Measuring Dialect Pronunciation Distances using Levenshtein distance, 
 * University of Groningen (2004), chapter 3, sections 3.1.4, 3.1.4.1 and 3.1.4.2, pp. 40-45.
 * (<a href="http://www.let.rug.nl/~kleiweg/lev/features.txt">http://www.let.rug.nl/~kleiweg/lev/features.txt</a>). 
 * (To (de)activate this mode use the flags <code>PHONETIC</code> or <code>PLAIN</code>) If another feature map should be used, 
 * this class allows to read in another feature map from any text file where the information is arranged in the same way as in the 
 * file provided on this URL. This feature is deactivated by default.</p>
 * 
 * <p>This class controls for special characters and removes them before the comparison if not requested differently by the
 * user of this program (Flags: <code>SPECIAL_CHARACTERS</code>, <code>NO_SPECIAL_CHARACTERS</code>). This feature is activated 
 * by default and must be activated if using the provided mapping of XSAMPA symbols where the phonetics of special characters are 
 * not defined.</p>
 * 
 * <p>If letters only / additionally differ in their case this will be taken into account by this program if not requested
 * differently by the user. The related flags are <code>IGNORE_CASE</code> and <code>USE_CASE</code>.</p>
 * 
 * <p>If characters can be swapped a smaller than the default distance can be computed. To activate or deactivate this feature, use
 * the flags <code>ALLOW_SWAP</code> or <code>DISALLOW_SWAP</code>. This feature is activated by default for plain input and 
 * deactivated by default for XSAMPA setting. If both flags are set on one feature, the default will be used. <code>ALLOW_SWAP</code> 
 * is dominant in the later case.</p>
 * 
 * <p>The source code contains additional private functions that can be used to visualize and explain the entire computation process if 
 * used on a console. For this purpose remove the related commenting tags in the source code and recompile this file.</p>
 * 
 * <p>This class is thread safe but not optimized for the usage of multiple processors.</p>
 * 
 * <p>This class is licensed under the GNU GPLv3. It comes without any warranty or support. However, if you experience problems with
 * using it, please contact me on: <a href="http://j-levenshtein.sourceforge.net">http://j-levenshtein.sourceforge.net</a>.</p>
 * 
 * @author Rafael W.
 * @version 0.1b
 * @see ComparedStrings
 * @see LevFlag
 * @see PhoneticException
 * @see HashPoint
 */
public class Levenshtein {
	
	/**
	 * A key for a Levenshtein transformation
	 */
	public final static int CHAR_SUBSTITUTION = 0,
							CHAR_INDEL = CHAR_SUBSTITUTION + 1,
							CHAR_SWAP = CHAR_INDEL + 1,
							CHAR_SWAP_CASE = CHAR_SWAP + 1,
							SWAP_PHONETIC = CHAR_SWAP_CASE + 1,
							WORD_POSITION_ERROR = SWAP_PHONETIC + 1,
							PHONETIC_MAXIMUM_DIFFERENCE = WORD_POSITION_ERROR + 1;
	
	/**
	 * An array with all costs of an operation
	 */
	protected volatile float[] operationCosts = {2f, 1f, 1f, 1.5f, 1.5f, 0.95f, 20f};
	
	/**
	 * A path for finding the mappings for phonetic comparison
	 */
	protected volatile File featurePath = new File(Levenshtein.class.getResource("/ressource/features.txt").getFile()),
							savePath = new File(Levenshtein.class.getResource("/ressource/features.dat").getFile());

	/**
	 * A key for the costs of a specific transformation
	 */
	protected final static int 	ONE_WORD = 0,
								USE_PHONETIC = ONE_WORD + 1,
								MIND_CASE = USE_PHONETIC + 1,
								IGNORE_SPECIAL = MIND_CASE + 1,
								SWAP = IGNORE_SPECIAL + 1,
								ABSOLUTE = SWAP + 1;
	
	/**
	 * A key indication a transformation action
	 */
	public final static int 	ACTION_START = 0,
								ACTION_INSERT = ACTION_START + 1,
								ACTION_DELETE = ACTION_INSERT + 1,
								ACTION_SUBSTITUTE = ACTION_DELETE + 1,
								ACTION_SWAP = ACTION_SUBSTITUTE + 1,
								ACTION_REMAIN = ACTION_SWAP + 1;

	/**
	 * The flags for a Levenshtein distance computation
	 */
	protected volatile boolean[] currentFlags = {false, false, true, true, true, false};
	
	/**
	 * A lock to ensure no flags are changed while a computation is running
	 */
	private final ReadWriteLock rwLock = new ReentrantReadWriteLock();
	  
	/**
	 * Basic constructor that takes optional flags
	 * @param userFlags The flags of this object
	 */
	public Levenshtein(LevFlag... userFlags) {
		setFlag(userFlags);
	}
	
	/**
	 * Constructor that copies the flags of another Levenshtein object
	 * @param levenshtein The object that should be copied
	 */
	public Levenshtein(Levenshtein levenshtein) {
		
		levenshtein.rwLock.readLock().lock();
		
		currentFlags = levenshtein.currentFlags.clone();
		operationCosts = levenshtein.operationCosts.clone();
		featurePath = levenshtein.featurePath;
		savePath = levenshtein.savePath;
		
		levenshtein.rwLock.readLock().unlock();
		
	}
	
	/**
	 * Function that gives an indicator of how similar two given collections of words are
	 * @param firstInput The first String that is to be compared
	 * @param secondInput The first String that is to be compared
	 * @return An indicator variable that will be 1 for identical Strings and decreases to 0 for very unsimilar Strings
	 * @throws PhoneticException An exception that is throws if the input contains invalid phonetic symbols or similar
	 */
	public float compare(String firstInput, String secondInput) throws PhoneticException {
		
		rwLock.readLock().lock();
		try {
			return currentFlags[ABSOLUTE] 	? compareStrings(firstInput, secondInput, false).distance() 
											: compareStrings(firstInput, secondInput, false).index();
		} finally {
			rwLock.readLock().unlock();
		}
		
	}
	
	/**
	 * Function that gives an indicator of how similar two given collections of words are
	 * @param firstInput The first String that is to be compared
	 * @param secondInput The first String that is to be compared
	 * @return An map containing all details on the comparison
	 * @throws PhoneticException An exception that is throws if the input contains invalid phonetic symbols
	 */
	public ComparedStrings compareDetailed(String firstInput, String secondInput) 
		throws PhoneticException {
		
		rwLock.readLock().lock();
		try {
			return (ComparedStrings)compareStrings(firstInput, secondInput, true);
		} finally {
			rwLock.readLock().unlock();
		}
		
	}
	
	/**
	 * Function to declare calculation options on the computations
	 * @param userFlags The flags that should be used in the function
	 */
	public void setFlag(LevFlag... userFlags) {
		
		boolean[] flagLock = new boolean[currentFlags.length];
		
		rwLock.writeLock().lock();
		
		// Check for flags and set corresponding lock and array value
		for(int i = 0; i < userFlags.length; i++) {

			switch(userFlags[i]) {
			
				// Should XSAMPA be used? If so, disable swap is not set by user.
				case TREAT_AS_ONE_WORD: 	currentFlags[ONE_WORD] = flagLock[ONE_WORD] == false ? true : false; 
											break;
							
				case TREAT_AS_SENTANCE:		currentFlags[ONE_WORD] = false;
											flagLock[ONE_WORD] = true; 
											break;
			
				// Should XSAMPA be used? If so, disable swap is not set by user.
				case PHONETIC: 				currentFlags[USE_PHONETIC] = flagLock[USE_PHONETIC] == false ? true : false;
											currentFlags[SWAP] = flagLock[USE_PHONETIC] == true ? true : false;
											break;
								
				case PLAIN:					currentFlags[USE_PHONETIC] = false;
											flagLock[USE_PHONETIC] = true;
											break;
				
				// Should upper/lower case be treated as the same? (always ignored by XSAMPA)					
				case IGNORE_CASE:			currentFlags[MIND_CASE] = flagLock[MIND_CASE] == false ? false : true;
											break;
				
				case USE_CASE:				currentFlags[MIND_CASE] = true;
											flagLock[MIND_CASE] = true;
											break;
			
				// Should special characters be deleted?
				case KEEP_SPECIAL:			currentFlags[IGNORE_SPECIAL] = flagLock[IGNORE_SPECIAL] == false ? false : true;
											break;
				
				case STRIP_SPECIAL:			currentFlags[IGNORE_SPECIAL] = true;
											flagLock[IGNORE_SPECIAL] = true;
											break;
											
				// Allow or disallow to swap characters.
				case DISALLOW_SWAP:			currentFlags[SWAP] = flagLock[SWAP] == false ? false : true;
											break;
				
				case ALLOW_SWAP:			currentFlags[SWAP] = true;
											flagLock[SWAP] = true;	
											break;
											
				// Compute absolute or relative distance
				case ABSOLUTE:				currentFlags[ABSOLUTE] = flagLock[ABSOLUTE] == false ? true : false;
											break;
				
				case RELATIVE:				currentFlags[ABSOLUTE] = false;
											flagLock[ABSOLUTE] = true;	
											break;
			
			}
			
		}
		
		rwLock.writeLock().unlock();
		
	}

	/**
	 * Function that gives an indicator of how similar two given collections of words are
	 * @param firstOriginalInput The first user string that is to be compared
	 * @param secondOriginalInput The second user string that is to be compared
	 * @param computeFeatures <code>true</code> if a detailed comparison object should be computed
	 * @return A comparison object that contains the requested information
	 * @throws PhoneticException An exception that is thrown if the input contains invalid phonetic symbols
	 * @throws NullPointerException An exception that indicates that at least one of the input strings is not set
	 */
	private BasicCompared compareStrings(String firstOriginalInput, String secondOriginalInput, boolean computeFeatures) 
		throws PhoneticException, NullPointerException {
		
		// Throw NullPointerException if one of the inputs is not set
		if(firstOriginalInput == null || secondOriginalInput == null) {
			throw new NullPointerException("At least one of the Strings that was to be compared is not set.");
		}
		
		// If both inputs are identical return null
		if(firstOriginalInput.equals(secondOriginalInput) && computeFeatures == false) {
			return new BasicCompared(1f, 0, null, null, false);
		}
		
		//System.out.printf("Comparing: '%s' with '%s'%n", firstInput, secondInput);
		
		String[] wordsFirstInput;
		String[] wordsSecondInput;
		
		String firstInput;
		String secondInput;
		
		// Change to lower case if XAMPA is not used and user requested this
		if(currentFlags[USE_PHONETIC] == false && currentFlags[MIND_CASE] == false) {
			firstInput = firstOriginalInput.toLowerCase();
			secondInput = secondOriginalInput.toLowerCase();
		} else {
			firstInput = firstOriginalInput;
			secondInput = secondOriginalInput;
		}
		
		// Check for special characters and delete if this is requested by the user
		if(currentFlags[IGNORE_SPECIAL]) {
			
			String regex;
			
			// Prepare regex depending on XAMPA usage
			if(currentFlags[USE_PHONETIC] == true) {
				regex = "[0\\W&&\\S&&[^:\\\\}@&`?]]";
			} else {
				regex = "[\\W&&\\S]";
			}
			
			firstInput = firstInput.replaceAll(regex, "");
			secondInput = secondInput.replaceAll(regex, "");
		
		}
		
		String regexPattern = "\\s+";
		
		// Split down to words only if user does not request to match the entire String
		if(currentFlags[ONE_WORD]) {
			
			wordsFirstInput = new String[1];
			wordsSecondInput = new String[1];
			
			wordsFirstInput[0] = firstInput.trim().replaceAll(regexPattern, " ");
			wordsSecondInput[0] = secondInput.trim().replaceAll(regexPattern, " ");
			
		} else {

			wordsFirstInput = firstInput.trim().split(regexPattern);
			wordsSecondInput = secondInput.trim().split(regexPattern);
			
		}
		
		//System.out.printf("Transformed to: '%s' and '%s'%n", firstInput, secondInput);
		//System.out.printf("************************%n");
		
		// Jump to compare words function is only words are found
		if(wordsFirstInput.length == 1 && wordsSecondInput.length == 1) {
			
			// Create dummy object
			float[][] matrix = getMatrix(wordsFirstInput[0], wordsSecondInput[0]);
			Map<Integer,Integer> overallComparedMapping = new HashMap<Integer,Integer>(1);
			overallComparedMapping.put(1, 1);
			
			// Compute basic return object
			BasicCompared basicCompared = new BasicCompared(getIndex(matrix), matrix[matrix.length-1][matrix[0].length-1], 
					overallComparedMapping, overallComparedMapping, false);
			
			if(computeFeatures == false) {
				return basicCompared;
			
			} else {
				float[][] comparedResults = {{getIndex(matrix)}};
				Map<HashPoint,float[][]> matrixCollection = new HashMap<HashPoint,float[][]>(1);
				matrixCollection.put(new HashPoint(0, 0), matrix);
				
				return new ComparedStrings(basicCompared, firstOriginalInput, secondOriginalInput, wordsFirstInput, wordsSecondInput, 
						comparedResults, matrixCollection);
				
			}
		}
		
		boolean stringsSwapped = false;
			
		// Set the longer array to be the first if necessary to get deterministic results
		if(wordsFirstInput.length < wordsSecondInput.length) {
			
			String[] temp = wordsFirstInput;
			wordsFirstInput = wordsSecondInput;
			wordsSecondInput = temp;
			
			stringsSwapped = true;
			
			//System.out.println("Swapped values, first argument is now second argument and vice versa.");
			
		}
		
		// Setting up variables to compare more than two words
		float[][] comparedResults = new float[wordsFirstInput.length][wordsSecondInput.length];
		List<ArrayList<Integer>> orderedResults = new ArrayList<ArrayList<Integer>>(wordsFirstInput.length);
		Map<HashPoint,float[][]> matrixCollection = new HashMap<HashPoint,float[][]>(wordsFirstInput.length*wordsSecondInput.length);
		
		// Set up some other variables
		float[] sumResults = new float[wordsFirstInput.length];
		HashPoint tempID;

		// Create matrix that gives similarity of single words as its values
		for(int i = 0; i < wordsFirstInput.length; i++) {
			
			// Add new element to Array list
			orderedResults.add(i, new ArrayList<Integer>());
			
			for(int j = 0; j < wordsSecondInput.length; j++) {
				
				// Get word matrix for these words and fill result into matrix for sequences
				float[][] matrix = getMatrix(wordsFirstInput[i], wordsSecondInput[j]);
				
				// Store matrix in map if detailed results are required
				if(computeFeatures == true) {
					tempID = stringsSwapped ? new HashPoint(j, i) : new HashPoint(i, j);
					matrixCollection.put(tempID, matrix);
				}

				// Store index in sequence matrix and sum index up to measure its volaitlity
				comparedResults[i][j] = getIndex(matrix) * 
					(float)Math.pow(operationCosts[WORD_POSITION_ERROR], Math.abs(i-j));
				sumResults[i] += comparedResults[i][j];
				
				// Find best match to use iteration for first result
				for (int k = 0; k <= j; k++) {
					
					// Check if it is time to add value (to assure descending ordering)
					if(j == 0 || k == j || comparedResults[i][j] >= comparedResults[i][orderedResults.get(i).get(k)]) {
						
						//System.out.print("- Adding " + j + " @ " + k + " - ");
						orderedResults.get(i).add(k, j);
						break;
						
					}
				
				}
				
				// Print resulting table (debugging)
				//System.out.printf("%d with %d: %.2f | ", i, j, comparedResults[i][j]);
				
			}
			
			// Print new line (debugging)
			//System.out.println();
			
		}
		
		//System.out.printf("************************%n");
		
		// Compute basic return object
		BasicCompared basicCompared = matchWordSequences(wordsFirstInput, wordsSecondInput, comparedResults, orderedResults, 
				sumResults, stringsSwapped);
		
		// Decide whether to compute features and return comparison object
		if(computeFeatures == false) {
			return basicCompared;
		
		// Compute more complex comparison object to return to user
		} else {
			
			return new ComparedStrings(basicCompared, firstOriginalInput, secondOriginalInput, wordsFirstInput, wordsSecondInput, 
					comparedResults, matrixCollection);
		}
		
	}
	
	/**
	 * This function computes a best matching of the words that were compared before. It requires that the 
	 * firstInput sequence contains at least as many words as the second sequence.
	 * @param wordsFirstInput The sequence of words of the first input
	 * @param wordsSecondInput The sequence of words of the second input
	 * @param comparedResults The matrix of compared results (of the words of the subsequence)
	 * @param sumResults The sum of index values over all values
	 * @param orderedResults The order of best words to match with a given String
	 * @param stringsSwapped An indicator to signal that the input strings have been swapped
	 * @return A basic object that contains any relevant information gained in this function
	 * 		Also this information is sufficient to compute simple distance
	 */
	private BasicCompared matchWordSequences(String[] wordsFirstInput, String[] wordsSecondInput, 
			float[][] comparedResults, List<ArrayList<Integer>> orderedResults, float[] sumResults, boolean stringsSwapped) {
		
		// Create collection to store used keys
		Collection<Integer> usedKeys = new HashSet<Integer>(); 
		Collection<Integer> usedRows = new HashSet<Integer>(); 
		
		// Initialize map to output details
		Map<Integer,Integer> firstComparedMapping = new HashMap<Integer,Integer>();
		Map<Integer,Integer> secondComparedMapping = new HashMap<Integer,Integer>();
		
		float sumOfBestValues = 0;
		float totalWeight = 0;
		
		// Run routine to determine best match (greedy)
		// Check every word in the smaller array for similarities
		for (int i = 0; i < wordsSecondInput.length; i++) {
			
			// Initialize first row as the best remaining key for this row and check it against the other
			int rowWithBestValue;
			for(rowWithBestValue = 0; rowWithBestValue < orderedResults.size(); rowWithBestValue++) {
				if (usedRows.contains(rowWithBestValue) == false) break;
			}

			int bestRowsBestKey = getBestRemainingKey(orderedResults.get(rowWithBestValue), usedKeys);
			float bestRowMatchingFactor = relativeMatchingFactor(rowWithBestValue, comparedResults[rowWithBestValue], usedKeys);
			
			// Iterate through rows (skipping rows that were added already and skip the next since this row
			// does not need to be compared with itself
			for (int j = rowWithBestValue + 1; j < orderedResults.size(); j++) {
				
				// Skip row if already implemented
				if (usedRows.contains(j)) continue;
				
				// Reset best remaining key to the first position
				int bestRemainingKey = getBestRemainingKey(orderedResults.get(j), usedKeys);
				float currentRowMatchingFactor = relativeMatchingFactor(bestRemainingKey, comparedResults[j], usedKeys);

				// Set new row as best row if its result is superior to the former best row (greedy)
				// Also save the key of this row since all coming rows will now be compared to this row
				// Take into account the matching of one value to the remaining rows (Add a minimal value to avoid
				// division by zero.
				if(currentRowMatchingFactor > bestRowMatchingFactor) {
					rowWithBestValue = j;
					bestRowsBestKey = bestRemainingKey;
					bestRowMatchingFactor = currentRowMatchingFactor;
				}
				
				// Summary output to console (debugging)
				/*System.out.printf("Best remaining key for row %d is %d yielding: %.2f (relative factor: %.2f)" +
						"%nThe best row so far: %d with %.2f (relative factor: %.2f)%n", 
						j, bestRemainingKey, comparedResults[j][bestRemainingKey], currentRowMatchingFactor, rowWithBestValue, 
						comparedResults[rowWithBestValue][bestRowsBestKey], bestRowMatchingFactor);
				System.out.printf("The relative matching factors are %.2f for %d and %.2f for %d (current best row)%n", 
						currentRowMatchingFactor, j, bestRowMatchingFactor, rowWithBestValue);
				System.out.println("--------------");*/
			
			}
			
			// Saves sum and remove respectively mark used row and column
			float currentWeight = Math.min(wordsFirstInput[rowWithBestValue].length(), wordsSecondInput[bestRowsBestKey].length()) * 
				operationCosts[CHAR_SUBSTITUTION] + Math.abs(wordsFirstInput[rowWithBestValue].length() - 
				wordsSecondInput[bestRowsBestKey].length()) * operationCosts[CHAR_INDEL];
			
			totalWeight += currentWeight;
			sumOfBestValues += comparedResults[rowWithBestValue][bestRowsBestKey] * currentWeight;
			
			// Add keys to containers that indicates that they were used
			usedKeys.add(bestRowsBestKey);
			usedRows.add(rowWithBestValue);
			
			// Save details in mapping variable in direction of the original input (indicated by stringsSwapped)
			// Construct two sets to make search more easy
			if (stringsSwapped == false) {
				firstComparedMapping.put(rowWithBestValue, bestRowsBestKey);
				secondComparedMapping.put(bestRowsBestKey, rowWithBestValue);
				
			} else {
				firstComparedMapping.put(bestRowsBestKey, rowWithBestValue);
				secondComparedMapping.put(rowWithBestValue, bestRowsBestKey);
			}
			
			// Print information about operations (debugging)
			/*System.out.printf("=> Row %d selected: '%s' and matched to '%s'%n" +
					"=> Current value: %.2f (accuracy: %.2f, weight: %.1f, amount added: %.2f)%n", rowWithBestValue, 
					wordsSecondInput[bestRowsBestKey], wordsFirstInput[rowWithBestValue], sumOfBestValues, 
					comparedResults[rowWithBestValue][bestRowsBestKey], currentWeight, 
					comparedResults[rowWithBestValue][bestRowsBestKey] * currentWeight);
			System.out.println("************************");*/
			
		}
		
		// Add weight of words that were not matched
		for (int i = 0; i < wordsFirstInput.length; i++) {
			if (usedRows.contains(i)) continue;
			totalWeight += wordsFirstInput[i].length() * operationCosts[CHAR_INDEL];
		}
		
		// Correct for white spaces
		totalWeight += (Math.min(wordsFirstInput.length, wordsSecondInput.length) - 1f) * operationCosts[CHAR_SUBSTITUTION] +
			Math.abs(wordsFirstInput.length - wordsSecondInput.length) * operationCosts[CHAR_INDEL];
		sumOfBestValues += (Math.min(wordsFirstInput.length, wordsSecondInput.length) - 1f) * operationCosts[CHAR_SUBSTITUTION];
		
		// Print information about total result (debugging)
		//System.out.printf("Total value: %.2f - Total weight: %.1f%n", sumOfBestValues, totalWeight);
		//System.out.printf("Final accuracy: %.2f%n%n", sumOfBestValues / totalWeight);
		
		// Print (first) resulting map to console (debugging)
		//Map<Integer,Integer> tempComparedMapping = stringsSwapped == true ? secondComparedMapping : firstComparedMapping;
		//printMatchMap(tempComparedMapping, comparedResults, wordsFirstInput, wordsSecondInput);
		//System.out.printf("%n////////////////////////////////////////////////////%n%n");
		
		// Return basic comparison object that holds basic information needed for simple comparisons
		return new BasicCompared(sumOfBestValues / totalWeight, sumOfBestValues, 
			firstComparedMapping, secondComparedMapping, stringsSwapped);
		
	}
	
	/**
	 * Computes a correction factor for a specified key (with respect to the remaining rows exp. value)
	 * @param keyOfInterest The key that is currently selected for the particular row
	 * @param rowComparedResults The compared values of the row that is to be analyzed
	 * @param usedKeys The keys that should not be taken into account
	 * @return The expected value of the remaining rows
	 */
	private static float relativeMatchingFactor(int keyOfInterest, float[] rowComparedResults, Collection<Integer> usedKeys) {
	
		float rowOthersExpectedValue = 0;
		int addedRows = 0;
		
		// Compute expected value of the remaining keys (correct for NaN later)
		for (int i = 0; i < rowComparedResults.length; i++) {
			if(i != keyOfInterest && usedKeys.contains(i) == false) {
				rowOthersExpectedValue += rowComparedResults[i];
				addedRows++;
			}
		}

		rowOthersExpectedValue /= addedRows == 0 ? 1 : addedRows;
		//System.out.printf("Relative matching factor of key %d: %.2f (Other keys exp. value: %.2f)%n", keyOfInterest, 
		//		rowComparedResults[keyOfInterest] - rowOthersExpectedValue, rowOthersExpectedValue);
		
		return rowComparedResults[keyOfInterest] - rowOthersExpectedValue;
		
	}
	
	
	/**
	 * A helper function that identifies the best remaining key
	 * @param orderedResultsForKey The list with ordered keys for the called entry
	 * @param usedKeys The set of used keys
	 * @return The best remaining key
	 */
	private static int getBestRemainingKey(List<Integer> orderedResultsForKey, Collection<Integer> usedKeys) {
		
		int bestRemainingKey = orderedResultsForKey.get(0);
		
		// Find best remaining key in the ordered list, break when found
		for (int i = 1; i < orderedResultsForKey.size(); i++) {
			
			// Get the best key that is not yet used and remove if used
			if(usedKeys.contains(bestRemainingKey) == false) {
				break;
			}
			
			bestRemainingKey = orderedResultsForKey.get(i);
			//System.out.print("Checking Key: " + bestRemainingKey + " |�");
			
		}
		
		return bestRemainingKey;
		
	}
	
	/**
	 * Transforms a Damerau-Levenshtein distance into a [0,1] scaled index.
	 * @param matrix The matrix of all distances
	 * @return The overall similarity index value
	 */
	private float getIndex(float[][] matrix) {
		
		float indexDenominator;
		float levenshteinDistance = matrix[matrix.length - 1][matrix[0].length - 1];
		
		// Compute denominator of index depending on whether the distance is based XSAMPA or on plain
		if(currentFlags[USE_PHONETIC]) {
			
			indexDenominator = operationCosts[PHONETIC_MAXIMUM_DIFFERENCE] * 2f * (Math.min(matrix.length, matrix[0].length) - 1f) + 
				operationCosts[PHONETIC_MAXIMUM_DIFFERENCE] * Math.abs(matrix.length - matrix[0].length);
		
		// Compute for Plain comparison (Is 0 for very unsimilar words and 1 iff both words are equal)
		} else {
	
			indexDenominator = operationCosts[CHAR_SUBSTITUTION] * (Math.min(matrix.length, matrix[0].length) - 1f) + 
				operationCosts[CHAR_INDEL] * Math.abs(matrix.length - matrix[0].length);
			
		}
		
		// Print out the result to console (debugging)
		//System.out.printf("The index value is: %.2f with a Levenshtein distance of %.2f and a denominator of %.2f%n", 
		//		1f - levenshteinDistance / indexDenominator, levenshteinDistance, indexDenominator);
		//System.out.println("----------------------");
		
		return 1f - levenshteinDistance / indexDenominator;
		
	}
	
	/**
	 * Yields the XSAMPA symbols of the given Strings if phonetic comparison is requested
	 * @param firstWord The first word that is to be compared
	 * @param secondWord The second word that is to be compared
	 * @return A list containing all the XSAMPA symbols (0 = first word, 1 = second word)
	 * @throws PhoneticException An exception that indicates invalid XSAMPA symbols
	 */
	private static List<String[]> getSymbols(String firstWord, String secondWord) throws PhoneticException {
		
		// Set up container, 0 = first word, 1 = second word
		List<String[]> wordArray = new ArrayList<String[]>(2);
		
		// Create regex to match pattern
		Pattern symbolPattern = Pattern.compile("((?:\\S[\\\\`]{0,2})(?::(?:\\\\)?)?)");
		Matcher matcher;
		
		// Prepare both words
		for (int i = 0; i < 2; i++) {
			
			// Temporary lists
			List<String> tempWordSymbols;
			
			// Set pointers to particular variable
			String tempWord = i == 0 ? firstWord : secondWord;
			
			// Prepare pattern
	    	matcher = symbolPattern.matcher(tempWord);
	    	tempWordSymbols = new ArrayList<String>();
	
	    	// Write all matches to temporary array
	        while (matcher.find()) {
	        	tempWordSymbols.add(matcher.group(1));
	        }
	        
	        // Add final result to list
	        wordArray.add(tempWordSymbols.toArray(new String[0]));
	        
	        // Print results to console (debugging)
	        //System.out.printf("Phonetic matches for word %d: '%s'%n", i, tempWord);
	        //for(int j = 0; j < tempWordSymbols.size(); j++) {
	        //	System.out.printf("Symbol %d: %s%n", j, wordArray.get(i)[j]);
	        //}
	        
		}
		
		//System.out.println("****************");
		
		// Throw exception if at least one of the words does not contain phonetic expressions
		if(wordArray.get(0).length == 0) {
			System.err.printf("The word '%s' does not contain any phonetic expressions", firstWord);
			throw new PhoneticException("The first input does not contain any phonetic expression.");
		} else if (wordArray.get(1).length == 0) {
			System.err.printf("The word '%s' does not contain any phonetic expressions", secondWord);
			throw new PhoneticException("The second input does not contain any phonetic expression.");
		}
		
		return wordArray;
	
	}
	
	/**
	 * A function that gets the distance to zero for all strings to avoid double calculation
	 * @param containers String arrays that contain the symbols to look up
	 */
	private Map<String,Float> getZeroDistance(String[]... containers) throws PhoneticException {
		
		Map<String,Float> symbolZeroDistances = new HashMap<String,Float>();
		
		for(int i = 0; i < containers.length; i++) {
			for (int j = 0; j < containers[i].length; j++) {
				
				// Check if key exists, if not, compute phonetic difference to zero
				if(symbolZeroDistances.containsKey(containers[i][j]) == false) {
					symbolZeroDistances.put(containers[i][j], getPhoneticDifference(containers[i][j], "0"));
				}
				
			}
		}
		
		return symbolZeroDistances;
		
	}
	
	/**
	 * A function that establishes a fully build Damerau-Levenshtein matrix
	 * @param firstWord The first word that is analyzed
	 * @param secondWord The second word that is analyzed
	 * @return The Damerau-Levenshtein matrix
	 */
	private float[][] getMatrix(String firstWord, String secondWord) {
		
		// Create pointer
		float[][] matrix;
		int firstSequenceLength, secondSequenceLength;
		
		// Create matrix with needed user requirements 
		if(currentFlags[USE_PHONETIC]) {
			
			List<String[]> symbols = getSymbols(firstWord, secondWord);
			Map<String,Float> symbolZeroDistances = getZeroDistance(symbols.get(0), symbols.get(1));
			matrix = createMatrixPhonetic(symbols, symbolZeroDistances);
			fillMatrixPhonetic(matrix, symbols, symbolZeroDistances);
			
			// Print Damerau-Levenshtein matrix (debugging)
			//printMatrix(matrix, symbols.get(0), symbols.get(1));
			
			// Adjust length of Strings to number of symbols
			firstSequenceLength = symbols.get(0).length;
			secondSequenceLength = symbols.get(1).length;

		} else {
			
			firstSequenceLength = firstWord.length();
			secondSequenceLength = secondWord.length();
			
			matrix = createMatrixCasual(firstSequenceLength, secondSequenceLength);
			fillMatrixCasual(matrix, firstWord, secondWord);
			
			// Print Damerau-Levenshtein matrix (debugging)
			//printMatrix(matrix, firstWord.toCharArray(), secondWord.toCharArray());
			
		}
		
		return matrix;
		
	}
	
	/**
	 * Create matrix to run basic Damerau-Levenshtein algorithm
	 * @param firstWordLength The String length of the first word that will be compared
	 * @param secondWordLength The String length of the second word that will be compared
	 * @return A partly prepared matrix with the correct dimensions
	 */
	private float[][] createMatrixCasual(int firstWordLength, int secondWordLength) {
		
		float[][] matrix = new float[firstWordLength + 1][secondWordLength + 1];
		matrix[0][0] = 0;
		
		for(int i = 1; i <= firstWordLength; i++) {
			matrix[i][0] = operationCosts[CHAR_INDEL] * i;
		}
		
		for(int i = 1; i <= secondWordLength; i++) {
			matrix[0][i] = operationCosts[CHAR_INDEL] * i;
		}
		
		return matrix;
		
	}
	
	/**
	 * Create matrix to run Damerau-Levenshtein algorithm with phonetic (XSAMPA) comparison
	 * @param symbols A list with all symbols of this comparison
	 * @param symbolZeroDistances A map with all distances of any symbol to the zero symbol
	 * @return A partly prepared matrix with the correct dimensions
	 */
	private static float[][] createMatrixPhonetic(List<String[]> symbols, Map<String,Float> symbolZeroDistances) {
		
		float[][] matrix = new float[symbols.get(0).length + 1][symbols.get(1).length + 1];
		matrix[0][0] = 0;
		
		for(int i = 1; i <= symbols.get(0).length; i++) {
			matrix[i][0] = matrix[i-1][0] + symbolZeroDistances.get(symbols.get(0)[i-1]);
		}
		
		for(int i = 1; i <= symbols.get(1).length; i++) {
			matrix[0][i] = matrix[0][i-1] + symbolZeroDistances.get(symbols.get(1)[i-1]);
		}
		
		return matrix;
		
	}
	
	/**
	 * Run basic Damerau-Levenshtein algorithm on prepared matrix (object will not be cloned)
	 * Apply normal (not phonetic) Damerau-Levensthein algorithm
	 * @param matrix The matrix that is to be filled
	 * @param firstWord The first word that is to be compared
	 * @param secondWord The second word that is to be compared
	 */
	private float[][] fillMatrixCasual(float[][] matrix, String firstWord, String secondWord) {
		
		// Initiate basic variables
		float costCross;
		
		final int firstWordLength = firstWord.length();
		final int secondWordLength = secondWord.length();
		
		char[] firstWordChars = firstWord.toCharArray();
		char[] secondWordChars = secondWord.toCharArray();
		
		// Fill matrix with values that describe word similarity
		for (int j = 1; j <= secondWordLength; j++) {
			for (int i = 1; i <= firstWordLength; i++) {
				
				// Do not add costs for identical characters into the cross row of the matrix
				if(firstWordChars[i-1] == secondWordChars[j-1]) {
					costCross = matrix[i-1][j-1] + 0f;
					
				// Add costs for swap if characters can be swapped
				} else if(currentFlags[SWAP] && i != 1 && j != 1 && firstWordChars[i-1] == secondWordChars[j-2] && 
						firstWordChars[i-2] == secondWordChars[j-1]) {
					costCross = matrix[i-2][j-2] + operationCosts[CHAR_SWAP];
					
				// Set costs for swap if just the case of the letters is wrong
				} else if(currentFlags[MIND_CASE] && Character.toLowerCase(firstWordChars[i-1]) == Character.toLowerCase(secondWordChars[j-1])) {
					costCross = matrix[i-1][j-1] + operationCosts[SWAP_PHONETIC];				
				
				// Add costs for swap if characters can be swapped
				} else if(currentFlags[MIND_CASE] && i != 1 && j != 1 && 
						Character.toLowerCase(firstWordChars[i-1]) == Character.toLowerCase(secondWordChars[j-2]) && 
						Character.toLowerCase(firstWordChars[i-2]) == Character.toLowerCase(secondWordChars[j-1])) {
					costCross = matrix[i-2][j-2] + operationCosts[CHAR_SWAP_CASE];
					
				// Add costs for character substitution if none of the above applies
				} else {
					costCross = matrix[i-1][j-1] + operationCosts[CHAR_SUBSTITUTION];
					
				}
				
				// Add best value to matrix path
				matrix[i][j] = Math.min(Math.min(matrix[i][j-1] + operationCosts[CHAR_INDEL], matrix[i-1][j] + operationCosts[CHAR_INDEL]), costCross);
				
			}
		}
		
		return matrix;
		
	}
	
	/**
	 * Run Damerau-Levenshtein algorithm on prepared matrix with phonetic values (object will not be cloned)
	 * @param matrix The matrix that should be filled (using phonetic values)
	 * @param symbols The symbols of the word that are to be compared, 0 = first word, 1 = second word
	 * @param symbolZeroDistances The distance of all words to zero
	 * @return The filled matrix
	 * @throws PhoneticException An exception that is throws if the input contains invalid phonetic symbols
	 */
	private float[][] fillMatrixPhonetic(float[][] matrix, List<String[]> symbols, 
			Map<String,Float> symbolZeroDistances) throws PhoneticException {
		
		// Initiate basic variables
		float costCross, costSwap, tempDistance;
		
		// Fill matrix with values that describe word similarity, taking into account the phonetic notation
		for (int j = 1; j <= symbols.get(1).length; j++) {
			for (int i = 1; i <= symbols.get(0).length; i++) {
				
				// Compute costs of possible swapping of characters, if allowed, set to maximum otherwise
				// Do this in a separate step since a substitution could still be less expensive with XSAMPA
				if (currentFlags[SWAP] && i != 1 && j != 1 && symbols.get(0)[i-1].equals(symbols.get(1)[j-2]) && 
						symbols.get(0)[i-2].equals(symbols.get(1)[j-1])) {
					costSwap = matrix[i-2][j-2] + operationCosts[SWAP_PHONETIC];
				} else {
					costSwap = Float.MAX_VALUE;
				}
				
				// Do not add costs for identical characters into the crossrow of the matrix
				if(symbols.get(0)[i-1].equals(symbols.get(1)[j-1])) {
					costCross = matrix[i-1][j-1] + 0f;
					
				// Add costs for character substitution if none of the above applies (phonetic difference)
				// If function returns -1, distance of both symbols to zero should be added.
				} else {
					
					// Check if one of the values is 0 and use zero-distances table, if so
					if(symbols.get(0)[i-1].equals("0")) {
						tempDistance = symbolZeroDistances.get(symbols.get(1)[j-1]);
					} else if(symbols.get(1)[j-1].equals("0")) {
						tempDistance = symbolZeroDistances.get(symbols.get(0)[i-1]);
					} else {
						tempDistance = getPhoneticDifference(symbols.get(0)[i-1], symbols.get(1)[j-1]);
					}
					
					// Set cross row value, if indicated, set value as sum of substitutions
					if (tempDistance == -1f) {
						
						costCross = matrix[i-1][j-1] + symbolZeroDistances.get(symbols.get(0)[i-1]) + 
							symbolZeroDistances.get(symbols.get(1)[j-1]);
						//System.out.printf("Vowel / consonant mismatching at cell (%d,%d). Distance value %.2f substituted.%n", 
						//		i, j, matrix[i][j]);
						
					} else {
						
						costCross = matrix[i-1][j-1] + tempDistance;
						
					}
					
				}
				
				// Print all possibilities to console (debugging)
				/*System.out.printf("(%d,%d) : (%s,%s) From cross: %.2f |�from up: %.2f | from left: %.2f%n", i, j, symbols.get(0)[i-1], 
						symbols.get(1)[j-1], costCross, matrix[i-1][j] + symbolZeroDistances.get(symbols.get(0)[i-1]), 
						matrix[i][j-1] + symbolZeroDistances.get(symbols.get(1)[j-1]));*/
				
				// Add best value to matrix path (by phonetic difference)
				matrix[i][j] = Math.min(Math.min(matrix[i-1][j] + symbolZeroDistances.get(symbols.get(0)[i-1]), 
						matrix[i][j-1] + symbolZeroDistances.get(symbols.get(1)[j-1])), Math.min(costCross, costSwap));
				
			}
		
		}
			
		return matrix;
		
	}
	
	/**
	 * Function that computes the phonetic difference between two phonetic symbols
	 * @param firstSymbol The first phonetic sequence
	 * @param secondSymbol The second phonetic sequence
	 * @return The absolute difference (total difference, not the 0-1 indicator)
	 * @throws PhoneticException An exception that is thrown if one of the patterns contains undefined symbols
	 */
	protected float getPhoneticDifference(String firstSymbol, String secondSymbol) throws PhoneticException {
		
		// Return default if symbols are equal
		if(firstSymbol.equals(secondSymbol)) return 0f; 
		
		// Read map and try to find keys
		Map<String, Float[]> phoneticMap = readPhonetics(false);
		
		Float[] firstFeatures = phoneticMap.get(firstSymbol);
		Float[] secondFeatures = phoneticMap.get(secondSymbol);
		
		// Create String with name of the phonetic keys (debugging)
		//final String[] keyNames = {"vowel", "adv", "height", "round", "consonant", "place", "manner", "voice", "long"};
		
		// If keys do not exist, throw Exception
		if(firstFeatures == null || secondFeatures == null) {
			
			if(firstFeatures == null) {
				System.err.printf("Phonetic sequence contains symbols that are not defined. (Failed to interpret: '%s')", 
						firstSymbol);
			}
			
			if(secondFeatures == null) {
				System.err.printf("Phonetic sequence contains symbols that are not defined. (Failed to interpret: '%s')", 
					secondSymbol);
			}
			
			throw new PhoneticException("The phonetic pattern contains symbols that are not defined.");
			
		}
		
		// Print entire features array to console (debugging)
		//for(int i = 0; i < firstFeatures.length; i++) {
		//	System.out.printf("Feature %d: %.2f ('%s') | %.2f ('%s')%n", i, firstFeatures[i], 
		//		firstPhonetic, secondFeatures[i], secondPhonetic);
		//}
		
		// Calculate difference (check for vowel (0) and consonant (4) keys
		float totalDistance = 0f;
		
		// Write out information to console (debugging)
		//System.out.printf("Comparing features of: '%s' (keys: %s = %s, %s = %s) and '%s' (keys %s: %s, %s: %s)%n", 
		//		firstSymbol, keyNames[0], firstFeatures[0], keyNames[4], firstFeatures[4], secondSymbol, keyNames[0], 
		//		secondFeatures[0], keyNames[4], secondFeatures[4]);
		
		// Check if vowel and consonant feature of both symbols are defined (keys 0 and 4)
		if(firstFeatures[0] == null || firstFeatures[4] == null || secondFeatures[0] == null || secondFeatures[4] == null) {
				
			if(firstFeatures[0] == null || firstFeatures[4] == null) {
				System.err.printf("Phonetic map does not define all vital features. (Failed to interpret: '%s')", 
						firstSymbol);
			} 
			if(secondFeatures[0] == null || secondFeatures[4] == null) {
				System.err.printf("Phonetic map does not define all vital feature. (Failed to interpret: '%s')", 
						secondSymbol);
			}
			
			throw new PhoneticException("The phonetic map contains symbols that are not well defined.");
				
			}
		
		// If two symbols are both of different type (one vowel, other consonant) return -1f to indicate that
		// the distance of both symbols to '0' should be used (this sum must be computed anyhow s.t. this internal
		//function only indicates this, this is meant to accelerate the computation
		if(firstFeatures[0].equals(secondFeatures[0]) == false && firstFeatures[4].equals(secondFeatures[4]) == false) {
			
			//System.out.println("Skipping further calculation and suggesting to sum both distances to vowels.");
			return -1f;
			
		}
		
		// Check both indicator variables separately
		for (int i = 0; i < 5; i+=4) {
				
			// Calculate difference if both symbols are vowels respectively consonants
			if(firstFeatures[i].floatValue() == 1f && firstFeatures[i].equals(secondFeatures[i])) {
				
				// Print out match for key to console
				//System.out.printf("Both symbols are true for key '%s' %n", keyNames[i]);
				
				// Feature space is described by the following 3 variables
				for(int j = 1; j < 4; j++) {
					
					// Adding up absolute differences of the feature space
					if(firstFeatures[i+j] != null && secondFeatures[i+j] != null) {
						
						totalDistance += Math.abs(firstFeatures[i+j].floatValue() - secondFeatures[i+j].floatValue());
						//System.out.printf("Adding key %d ('%s') with difference: %.2f%n", i+j, keyNames[i+j], 
						//		Math.abs(firstFeatures[i+j] - secondFeatures[i+j]));
					
					}
					
				}

			}
		
		}
		
		// Initiate weight variables
		float firstWeight;
		float secondWeight;
		
		// If the symbols are both vowels and consonant adjust distance since the double amount of features was used.
		// Also, set the weight for feature 8 (2x weight if variable is a vowel, 1.5x weight if the variables are both 
		// vowels and consonants.
		// -------------------
		// Check if the symbols are both vowels and consonants
		if(firstFeatures[0].floatValue() == 1f && firstFeatures[4].floatValue() == 1f && firstFeatures[0].equals(secondFeatures[0]) && 
				firstFeatures[4].equals(secondFeatures[4])) {
			
			totalDistance /= 2;
			firstWeight = secondWeight = 1.5f;
		
		// Check if at least one of the sound properties fits but maybe distinguish if compared to zero value.
		} else if((firstFeatures[0].floatValue() == 1f && firstFeatures[0].equals(secondFeatures[0])) || 
				(firstSymbol.equals("0") == false && secondSymbol.equals("0") == false && secondFeatures[4].floatValue() == 1f && 
				secondFeatures[4].equals(secondFeatures[4]))) {
			
			firstWeight = secondWeight = 1f;
			
		// Find individual weights for both symbols if non of the above is true
		} else {
			
			firstWeight = firstFeatures[0].floatValue() == 1f ? 1f : 2f;
			secondWeight = secondFeatures[0].floatValue() == 1f ? 1f : 2f;
			
		}
		
		//System.out.println(firstWeight + " " + secondWeight);
		
		// Add last feature 'long' that is independent of vowel/consonant feature (key 8)
		if(firstFeatures[8] == null || secondFeatures[8] == null) {
			throw new PhoneticException("The phonetic map does not define the mandatory 'long' feature.");
		} else {
			totalDistance += Math.abs(firstFeatures[8].floatValue() * firstWeight - secondFeatures[8].floatValue() * secondWeight);
			//System.out.printf("Adding key %d ('%s') with difference: %.2f%n", 8, keyNames[8], 
			//	Math.abs(firstFeatures[8] * firstWeight - secondFeatures[8] * secondWeight));
			
		}
		
		//System.out.printf("Total distance of '%s' and '%s': %.2f%n****************%n", firstSymbol, secondSymbol, totalDistance);
		return totalDistance;
	}
	
	/**
	 * A function that finds the ideal path through a Damerau-Levenshtein matrix
	 * @param matrix The completed matrix for which the path should be found
	 * @return An map of coordinates that represent the path 
	 * 		(0 = starting point, 1 = deleted, 2 = inserted, 3 = substituted, 4 = swapped)
	 */
	protected static Map<HashPoint,Integer> getIdealPath(float[][] matrix) {
		
		// Return null if matrix is null (for public call of function in ComparedStrings)
		if(matrix == null) return null;
		
		// Set up basic variables
		Map<HashPoint,Integer> coordinates = new HashMap<HashPoint,Integer>();
		
		int x = matrix[0].length - 1;
		int y = matrix.length - 1;
		Integer method;
		
		// Add final point (build path from final)
		HashPoint point = new HashPoint(0, 0);
		coordinates.put(point, ACTION_START);
		
		// Go through matrix and break if limit is reached
		for(int i = 0; i < matrix.length + matrix[0].length - 2; i++) {
			
			// Break if limit is reached (faster than maximum part which is the sum minus two)
			if(y == 0 && x == 0) {
				break;
			}

			// Check if a letter swap would be an option
			if(x > 1 && y > 1 && matrix[y-1][x-1] > matrix[y][x]) {
				point = new HashPoint(x, y);
				x-=2; y-=2;
				method = ACTION_SWAP;
			
			// Check if going down or right is a strictly better option, if not go cross, check for matrix bounds
			// If going down and right would yield identical values, go right
			// Also check if going right or down does not yield a "dead end" where a later substitution must be done anyways
			} else if(y == 0 || (x > 0 && y > 0 && matrix[y][x-1] < Math.min(matrix[y-1][x-1], matrix[y-1][x]))) {
				point = new HashPoint(x--, y);
				method = ACTION_INSERT;
				
			} else if(x == 0 || (y > 0 && y > 0 && (matrix[y-1][x] < Math.min(matrix[y-1][x-1], matrix[y][x-1])))) {
				point = new HashPoint(x, y--);
				method = ACTION_DELETE;
				
			} else {
				point = new HashPoint(x--, y--);
				if(matrix[y][x] == matrix[y+1][x+1]) {
					method = ACTION_REMAIN;
				} else {
					method = ACTION_SUBSTITUTE;
				}
				
			}
			
			// Print path to console
			//System.out.printf("Bachward induction: Moved to (%d,%d) by action id: %d%n", point.x, point.y, method);
			
			// Add point to list
			coordinates.put(point, method);
				
		}
		
		return coordinates;
		
	}
	
	/**
	 * Read and setup feature map for phonetic distances
	 * @param forceReadOriginal Force to read the variable from the original file if true
	 * @return The matrix explaining the vocal distance
	 */
	@SuppressWarnings("unchecked")
	private Map<String, Float[]> readPhonetics(boolean forceReadOriginal) {
		
		// Initialize pointer
		Map<String, Float[]> phoneticMap;
		
		// Before reading original file, check if there is already a valid copy on the hard drive
		if(forceReadOriginal == false) {
			
			// The calendar date could be read from the file, if the date of the last time the original
			// file was read would be relevant to the user of this class. (remove the related comment tags)
			ObjectInputStream in = null;
			//Date date;
			//Integer objectHash;
		    
			// Try reading in file
			try {
				
				// Open file and read objects
				in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(savePath)));
				
				/*date = new Date(*/in.readLong()/*)*/;
				/*objectHash = (Integer)*/in.readObject();
				phoneticMap = (HashMap<String, Float[]>)in.readObject();
				
				// Print out phonetic map to console (debugging)
				//printPhoneticMap(phoneticMap);
				
				// Return map is reading was successful
				return phoneticMap;
			
			} catch(ClassNotFoundException e) {
				System.err.printf("Could not find necessary objects in file: %s%n'%s'%n", savePath, e.getMessage());
			} catch(IOException e) {
				System.err.printf("Could not read phonetic mapping from file: %s%n'%s'%n", savePath, e.getMessage());
			} catch(ClassCastException e) {
				System.err.printf("Could not cast phonetic mapping from file: %s%n'%s'%n", savePath, e.getMessage());
			
			// Make sure that opened file is closed
			} finally {
				if(in != null) {
					try {
						in.close();
					} catch (IOException e) {
						System.err.printf("Could not close open file: %s%n'%s'%n", savePath, e.getMessage());
					}
				}
			}
		}
		
		//System.out.printf("Try to open: %s%n", featurePath);
		
		// Open file from local hard drive
		BufferedReader readIn = null;
		
		// Create regex pattern, append to be able to use the group feature
		StringBuilder regexPattern = new StringBuilder("\\s*(\\S{1,3})\\s*(:\\\\?)?\\s+");
		for(int i = 0; i < 9; i++) {
			regexPattern.append("((?:\\d{1,2}(?:\\.\\d)?)|_)(?:\\s+|$)");
		}

    	// Initialize HashMap to store values
    	phoneticMap = new HashMap<String, Float[]>();
		
        try {
        	
        	// Try to read file
    		readIn = new BufferedReader(new FileReader(featurePath));
    		
    		//System.out.println("File successfully opened.");
    		
    		// Compile pattern and prepare matcher
        	Pattern linePattern = Pattern.compile(regexPattern.toString());
    		Matcher matcher;
    		
    		// Initialize indicator variables and key/value pointers for resulting map
        	String line;
        	Float[] value;
        	String key = null;
        	
        	// Read every line of the text file
            while ((line = readIn.readLine()) != null) {
            	
            	// Check line for pattern
            	matcher = linePattern.matcher(line);
                
            	// Check if line fits the required pattern
                if (matcher.find()) {
                	
                    // Print out entire match (debugging)
                	//System.out.printf("Valid line: '%s'%n", matcher.group(0));
                    
                    // Create and store values of interest
                	value = new Float[9];
                    
                	// Run loop through all values of interest and set keys and values
                    for (int i = 0; i < 11; i++) { 
                    	
                    	//System.out.printf("Valid match (%d): '%s'%n", i+1, matcher.group(i+1));
                    	
                    	// First two matches are the keys, put them together to get single key
                    	// (array objects are not recognized by Collection Map if pointer is different)
                    	if(i == 0) {
                    		key = matcher.group(i+1);
                    	} else if(i == 1) {
                    		if (matcher.group(i+1) != null) key = key.concat(matcher.group(i+1));
                    		
                    	// Set key variable -1f for empty values
                    	} else if(matcher.group(i+1).equals("_")) {
                    		value[i-2] = null;
                    		
                    	// Store values in regular case
                    	} else {
                    		value[i-2] = Float.parseFloat(matcher.group(i+1));
                    	}
                    	
                    	// Print out what was found for particular group (debugging)
                    	//System.out.printf("@%d: '%s' | ", i+1, matcher.group(i+1));
                    	
                    }
                    
                    // Stores results in Map
                    phoneticMap.put(key, value);
                    
                    //System.out.printf("%n*******************%n");
                    
                }

            }
           
        // Catch possible exceptions, report error and return null
        } catch (FileNotFoundException e) { 
        	
        	System.err.printf("Could not find required document at: %s%n%s%n", featurePath, e.getMessage());
        	System.exit(-1);
        	
		} catch(IOException e) {
			
			System.err.printf("Could not read document: %s%n%s%n", featurePath, e.getMessage());
			System.exit(-1);
		
		// Make sure that file is closed afterwards
		} finally {
		
			if(readIn != null) {
				try { readIn.close(); } catch (IOException e) {
					System.err.printf("Unable to close open file: %s%n'%s'%n", featurePath, e.getMessage());
				}
			}
			
        }
		
		// Print out result to console (debugging)
		//printPhoneticMap(phoneticMap);
		
		// Try to save resulting object for phonetic map to hard drive
		ObjectOutputStream out = null;
	    
		try {
			out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(savePath)));
			
			out.writeLong(new Date().getTime());
			out.writeObject(phoneticMap.hashCode());
			out.writeObject(phoneticMap);
			
		} catch(IOException e) {
			System.err.printf("Could not write phonetic mapping to file: %s%n'%s'" +
					"This might cause major delays in the calculation, since map cannot be stored locally.%n", savePath, e.getMessage());
		
		// Make sure that opened file is closed
		} finally {
			if(out != null) {
				try {
					out.close();
				} catch (IOException e) {
					System.err.printf("Could not close open file: %s%n'%s'%n", savePath, e.getMessage());
				}
			}
		}
		
		// Return result of function
		return phoneticMap;
		
	}
	
	/**
	 * (Debugging:) This function tries to reread the phonetic information from original file.
	 * @return true if attempt to reread was successful, false otherwise (details on System.err)
	 */
	@SuppressWarnings("unused")
	private boolean forceRereadPhonetics() {
		Map<String, Float[]> phoneticMap = readPhonetics(true);
		return phoneticMap != null ? true : false;
	}
	
	/**
	 * (Debugging:) Function to print any Damerau-Levenshtein table to the console
	 * @param matrix The (filled) matrix to be printed
	 * @param firstSymbols The array symbols of the first word to include into the matrix
	 * @param secondSymbols The array of symbols of the second word to include into the matrix
	 */
	//@SuppressWarnings("unused") // Used only by another unused function
	private static void printMatrix(float[][] matrix, String[] firstSymbols, String[] secondSymbols) {
	
		System.out.printf("Printing distance matrix to console.%n" +
				"(* indicates the transformation path, # indicates swapping of the next characters)%n");
		
		// Alter Point array to set of ideal points
		Map<HashPoint,Integer> path = getIdealPath(matrix);
		
		for (int i = -1; i <= firstSymbols.length; i++) {
			for (int j = -1; j <= secondSymbols.length; j++) {
				
				if(j == -1 && i == -1) {
					System.out.print("            ");
				} else if(j == -1 && i == 0) {
					System.out.print("      |");
				} else if(i == -1 && j > 0) {
					System.out.printf("  %5s", secondSymbols[j-1]);
				}  else if(j == -1 && i > 0) {
					System.out.printf("%-5s |", firstSymbols[i-1]);
				} else if(i != -1 || j != 0) {
					
					// Check if point is part of the ideal coordinate set
					String pathMarker;
					HashPoint tempPoint = new HashPoint(j,i);
					
					if(path.containsKey(tempPoint)) {
						
						if(path.get(tempPoint).intValue() == ACTION_SWAP) {
							pathMarker = "#";
						} else {
							pathMarker = "*";
						}
						
					} else {
						pathMarker = " ";
					}
					
					System.out.printf("%5.1f%s|", matrix[i][j], pathMarker);
				}
				
			}
			
			System.out.println();
			
		}
		
		System.out.println("----------------------");
		System.out.printf("Costs of transformation: %.1f%n", matrix[firstSymbols.length][secondSymbols.length]);
		System.out.println("----------------------");
		
	}
	
	/**
	 * (Debugging:) Helper function that converts char arrays to String arrays to use the proper function to print matrix
	 * @param matrix The matrix to print to the console
	 * @param firstSymbols The char array of symbols for the first word
	 * @param secondSymbols The char array of symbols for the second word
	 */
	@SuppressWarnings("unused")
	private static void printMatrix(float[][] matrix, char[] firstSymbols, char[] secondSymbols) {
		
		String[] firstTempArray = new String[firstSymbols.length];
		String [] secondTempArray = new String[secondSymbols.length];
		
		for(int i = 0; i < firstSymbols.length; i++) {
			firstTempArray[i] = String.valueOf(firstSymbols[i]);
		}
		for(int i = 0; i < secondSymbols.length; i++) {
			secondTempArray[i] = String.valueOf(secondSymbols[i]);
		}
		
		printMatrix(matrix, firstTempArray, secondTempArray);
		
	}
	
	/**
	 * (Debugging:) Internal debugging function to print a map of matchings to the console
	 * @param map The map to be printed
	 * @param comparedResults The matrix with the results of comparing all the sequence elements
	 * @param wordsFirstInput The first collection word to include into the output
	 * @param wordsSecondInput The second collection of word to include into the output
	 */
	@SuppressWarnings("unused")
	private static void printMatchMap(Map<Integer,Integer> map, float[][] comparedResults, String[] wordsFirstInput, String[] wordsSecondInput) {
	
		String firstWord;
		String secondWord;
		
		SortedSet<Integer> sortedMap = new TreeSet<Integer>(map.keySet());
		
		System.out.printf("Print map with results in sorted order: (Words that are not in the list were not matched.)%n%n");
		
		for(int i : sortedMap) {
			
			// Set word Strings
			firstWord = "\"" + wordsFirstInput[i] + "\"";
			secondWord = "\"" + wordsSecondInput[map.get(i).intValue()] + "\"";
			float result = comparedResults[i][map.get(i).intValue()];
			
			System.out.printf("Word %d (%s) maps to word %d (%s) with an overall accuracy of %.2f%n", i, 
					firstWord, map.get(i), secondWord, result);

		}
		
	}

	/**
	 * (Debugging:) Internal debugging function to print a phonetic map to the console
	 * @param map The phonetic map to be printed
	 */
	@SuppressWarnings("unused")
	private static void printPhoneticMap(Map<String,Float[]> map) {
	
		System.out.println("Print map (in random order) / 'n' stands for null:");
		
		Float[] currentValue;
		
		for(String currentKey : map.keySet()) {

			currentValue = map.get(currentKey);
			System.out.printf("Letter '%s': ", currentKey);
			
			for(int i = 0; i < currentValue.length; i++) {
				System.out.printf("[%d] is %.1f | ", i, currentValue[i]);
			}
			
			System.out.println();
	
		}
		
	}
	
	/**
	 * Get the path to the original feature file for XSAMPA
	 * @return The path to the original feature file as a String 
	 */
	public String getFeaturePath() {
		return featurePath.getPath();
	}

	/**
	 * Set the path to the original feature file for XSAMPA
	 * @return A boolean indicating if the new file was read sucessfully
	 */
	public boolean setFeaturePath(String featurePath) {
		
		rwLock.writeLock().lock();
		this.featurePath = new File(featurePath);
		rwLock.writeLock().unlock();
		
		return readPhonetics(true) == null ? false : true;
	}
	
	/**
	 * Set the path to the original feature file for XSAMPA
	 * @return A boolean indicating if the new file was read sucessfully
	 */
	public boolean setFeaturePath(File featureFile) {
		
		rwLock.writeLock().lock();
		this.featurePath = featureFile;
		rwLock.writeLock().unlock();
		
		return readPhonetics(true) == null ? false : true;
	}
	
	/**
	 * Get the path to the rendered feature file for XSAMPA (need writing permission
	 * for this file)
	 * @return The path to the rendered feature file as a String 
	 */
	public String getSavePath() {
		return savePath.getPath();
	}
	
	/**
	 * Set the path to the original feature file for XSAMPA  (need writing permission
	 * for this file)
	 * @return The function will try to read in an existing feature map at the given location
	 * 		or will try write the new feature map to this location if none exists. 
	 * 		returns true if successful, turns false if not
	 */
	public boolean setSavePath(String savePath) {
		
		rwLock.writeLock().lock();
		this.savePath = new File(savePath);
		rwLock.writeLock().unlock();
		
		return readPhonetics(false) == null ? false : true;
	}
	
	/**
	 * Set the path to the original feature file for XSAMPA  (need writing permission
	 * for this file)
	 * @return The function will try to read in an existing feature map at the given location
	 * 		or will try write the new feature map to this location if none exists. 
	 * 		returns true if successful, turns false if not
	 */
	public boolean setSavePath(File savePath) {
		
		rwLock.writeLock().lock();
		this.savePath = savePath;
		rwLock.writeLock().unlock();
		
		return readPhonetics(false) == null ? false : true;
	}
	
	/**
	 * A method that returns the costs for a given operation
	 * @param costID The ID of the operation
	 * @return The costs
	 */
	public float getCost(int costID) {
		if(costID >= 0 && costID < operationCosts.length) {
			return operationCosts[costID];
		} else {
			throw new IllegalArgumentException("This ID does not exist");
		}
	}
	
	/**
	 * A method to set the costs for a given operation. CHAR_INDEL should be the cheapest operation
	 * while CHAR_SUBSTITUTION should be the most expensive. Otherwise the results could be very odd.
	 * This is only not true if an operation should be strictly avoided. This can be accomplished by setting
	 * the cost for an operation to Integer.MAX_VALUE.
	 * @param cost The costs to set for this operation
	 * @param costID The ID of the operation
	 */
	public void setCost(float cost, int costID) {
		if(costID >= 0 && costID < operationCosts.length) {
			
			rwLock.writeLock().lock();
			operationCosts[costID] = cost;
			rwLock.writeLock().unlock();
			
		} else {
			throw new IllegalArgumentException("This ID does not exist");
		}
	}

}