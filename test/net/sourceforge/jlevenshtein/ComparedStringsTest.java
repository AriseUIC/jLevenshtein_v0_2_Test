package net.sourceforge.jlevenshtein;

import static org.junit.Assert.*;

import org.junit.Assert;
import org.junit.Test;
import net.sourceforge.jlevenshtein.Levenshtein;



public class ComparedStringsTest {

	@Test
	public void distance_test () {

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
	}
	
	@Test
	public void getSecond_test () {

		try {
			Levenshtein ls = new Levenshtein();
			String firstString = "The quill is mightier than the sword";
			String secondString = "The nerd's keyboard is mightier than the quill";
			ComparedStrings cs = ls.compareDetailed(firstString, secondString);
			String result = cs.getSecond(2);
			Assert.assertEquals("keyboard", cs.getSecond(2));
		}
		
		catch(Exception ex)
		{
			fail("Unexpected"+ex.getStackTrace());
		}

}
	
	@Test
	public void matchSecond_test() {
		try {
			Levenshtein ls = new Levenshtein();
			String firstString = "The quill is mightier than the sword";
			String secondString = "The nerd's keyboard is mightier than the quill";
			ComparedStrings cs = ls.compareDetailed(firstString, secondString);
			int result = cs.matchSecond(2);
			float result2 = cs.index(cs.matchSecond(2), 2);
			double delta=0.009;
			Assert.assertEquals(6,result,delta);
			Assert.assertEquals(0.38,result2,delta);
			Assert.assertEquals("sword", cs.getFirst(cs.matchSecond(2)));
			
			}
		catch (Exception ex) {
			fail("Unexpected"+ex.getStackTrace());
		}
	}
	
	@Test
	public void compareDetailed_test() {
		
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
	public void index_test() {
		
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
	
	@Test
	public void singleWord_distance_test() {
		try
		{
		Levenshtein ls = new Levenshtein();
		
		String firstString = "kitten";
		String secondString = "sitting";
		ComparedStrings cs = ls.compareDetailed(firstString, secondString);
		float result = cs.distance();
		double delta=0.009;
		Assert.assertEquals(5,result,delta);
		
	}
		catch(IndexOutOfBoundsException ex) {
			
			fail("Unexpected"+ex.getStackTrace());
		}
				
	}
	
	@Test
	public void singleWord_distance_test2() {
		try
		{
		Levenshtein ls = new Levenshtein();
		
		String firstString = "kitten";
		String secondString = "sitting";
		ComparedStrings cs = ls.compareDetailed(firstString, secondString);
		float result = cs.distance();
		double delta=0.009;
		Assert.assertEquals(5,result,delta);
		
	}
		catch(IndexOutOfBoundsException ex) {
			
			fail("Unexpected"+ex.getStackTrace());
		}
				
	}

	@Test
	public void singleWord_distance_test3() {
		try
		{
		Levenshtein ls = new Levenshtein();
		
		String firstString = "kitten";
		String secondString = "sitting";
		ComparedStrings cs = ls.compareDetailed(firstString, secondString);
		float result = cs.index();
		double delta=0.009;
		Assert.assertEquals(0.61,result,delta);
		
	}
		catch(IndexOutOfBoundsException ex) {
			
			fail("Unexpected"+ex.getStackTrace());
		}
				
	}//
	
	@Test
	public void singleWord_distance_test4() {
		try
		{
		Levenshtein ls = new Levenshtein();
		
		String firstString = "kitten";
		String secondString = "sitting";
		ComparedStrings cs = ls.compareDetailed(firstString, secondString);
		float result = cs.matchFirst(0);
		double delta=0.009;
		Assert.assertEquals(-1,result,delta);
		
	}
		catch(IndexOutOfBoundsException ex) {
			
			fail("Unexpected"+ex.getStackTrace());
		}
				
	}
	
	@Test
	public void singleWord_distance_test5() {
		try
		{
		Levenshtein ls = new Levenshtein();
		
		String firstString = "A good man is hard to find.";
		String secondString = "A house is not a home.";
		ComparedStrings cs = ls.compareDetailed(firstString, secondString);
		float result = cs.index(1,cs.matchFirst(1));
		double delta=0.009;
		Assert.assertEquals(0.22,result,delta);
		
	}
		catch(IndexOutOfBoundsException ex) {
			
			fail("Unexpected"+ex.getStackTrace());
		}
				
	}
	
	@Test
	public void distance_two_arguement_test() {
		try
		{
		Levenshtein ls = new Levenshtein();
		
		String firstString = "A good man is hard to find.";
		String secondString = "A house is not a home.";
		ComparedStrings cs = ls.compareDetailed(firstString, secondString);
		float result = cs.distance(1,cs.matchFirst(1));
		double delta=0.009;
		Assert.assertEquals(7.0,result,delta);
		
	}
		catch(IndexOutOfBoundsException ex) {
			
			fail("Unexpected"+ex.getStackTrace());
		}
				
	}
	
	@Test
	public void maxIDFirst_test() {
		try
		{
		Levenshtein ls = new Levenshtein();
		
		String firstString = "A good man is hard to find.";
		String secondString = "A house is not a home.";
		ComparedStrings cs = ls.compareDetailed(firstString, secondString);
		int result = cs.maxIDFirst();
		double delta=0.009;
		Assert.assertEquals(6,result,delta);
		
	}
		catch(IndexOutOfBoundsException ex) {
			
			fail("Unexpected"+ex.getStackTrace());
		}
				
	}
	
	@Test
	public void maxIDSecond_test() {
		try
		{
		Levenshtein ls = new Levenshtein();
		
		String firstString = "A good man is hard to find.";
		String secondString = "A house is not a home.";
		ComparedStrings cs = ls.compareDetailed(firstString, secondString);
		int result = cs.maxIDSecond();
		double delta=0.009;
		Assert.assertEquals(5,result,delta);
		
	}
		catch(IndexOutOfBoundsException ex) {
			
			fail("Unexpected"+ex.getStackTrace());
		}
				
	}
	
}