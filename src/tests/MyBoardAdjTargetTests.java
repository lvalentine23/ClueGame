package tests;


import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;

import clueGame.Board;
import clueGame.BoardCell;

public class MyBoardAdjTargetTests {

	// We make the Board static because we can load it one time and 
		// then do all the tests. 
		private static Board board;
		@BeforeClass
		public static void setUp() throws IOException {
			// Board is singleton, get the only instance
			board = Board.getInstance();
			// set the file names to use my config files
			board.setConfigFiles("CLUE_BOARD.csv", "ClueRooms.txt");		
			// Initialize will load BOTH config files 
			board.initialize();
		}
		

		// Ensure that player does not move around within room
		// These cells are ORANGE on the planning spreadsheet
		@Test
		public void testAdjacenciesInsideRooms()
		{
			// Test a corner
			Set<BoardCell> testList = board.getAdjList(0, 0);
			assertEquals(0, testList.size());
			// Test one that has walkway underneath
			testList = board.getAdjList(4, 5);
			assertEquals(0, testList.size());
			// Test one that has walkway above
			testList = board.getAdjList(18, 5);
			assertEquals(0, testList.size());
			// Test one that is in middle of room
			testList = board.getAdjList(21, 7);
			assertEquals(0, testList.size());
			// Test one beside a door
			testList = board.getAdjList(17, 14);
			assertEquals(0, testList.size());
			// Test one in a corner of room
			testList = board.getAdjList(4, 17);
			assertEquals(0, testList.size());
		}

		// Ensure that the adjacency list from a doorway is only the
		// walkway. NOTE: This test could be merged with door 
		// direction test. 
		// These tests are PURPLE on the planning spreadsheet
		@Test
		public void testAdjacencyRoomExit()
		{
			// TEST DOORWAY RIGHT 
			Set<BoardCell> testList = board.getAdjList(2, 5);
			assertEquals(1, testList.size());
			assertTrue(testList.contains(board.getCellAt(2, 6)));
			// TEST DOORWAY LEFT 
			testList = board.getAdjList(22, 12);
			assertEquals(1, testList.size());
			assertTrue(testList.contains(board.getCellAt(22, 11)));
			//TEST DOORWAY DOWN
			testList = board.getAdjList(13, 5);
			assertEquals(1, testList.size());
			assertTrue(testList.contains(board.getCellAt(14, 5)));
			//TEST DOORWAY UP
			testList = board.getAdjList(18, 3);
			assertEquals(1, testList.size());
			assertTrue(testList.contains(board.getCellAt(19, 3)));
			//TEST DOORWAY RIGHT, WHERE THERE'S A WALKWAY BELOW
			testList = board.getAdjList(20, 10);
			assertEquals(1, testList.size());
			assertTrue(testList.contains(board.getCellAt(20, 11)));
			
		}
		
		// Test adjacency at entrance to rooms
		// These tests are GREEN in planning spreadsheet
		@Test
		public void testAdjacencyDoorways()
		{
			// Test beside a door direction RIGHT
			Set<BoardCell> testList = board.getAdjList(4, 4);
			assertTrue(testList.contains(board.getCellAt(4, 3)));
			assertTrue(testList.contains(board.getCellAt(4, 5)));
			assertTrue(testList.contains(board.getCellAt(5, 4)));
			assertEquals(3, testList.size());
			// Test beside a door direction DOWN
			testList = board.getAdjList(6, 15);
			assertTrue(testList.contains(board.getCellAt(5, 15)));
			assertTrue(testList.contains(board.getCellAt(6, 14)));
			assertTrue(testList.contains(board.getCellAt(6, 16)));
			assertEquals(3, testList.size());
			// Test beside a door direction LEFT
			testList = board.getAdjList(15, 17);
			assertTrue(testList.contains(board.getCellAt(15, 16)));
			assertTrue(testList.contains(board.getCellAt(15, 18)));
			assertTrue(testList.contains(board.getCellAt(14, 17)));
			assertTrue(testList.contains(board.getCellAt(16, 17)));
			assertEquals(4, testList.size());
			// Test beside a door direction UP
			testList = board.getAdjList(13, 11);
			assertTrue(testList.contains(board.getCellAt(13, 10)));
			assertTrue(testList.contains(board.getCellAt(13, 12)));
			assertTrue(testList.contains(board.getCellAt(12, 11)));
			assertTrue(testList.contains(board.getCellAt(14, 11)));
			assertEquals(4, testList.size());
		}

		// Test a variety of walkway scenarios
		// These tests are LIGHT PURPLE on the planning spreadsheet
		@Test
		public void testAdjacencyWalkways()
		{
			// Test on top edge of board, just one walkway piece
			Set<BoardCell> testList = board.getAdjList(0, 4);
			assertTrue(testList.contains(board.getCellAt(0, 5)));
			assertEquals(1, testList.size());
			
			// Test on left edge of board, three walkway pieces
			testList = board.getAdjList(6, 0);
			assertTrue(testList.contains(board.getCellAt(5, 0)));
			assertTrue(testList.contains(board.getCellAt(6, 1)));
			assertTrue(testList.contains(board.getCellAt(7, 0)));
			assertEquals(3, testList.size());

			// Test between two rooms, walkways right and left
			testList = board.getAdjList(6, 21);
			assertTrue(testList.contains(board.getCellAt(6, 20)));
			assertTrue(testList.contains(board.getCellAt(6, 22)));
			assertEquals(2, testList.size());

			// Test surrounded by 4 walkways
			testList = board.getAdjList(15,7);
			assertTrue(testList.contains(board.getCellAt(15, 8)));
			assertTrue(testList.contains(board.getCellAt(15, 6)));
			assertTrue(testList.contains(board.getCellAt(14, 7)));
			assertTrue(testList.contains(board.getCellAt(16, 7)));
			assertEquals(4, testList.size());
			
			// Test on bottom edge of board, next to 1 room piece
			testList = board.getAdjList(21, 15);
			assertTrue(testList.contains(board.getCellAt(21, 16)));
			assertTrue(testList.contains(board.getCellAt(20, 15)));
			assertEquals(2, testList.size());
			
			// Test on right edge of board, next to 1 room piece
			testList = board.getAdjList(14, 22);
			assertTrue(testList.contains(board.getCellAt(14, 21)));
			assertTrue(testList.contains(board.getCellAt(13, 22)));
			assertEquals(2, testList.size());

			// Test on walkway next to  door that is not in the needed
			// direction to enter
			testList = board.getAdjList(5, 3);
			assertTrue(testList.contains(board.getCellAt(5, 2)));
			assertTrue(testList.contains(board.getCellAt(5, 4)));
			assertTrue(testList.contains(board.getCellAt(6, 3)));
			assertEquals(3, testList.size());
		}
		
		
		// Tests of just walkways, 1 step, includes on edge of board
		// and beside room
		// Have already tested adjacency lists on all four edges, will
		// only test two edges here
		// These are LIGHT BLUE on the planning spreadsheet
		@Test
		public void testTargetsOneStep() {
			board.calcTargets(21, 7, 1);
			Set<BoardCell> targets= board.getTargets();
			assertEquals(2, targets.size());
			assertTrue(targets.contains(board.getCellAt(20, 7)));
			assertTrue(targets.contains(board.getCellAt(21, 6)));	
			
			board.calcTargets(14, 0, 1);
			targets= board.getTargets();
			assertEquals(3, targets.size());
			assertTrue(targets.contains(board.getCellAt(14, 1)));
			assertTrue(targets.contains(board.getCellAt(13, 0)));	
			assertTrue(targets.contains(board.getCellAt(15, 0)));			
		}
		
		// Tests of just walkways, 2 steps
		// These are LIGHT BLUE on the planning spreadsheet
		@Test
		public void testTargetsTwoSteps() {
			board.calcTargets(21, 7, 2);
			Set<BoardCell> targets= board.getTargets();
			assertEquals(2, targets.size());
			assertTrue(targets.contains(board.getCellAt(19, 7)));
			assertTrue(targets.contains(board.getCellAt(20, 6)));
			
			board.calcTargets(14, 0, 2);
			targets= board.getTargets();
			assertEquals(3, targets.size());
			assertTrue(targets.contains(board.getCellAt(12, 0)));
			assertTrue(targets.contains(board.getCellAt(14, 2)));	
			assertTrue(targets.contains(board.getCellAt(15, 1)));			
		}
		
		// Tests of just walkways, 4 steps
		// These are LIGHT BLUE on the planning spreadsheet
		@Test
		public void testTargetsFourSteps() {
			board.calcTargets(21, 7, 4);
			Set<BoardCell> targets= board.getTargets();
			assertEquals(4, targets.size());
			assertTrue(targets.contains(board.getCellAt(17, 7)));
			assertTrue(targets.contains(board.getCellAt(19, 7)));
			assertTrue(targets.contains(board.getCellAt(18, 6)));
			assertTrue(targets.contains(board.getCellAt(20, 6)));
			
			// Includes a path that doesn't have enough length
			board.calcTargets(14, 0, 4);
			targets= board.getTargets();
			assertEquals(4, targets.size());
			assertTrue(targets.contains(board.getCellAt(14, 4)));
			assertTrue(targets.contains(board.getCellAt(15, 3)));	
			assertTrue(targets.contains(board.getCellAt(14, 2)));	
			assertTrue(targets.contains(board.getCellAt(15, 1)));	
		}	
		
		// Tests of just walkways plus one door, 6 steps
		// These are LIGHT BLUE on the planning spreadsheet
		//think i am counting correctly (doble check)
		@Test
		public void testTargetsSixSteps() {
			board.calcTargets(5, 0, 6);
			Set<BoardCell> targets= board.getTargets();
			assertEquals(5, targets.size());
			assertTrue(targets.contains(board.getCellAt(6, 5)));
			assertTrue(targets.contains(board.getCellAt(5, 6)));	
			assertTrue(targets.contains(board.getCellAt(6, 3)));	
			assertTrue(targets.contains(board.getCellAt(5, 4)));	
			assertTrue(targets.contains(board.getCellAt(4, 4)));
		}	
		
		// Test getting into a room
		// These are LIGHT BLUE on the planning spreadsheet

		@Test 
		public void testTargetsIntoRoom()
		{
			// One room is exactly 2 away
			board.calcTargets(11, 15, 2);
			Set<BoardCell> targets= board.getTargets();
			assertEquals(7, targets.size());
			// directly left (can't go right 2 steps)
			assertTrue(targets.contains(board.getCellAt(11, 17)));
			// directly up and down
			assertTrue(targets.contains(board.getCellAt(9, 15)));
			assertTrue(targets.contains(board.getCellAt(13, 15)));
			// one up/down, one left/right
			assertTrue(targets.contains(board.getCellAt(12, 16)));
			assertTrue(targets.contains(board.getCellAt(13, 16)));
			assertTrue(targets.contains(board.getCellAt(12, 14)));
			assertTrue(targets.contains(board.getCellAt(13, 14)));
		}
		
		// Test getting into room, doesn't require all steps
		// These are LIGHT BLUE on the planning spreadsheet
		@Test
		public void testTargetsIntoRoomShortcut() 
		{
			board.calcTargets(15, 3, 3);
			Set<BoardCell> targets= board.getTargets();
			//?
			assertEquals(12, targets.size());
			// directly up and down
			assertTrue(targets.contains(board.getCellAt(18, 3)));
			// directly right/left 
			assertTrue(targets.contains(board.getCellAt(15, 6)));
			assertTrue(targets.contains(board.getCellAt(15, 0)));
			// right then down
			assertTrue(targets.contains(board.getCellAt(17, 4)));
			assertTrue(targets.contains(board.getCellAt(16, 5)));
			// down then left
			assertTrue(targets.contains(board.getCellAt(16, 1)));
			// right then up
			assertTrue(targets.contains(board.getCellAt(13, 4)));
			// into the rooms
			assertTrue(targets.contains(board.getCellAt(13, 4)));
			assertTrue(targets.contains(board.getCellAt(18, 3)));		
					
			
		}

		// Test getting out of a room
		// These are LIGHT BLUE on the planning spreadsheet
		@Test
		public void testRoomExit()
		{
			// Take one step, essentially just the adj list
			board.calcTargets(1, 14, 1);
			Set<BoardCell> targets= board.getTargets();
			// Ensure doesn't exit through the wall
			assertEquals(1, targets.size());
			assertTrue(targets.contains(board.getCellAt(0, 14)));
			// Take two steps
			board.calcTargets(1, 14, 2);
			targets= board.getTargets();
			assertEquals(2, targets.size());
			assertTrue(targets.contains(board.getCellAt(0, 15)));
			assertTrue(targets.contains(board.getCellAt(0, 13)));
		}

}