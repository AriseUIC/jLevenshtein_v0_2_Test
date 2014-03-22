package net.sourceforge.jlevenshtein;

import static org.junit.Assert.*;

import org.junit.Assert;
import org.junit.Test;

public class LevenshteinTest {

	@Test
	public void compare_Nullpointer_test() {
		try
		{
		Levenshtein ls = new Levenshtein();
		
		String firstString = null;
		String secondString = "A börd in zhä händ is wörth tuh in thä busch";
		float answer=ls.compare(firstString, secondString);
		double delta=0.009;
		Assert.assertEquals(0.85, answer,delta);
		
		}
		catch(NullPointerException ex) {
			Boolean condition=ex instanceof NullPointerException;
			Assert.assertTrue(condition);
		}
				
	}
	
	@Test
	public void compare_Phonetic_test() {
		try
		{
		Levenshtein ls = new Levenshtein();
		
		String firstString = "A bird in the hand is worth two in the bush";
		String secondString = "A b�rd in zhä h�xind is wörth tuh in thä busch";
		
		float answer=ls.compare(firstString, secondString);
		
		//double delta=0.009;
		//Assert.assertEquals(0.85, answer,delta);
		
		}
		catch(PhoneticException ex)
		{
			Boolean condition=ex instanceof PhoneticException;
			Assert.assertTrue(condition);
		}
				
	}
	
	@SuppressWarnings("deprecation")
	@Test
	public void compare_CopyConst_test() {
		try
		{
		Levenshtein ls1 = new Levenshtein();
		String firstString = "A bird in the hand is worth two in the bush";
		String secondString = "A börd@ in zhä händ is wörth tuh in thä busch";
		double answer1=ls1.compare(firstString, secondString);
		Levenshtein ls2 = new Levenshtein(ls1);
		//double answer2=ls2.compare(firstString, secondString);
		Assert.assertFalse(ls1.equals(ls2));
		}
		catch(Exception ex)
		{
			fail("Unexpected"+ex.getStackTrace());
		}
		/*catch(PhoneticException ex)
		{
			Boolean condition=ex instanceof PhoneticException;
			Assert.assertTrue(condition);
		}*/
				
	}
	
	@Test
	public void compare_Sample2_test() {
		try
		{
		Levenshtein ls = new Levenshtein();
		
		// First example
		String firstString = "A bird in the hand is worth two in the bush";
		String secondString = "A börd in zhä händ is wörth tuh in thä busch";
		
		float answer=ls.compare(firstString, secondString);
		
		double delta=0.009;
		Assert.assertEquals(0.85, answer,delta);
		
		}
		catch(Exception ex)
		{
			fail("Unexpected"+ex.getStackTrace());
		}
				
	}
	
	@Test
	public void compare_Sample3_test() {
		try
		{
		Levenshtein ls = new Levenshtein();	
		
		String firstString = "A bird in the hand is worth two in the bush";
		String secondString = "bird a in hand the worth is in the bush two";
		
		float answer=ls.compare(firstString, secondString);
		
		double delta=0.009;
		Assert.assertEquals(0.94, answer,delta);
		
		}
		catch(Exception ex)
		{
			fail("Unexpected"+ex.getStackTrace());
		}
				
	}
	
	@Test
	public void levFlag_test() {
		try
		{
			String firstString = "A bird in the hand is worth two in the bush";
			String secondString = "A börd in zhä händ is wörth tuh in thä busch";
			
			Levenshtein ls = new Levenshtein();
			ls.setFlag(LevFlag.ABSOLUTE);
			ls.compare(firstString, secondString);
			float answer=ls.compare(firstString, secondString);
		
			double delta=0.009;
			Assert.assertEquals(70.0, answer,delta);
			//------------------------------------
			ls.setFlag(LevFlag.RELATIVE);
			secondString = "bird a in hand the worth is in the bush two";
			answer=ls.compare(firstString, secondString);
			delta=0.009;
			Assert.assertEquals(0.94, answer,delta);
			
			// With one-word flag
			ls.setFlag(LevFlag.TREAT_AS_ONE_WORD); 
			answer=ls.compare(firstString, secondString);
			delta=0.009;
			Assert.assertEquals(0.74, answer,delta);
			
			ls.setFlag(LevFlag.TREAT_AS_SENTANCE);
			secondString = "BIrD A iN HaNd tHE wORTh iS iN tHE buSH tWo";
			answer=ls.compare(firstString, secondString);
			delta=0.009;
			Assert.assertEquals(0.66, answer,delta);
			
			ls.setFlag(LevFlag.IGNORE_CASE);
			answer=ls.compare(firstString, secondString);
			delta=0.009;
			Assert.assertEquals(0.96, answer,delta);
			
			ls.setFlag(LevFlag.USE_CASE);
			answer=ls.compare(firstString, secondString);
			delta=0.009;
			Assert.assertEquals(0.66, answer,delta);
			
			// First example: Swapping of characters
			secondString = "A iddr ni eth hnad si owtrh tow ni teh ubsh";
			answer=ls.compare(firstString, secondString);
			delta=0.009;
			Assert.assertEquals(0.84, answer,delta);
			
			ls.setFlag(LevFlag.DISALLOW_SWAP);
			
			answer=ls.compare(firstString, secondString);
			delta=0.009;
			Assert.assertEquals(0.78, answer,delta);
			
			ls.setFlag(LevFlag.ALLOW_SWAP);
			
			// First example: Different match of same length
			secondString = "It's not over until the fat lady sings";
			answer=ls.compare(firstString, secondString);
			delta=0.009;
			Assert.assertEquals(0.47, answer,delta);
			
			// First example: Few words
			secondString = "Duh!";
			
			answer=ls.compare(firstString, secondString);
			delta=0.009;
			Assert.assertEquals(0.05, answer,delta);
			
			// First example: Special characters
			secondString = "A bird! in %the% han&&d (is worth) two in �the bu$h";
			ls.setFlag(LevFlag.KEEP_SPECIAL);
			answer=ls.compare(firstString, secondString);
			delta=0.009;
			Assert.assertEquals(0.88, answer,delta);
			
			ls.setFlag(LevFlag.STRIP_SPECIAL);
			answer=ls.compare(firstString, secondString);
			delta=0.009;
			Assert.assertEquals(0.99, answer,delta);
			
			// Second example: XSAMPA
			ls.setFlag(LevFlag.PHONETIC);
			
			firstString = "gudmO:nIN mAn";
			secondString = "g@mOn@n ma:\\n";
			
			answer=ls.compare(firstString, secondString);
			delta=0.009;
			Assert.assertEquals(0.95, answer,delta);
			
			ls.setFlag(LevFlag.PLAIN);
			
			// Third example: Using the more detailed version of the function
			firstString = "The quill is mightier than the sword";
			secondString = "The nerd's keyboard is mightier than the quill";
			answer=ls.compare(firstString, secondString);
			delta=0.009;
			Assert.assertEquals(0.77, answer,delta);
		}
		
		catch(Exception ex)
		{
			fail("Unexpected"+ex.getStackTrace());
		}
	}
	
	@Test
	public void levFlag_test2() {
		//Identical input
		try
		{
			String firstString = "A bird in the hand is worth two in the bush";
			String secondString = "A bird in the hand is worth two in the bush";
			
			Levenshtein ls = new Levenshtein();
			ls.setFlag(LevFlag.ABSOLUTE);
			ls.compare(firstString, secondString);
			float answer=ls.compare(firstString, secondString);
		
			double delta=0.009;
			Assert.assertEquals(0.0, answer,delta);
			
		}
		
		catch(Exception ex)
		{
			fail("Unexpected"+ex.getStackTrace());
		}
	}

	@Test
	public void levFlag_test3() {
		//Identical input
		try
		{
			String firstString = "A bird in the hand is worth two in the bush";
			String secondString = null;
			
			Levenshtein ls = new Levenshtein();
			ls.setFlag(LevFlag.ABSOLUTE);
			
			float answer=ls.compare(firstString, secondString);
		
			
		}
		
		catch(NullPointerException ex) {
			Boolean condition=ex instanceof NullPointerException;
			Assert.assertTrue(condition);
		}
	}
	
	@Test
	public void levFlag_test4() {
		//Identical input
		try
		{
			String firstString = "kitten";
			String secondString = "sitting";
			
			Levenshtein ls = new Levenshtein();
			ls.setFlag(LevFlag.ABSOLUTE);
			ls.compare(firstString, secondString);
			float answer=ls.compare(firstString, secondString);
		
			double delta=0.009;
			Assert.assertEquals(5, answer,delta);
			
		}
		
		catch(Exception ex)
		{
			fail("Unexpected"+ex.getStackTrace());
		}
	}
	
	@Test
	public void levFlag_test5() {
		//Identical input
		try
		{
			String firstString = "kitten";
			String secondString = "sitting";
			
			Levenshtein ls = new Levenshtein();
			ls.setFlag(LevFlag.ABSOLUTE);
			ls.compare(firstString, secondString);
			float answer=ls.compare(firstString, secondString);
		
			double delta=0.009;
			Assert.assertEquals(5, answer,delta);
			
		}
		
		catch(ArrayIndexOutOfBoundsException ex)
		{
			Boolean condition=ex instanceof ArrayIndexOutOfBoundsException;
			Assert.assertTrue(condition);
		}
	}
	
	@Test
	public void cost_test() {
		//Identical input
		try
		{
			String firstString = "kitten";
			String secondString = "sitting";
			
			Levenshtein ls = new Levenshtein();
			ls.setFlag(LevFlag.ABSOLUTE);
			//ls.compare(firstString, secondString);
			ls.setCost(1, 0);
			float answer=ls.getCost(0);
			
			double delta=0.009;
			Assert.assertEquals(1, answer,delta);
			
		}
		
		catch(ArrayIndexOutOfBoundsException ex)
		{
			Boolean condition=ex instanceof ArrayIndexOutOfBoundsException;
			Assert.assertTrue(condition);
		}
	}

}