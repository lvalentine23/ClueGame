package clueGame;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import clueGame.BoardCell;
import clueGame.BoardCell.DoorDirection;

public class Board {
	public final int MAX_BOARD_SIZE = 50;
	private static int numRows = 30;
	private static int numColumns = 30;
	private String boardConfigFile;
	private String roomConfigFile;
	private String playerConfigFile;
	private String weaponConfigFile;
	private boolean fullConfig = false;

	private Map<BoardCell, Set<BoardCell>> adjMatrix = new HashMap<>();
	private HashSet<BoardCell> targets = new HashSet<BoardCell>();
	private HashSet<BoardCell> visited = new HashSet<BoardCell>();
	private Map<Character, String> legend;
	public static BoardCell board[][] = new BoardCell[numRows][numColumns];

	private ArrayList<Player> players = new ArrayList<Player>();
	private ArrayList<Weapon> weapons = new ArrayList<Weapon>();
	private ArrayList<Room> rooms = new ArrayList<Room>();
	private ArrayList<Card> allCards = new ArrayList<Card>();
	//new
	private Solution theAnswer;
	private Card disproveCard;
	private boolean gameOver = false;


	// variable used for singleton pattern
	private static Board theInstance = new Board();

	// constructor is private to ensure only one copy
	private Board() {
	}

	// this method returns the only Board
	public static Board getInstance() {
		return theInstance;
	}

	public void initialize() {
		//I kept separate try catches so I could tell which file errored
		try {
			loadRoomConfig(roomConfigFile);
		} catch (IOException e) {
			System.out.println("Trouble loading roomConfigFile - txt");
		}
		try {
			loadBoardConfig(boardConfigFile);
		} catch (IOException e) {
			System.out.println("Trouble loading boardConfigFile - csv");
		}
		//if statement keeps old tests working
		if(fullConfig) {
			try {
				loadPlayerConfig(playerConfigFile);
			} catch (IOException e) {
				System.out.println("Trouble loading playerConfigFile");
			}
			try {
				loadWeaponConfig(weaponConfigFile);
			} catch (IOException e) {
				System.out.println("Trouble loading weaponConfigFile");
			}
		}

		calcAdjacencies();
		if(fullConfig) {
			buildDeck();
			dealCards();
		}
	}

	/*
	 * ***********************SET UP CARDS AND PLAYERS***********************
	 */

	public void buildDeck() {
		//make cards for all rooms

		for(Room room : rooms) {
			if(!room.getName().equals("Closet")) {
				Card card = new Card(CardType.ROOM, room.getName());
				allCards.add(card);
			}
		}
		//make cards for all players
		for(Player player: players) {
			Card card = new Card(CardType.PERSON, player.getName());
			allCards.add(card);
		}
		//make cards for each weapon
		for(Weapon weapon: weapons) {
			Card card = new Card(CardType.WEAPON, weapon.getName());
			allCards.add(card);
		}
	}

	public void dealCards() {
		selectAnswer();
		//give card at random index to players until all cards are used
		int cardsLeftToDeal = allCards.size()-3;
		int currentPlayer = players.size();
		//count down until all cards are dealt
		while(cardsLeftToDeal > 0) {
			Random rand = new Random();
			int nextIndex = rand.nextInt(allCards.size());
			if(allCards.get(nextIndex).isDealt()) {
				continue;
			} else {
				//only increase the index of the player being dealt if the card gets dealt
				currentPlayer = (currentPlayer+1) % players.size();
				players.get(currentPlayer).myCards.add(allCards.get(nextIndex));
				allCards.get(nextIndex).setStatus(true);
				cardsLeftToDeal -= 1;
			}
		}

	}

	public void selectAnswer() {
		//select a room (index 0 through numRooms)
		Random rand = new Random();
		int nextIndex1 = rand.nextInt(rooms.size()-1);
		int nextIndex2 = rand.nextInt(players.size());
		int nextIndex3 = rand.nextInt(weapons.size());
		theAnswer = new Solution(allCards.get(nextIndex2+rooms.size()).getName(), 
				allCards.get(nextIndex1).getName(), 
				allCards.get(nextIndex3+rooms.size()+players.size()).getName());
		allCards.get(nextIndex1).setStatus(true);
		//select a person (index numRooms+1 through numRooms + numPeople)
		allCards.get(nextIndex2+rooms.size()).setStatus(true);
		//select a weapon (index numRooms+numPeople +1 through numCards)
		//nextIndex = rand.nextInt(weapons.size());
		allCards.get(nextIndex3+rooms.size()+players.size()).setStatus(true);
	}

	public Card handleSuggestion() {
		return null;
	}

	public boolean checkAccusation(Solution accusation) {
		return false;
	}


	/*
	 * ***********************SET UP BOARD*****************************
	 */

	public void calcAdjacencies() {
		for (int row = 0; row < numRows; row++) {
			for (int col = 0; col < numColumns; col++) {
				HashSet<BoardCell> adjSet = new HashSet<BoardCell>();
				BoardCell testCell = new BoardCell();
				BoardCell startCell = new BoardCell();
				startCell = board[row][col];
				// up
				if (row > 0) {
					testCell = board[row - 1][col];
					if (adjHelp(testCell, startCell))
						adjSet.add(board[row - 1][col]);
				}
				// down
				if (numRows - 1 > row) {
					testCell = board[row + 1][col];
					if (adjHelp(testCell, startCell))
						adjSet.add(board[row + 1][col]);
				}
				// right
				if (col < numColumns - 1) {
					testCell = board[row][col + 1];
					if (adjHelp(testCell, startCell))
						adjSet.add(board[row][col + 1]);
				}
				// left
				if (col > 0) {
					testCell = board[row][col - 1];
					if (adjHelp(testCell, startCell))
						adjSet.add(board[row][col - 1]);
				}
				// push the adj list and corresponding cell to map
				adjMatrix.put(getCellAt(row, col), adjSet);
			}
		}
	}

	public HashSet<BoardCell> calcTargets(int row, int col, int dieRoll) {
		// create new hashset
		BoardCell startCell = getCellAt(row, col);
		BoardCell originalStart = new BoardCell();
		originalStart = getCellAt(row, col);
		HashSet<BoardCell> foundTargets = new HashSet<BoardCell>();
		// clear the old one
		targets.clear();
		foundTargets = findAllTargets(startCell, dieRoll, originalStart);
		// remove the startCell if it exists (illegal move)
		foundTargets.remove(startCell);
		return (foundTargets);
	}

	public HashSet<BoardCell> findAllTargets(BoardCell startCell, int stepsRemaining, BoardCell startingSquare) {
		// go through every adj cell
		for (BoardCell testCell : getAdjList(startCell.getRow(), startCell.getColumn())) {
			if (testCell.getRow() == startingSquare.getRow() && testCell.getColumn() == startingSquare.getColumn()) {
				visited.add(startingSquare);
				continue;
			}
			if (testCell.isDoorway() && isGoodDoor(testCell, startCell)) {
				targets.add(testCell);
				continue;
			}
			if (visited.contains(testCell)) {
				continue;
			}
			// add to visited
			visited.add(testCell);
			// if numsteps are 1 we have reached are target
			if (stepsRemaining == 1) {
				// System.out.println(b.getInitialFull());
				targets.add(testCell);
			}
			// else call recursive
			else if (!testCell.isDoorway()) {
				findAllTargets(testCell, stepsRemaining - 1, startingSquare);
			}
			visited.remove(testCell);
		}
		// return them targets
		return (targets);
	}

	/*
	 * *************************HELPER FUNCTIONS*************************************
	 */

	public boolean adjHelp(BoardCell testCell, BoardCell startCell) {
		// if the cell is not a doorway and it has two initials don't enter
		// if it is not an exit and don't leave the room
		// if you are on a walkway only enter if door
		// if it is a walkway you can keep walking on the walkway
		if ((!(testCell.isDoorway() && isGoodDoor(testCell, startCell)) && testCell.getInitials().length() == 2)
				|| ((!isExitSquare(testCell, startCell)) && startCell.getInitials().length() == 2)
				|| ((isSameRoom(testCell, startCell) && startCell.getFirstInitial() != 'W'))
				|| (startCell.getFirstInitial() == 'W' && testCell.getFirstInitial() != 'W'
				&& testCell.getInitials().length() != 2)
				|| (startCell.getFirstInitial() != 'W' && testCell.getFirstInitial() == 'W'
				&& testCell.getInitials().length() != 2 && startCell.getInitials().length() != 2)) {
			return false;
		}
		// otherwise you can not move
		else {
			return true;
		}
	}

	// checks if target is the right placement to exit based on starting origin
	// being a door and it's direction
	public boolean isExitSquare(BoardCell target, BoardCell origin) {
		// a walkway above a door with direction up
		if (origin.getRow() > target.getRow() && origin.getDoorDirection() == DoorDirection.UP) {
			return true;
		}
		// a walkway below a door with direction down
		else if (origin.getRow() < target.getRow() && origin.getDoorDirection() == DoorDirection.DOWN) {
			return true;
		}
		// a walkway to the left of a door with direction left
		else if (origin.getColumn() > target.getColumn() && origin.getDoorDirection() == DoorDirection.LEFT) {
			return true;
		}
		// a walkway to the right of a door with direction right
		else if (origin.getColumn() < target.getColumn() && origin.getDoorDirection() == DoorDirection.RIGHT) {
			return true;
		} else {
			return false;
		}
	}

	// checks door direction and adds to targets if it works
	public boolean isGoodDoor(BoardCell target, BoardCell origin) {
		// a cell under a door, and door direction is up
		if (origin.getRow() < target.getRow() && target.getDoorDirection() == DoorDirection.UP) {
			return true;
		}
		// walkway is above door and door direction is down
		else if (origin.getRow() > target.getRow() && target.getDoorDirection() == DoorDirection.DOWN) {
			return true;
		}
		// walkway is to the left of door and door direction is left
		else if (origin.getColumn() < target.getColumn() && target.getDoorDirection() == DoorDirection.LEFT) {
			return true;
		}
		// walkway is to the right of door and door direction is right
		else if (origin.getColumn() > target.getColumn() && target.getDoorDirection() == DoorDirection.RIGHT) {
			return true;
		} else {
			return false;
		}
	}

	// checks to see if the target has the same initial as the origin cell
	public boolean isSameRoom(BoardCell target, BoardCell origin) {
		if (origin.getFirstInitial() == target.getFirstInitial()) {
			return true;
		} else {
			return false;
		}
	}

	// checks if the symbol is a closet
	public boolean isCloset(BoardCell target) {
		if (target.getFirstInitial() == 'X') {
			return true;
		} else {
			return false;
		}
	}


	public static boolean isRoom(int row, int col) {
		if(getCellAt(row,col).getFirstInitial() != 'X' && getCellAt(row,col).getFirstInitial() != 'W') {
			return true;
		} else {
			return false;

		}
	}

	/*
	 * ********************CONFIGURATION METHODS********************************
	 */

	public void setCardConfigFiles(String txtPlayer, String txtWeapon) {
		fullConfig = true;
		playerConfigFile = txtPlayer;
		weaponConfigFile = txtWeapon;
	}
	public void setConfigFiles(String csv, String txt) {
		boardConfigFile = csv;
		roomConfigFile = txt;
	}

	public void loadPlayerConfig(String player_txt) throws IOException{
		String line = "";
		String name = "";
		String color = "";
		int row = 0;
		int col = 0;
		char status = ' ';
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(player_txt), "UTF-8"));
		while((line= br.readLine()) != null) {
			//player name
			name = line;
			//player color
			if((line = br.readLine()) == null) {
				System.out.println("File formatted wrong");
			} else {
				color = line;
			}
			//player start row
			if((line = br.readLine()) == null) {
				System.out.println("File formatted wrong");
			} else {
				row = Integer.parseInt(line);
			}
			//player start column
			if((line = br.readLine()) == null) {
				System.out.println("File formatted wrong");
			} else {
				col = Integer.parseInt(line);
			}
			//human or computer option:
			if((line = br.readLine()) == null) {
				System.out.println("File formatted wrong");
			} else {
				status = line.charAt(0);
			}
			//create that player object store in players array list
			if(status == 'C') {
				Player player = new ComputerPlayer(name,row,col,color);
				players.add(player);
			} else if(status == 'H') {
				Player player = new HumanPlayer(name,row,col,color);
				players.add(player);
			}
		}
		br.close();
	}

	public void loadWeaponConfig(String weapon_txt) throws IOException{
		String line = "";
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(weapon_txt), "UTF-8"));	
		while((line= br.readLine()) != null) {
			Weapon weapon = new Weapon(line);
			weapons.add(weapon);
		}
		br.close();
	}

	public void loadRoomConfig(String legend_txt) throws IOException {
		String line = "";
		String splitBy = ", ";
		String[] lines;
		legend = new HashMap<Character, String>();
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(legend_txt), "UTF-8"));
		while ((line = br.readLine()) != null) {
			lines = line.split(splitBy);
			char x = lines[0].charAt(0);
			legend.put(x, lines[1]);
			Room room = new Room(lines[0].charAt(0),lines[1], lines[2]);
			rooms.add(room);
		}
		br.close();
	}

	public void loadBoardConfig(String layout_csv) throws IOException {
		// set up file reader for csv
		BufferedReader in = new BufferedReader(new FileReader(layout_csv));
		String line;
		int row = 0;
		int col = 0;
		// used to make sure there are the same number of columns in each row
		int checked = 0;
		int cols_seen = 0;
		while ((line = in.readLine()) != null) {
			String[] initials = line.split(",");
			for (String i : initials) {
				board[row][col] = new BoardCell(row, col, i);
				col++;
			}
			if (col != cols_seen) {
				if (checked == 0) {
					cols_seen = col;
					checked += 1;
				} else {
					System.out.println("CSV file has columns of uneven length");
				}
			}
			col = 0;
			row++;
		}
		in.close();
		numRows = row;
		numColumns = cols_seen;
	}

	/*
	 ************************** GETTERS AND SETTERS *********************************
	 */

	public Set<BoardCell> getAdjList(int row, int col) {
		return adjMatrix.get(getCellAt(row, col));
	}

	public static BoardCell getCellAt(int row, int col) {
		// return the corresponding2D array
		return board[row][col];
	}

	public Set<BoardCell> getTargets() {
		return targets;
	}
	
	/*
	 * ***********************USED FOR TESTING***************************************
	 */

	public int getNumRows() {
		return numRows;
	}

	public int getNumColumns() {
		return numColumns;
	}

	public Map<Character, String> getLegend() {
		return legend;
	}

	public ArrayList<Player> getPlayers() {
		return players;
	}

	public ArrayList<Weapon> getWeapons() {
		return weapons;
	}
	public Solution getSolution() {
		return (theAnswer);
	}

	public ArrayList<Card> getCards() {
		return allCards;
	}

	public boolean cardExists(CardType type, String name) {
		for(Card card : allCards) {
			if(card.getType() == type && card.getName().equals(name)) {
				return true;
			}
		}
		return false;
	}

	public int cardCount(CardType type) {
		int counter = 0;
		for(Card card : allCards) {
			if(card.getType() == type) {
				counter += 1;
			}
		}
		return counter;
	}

	public boolean noDuplicates(Card card) {
		int counter = 0;
		for(Player player : players) {
			if(player.hasCard(card)) {
				counter += 1;
			}
		}
		if(counter > 1) {
			return false;
		} else return true;
	}

	public CardType getType(String name) {
		CardType type = CardType.NONE;
		for(Card card : allCards) {
			if(card.getName().equals(name)) {
				type = card.getType();
			}
		}
		return type;
	}

	public void setSolution(String person, String room, String weapon) {

	}

	public Object getDisproveCard() {
		return disproveCard;
	}

	public void setCards(String player, String cardName, CardType type) {

	}

	public void setAnswer(String person, String room, String weapon) {
		// TODO Auto-generated method stub

	}
}
