package net.sourceforge.jlevenshtein;

import static org.junit.Assert.*;

import org.junit.Assert;
import org.junit.Test;
import net.sourceforge.jlevenshtein.Levenshtein;



public class ComparedStringsTest {

	@Test
	public void distanceTest () {

		try {
			Levenshtein ls = new Levenshtein();
			String firstString = "The quill is mightier than the sword";
			String secondString = "The nerd's keyboard is mightier than the quill";
			ComparedStrings cs = ls.compareDetailed(firstString, secondString);
			float result = cs.distance();
			double delta=0.009;
			Assert.assertEquals(62.54,result,delta);
		}
		
		catch(Exception ex)
		{
			fail("Unexpected"+ex.getStackTrace());
		}
		/*System.out.printf("The 3rd identified (and possibly transformed) word of string two is: '%s'%n", cs.getSecond(2));
		System.out.printf("This word was matched to the %dth word of string one: '%s' (overall accuracy: %.2f)%n", 
				cs.matchSecond(2) + 1, cs.getFirst(cs.matchSecond(2)), cs.index(cs.matchSecond(2), 2));
		
		System.out.printf("The 2nd identified (and possibly transformed) word of string one is: '%s'%n", cs.getFirst(1));
		System.out.printf("This word was matched to the %dth word of string two: '%s' (overall accuracy: %.2f)%n", 
				cs.matchFirst(1) + 1, cs.getSecond(cs.matchFirst(1)), cs.index(1, cs.matchFirst(1)));
		*/

	}
	
	@Test
	public void getSecondTest () {

		try {
			Levenshtein ls = new Levenshtein();
			String firstString = "The quill is mightier than the sword";
			String secondString = "The nerd's keyboard is mightier than the quill";
			ComparedStrings cs = ls.compareDetailed(firstString, secondString);
			String result = cs.getSecond(2);
			System.out.println(result);
			Assert.assertEquals("keyboard", cs.getSecond(2));//(62.54,result,delta);
		}
		
		catch(Exception ex)
		{
			fail("Unexpected"+ex.getStackTrace());
		}
		/*System.out.printf("The 3rd identified (and possibly transformed) word of string two is: '%s'%n", cs.getSecond(2));
		System.out.printf("This word was matched to the %dth word of string one: '%s' (overall accuracy: %.2f)%n", 
				cs.matchSecond(2) + 1, cs.getFirst(cs.matchSecond(2)), cs.index(cs.matchSecond(2), 2));
		
		System.out.printf("The 2nd identified (and possibly transformed) word of string one is: '%s'%n", cs.getFirst(1));
		System.out.printf("This word was matched to the %dth word of string two: '%s' (overall accuracy: %.2f)%n", 
				cs.matchFirst(1) + 1, cs.getSecond(cs.matchFirst(1)), cs.index(1, cs.matchFirst(1)));
		*/

	}

	@Test
	public void compareDetailedTest() {
		
		try {
		Levenshtein ls = new Levenshtein();
		String firstString = "The quill is mightier than the sword";
		String secondString = "The nerd's keyboard is mightier than the quill";
		
		ComparedStrings cs = ls.compareDetailed(firstString, secondString);
		Assert.assertEquals("The quill is mightier than the sword", cs.getFirst());
		Assert.assertEquals("The nerd's keyboard is mightier than the quill", cs.getSecond());		
		}
		
		catch(Exception ex)
		{
			fail("Unexpected"+ex.getStackTrace());
		}
	}

	@Test
	public void indexTest() {
		
		try {
		Levenshtein ls = new Levenshtein();
		String firstString = "The quill is mightier than the sword";
		String secondString = "The nerd's keyboard is mightier than the quill";
		
		ComparedStrings cs = ls.compareDetailed(firstString, secondString);
		float result = cs.index();
		double delta=0.009;
		Assert.assertEquals(0.77,result,delta);
		}
		
		catch(Exception ex)
		{
			fail("Unexpected"+ex.getStackTrace());
		}
	}

}