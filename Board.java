import java.util.Arrays;
import java.util.ArrayList;

public class Board extends TerminallyIll {

	private int width;
	private int height;
	private int displacement;
	public int minesCount;
	private boolean won = false;
	private boolean showMines = false;
	private boolean[][] mines;
	private boolean[][] viewed;
	private boolean[][] flags;
	public String offset = "";

	public Board(int newWidth, int newHeight, int newDisplacement) {
		this.width = newWidth;
		this.height = newHeight;
		this.displacement = newDisplacement;
		this.mines = new boolean[newHeight][newWidth];
		this.viewed = new boolean[newHeight][newWidth];
		this.flags = new boolean[newHeight][newWidth];
		this.minesCount = (int) (newWidth * newHeight * 0.15);
		for (int i = 0; i < 75 - newWidth / 2; i++)
			offset += " ";
	}

	//Generates mines in a way that makes sure to avoid the coors the player inputs in their first move
	public void generateMines(int avoidRow, int avoidCol) {
		int row, col;
		for (int i = 0; i < this.minesCount; ) {
			row = (int) (Math.random() * width);
			col = (int) (Math.random() * height);
			if (!this.mines[col][row] && col != avoidCol && row != avoidRow) {
				this.mines[col][row] = true;
				i++;
			}
		}
	}

	//Prints out a board with coors on side and top, numbers indicate nearby mines, @ means mine, ⚐ means flag, and X means misplaced flag
	public void displayBoard() {
		System.out.print(CLEAR_SCREEN);
		           
		for (int i = 0; i < this.width; i++) //for some reason the board doesn't properly allign top and bottom for the diff modes so we need to manually compensate for them all
                        System.out.print(TerminallyIll.goDisplacement(12, 76 + i, this.displacement) + (int) (i / 10));
		
		System.out.print("\n     " + offset);
		for (int i = 0; i < this.width; i++)
			System.out.print(i % 10);
                
		System.out.print("\n" + offset + "   ╔");
		for (int i = 0; i < this.width + 1; i++)
			System.out.print("═");
		
		System.out.println();
		for (int row = 0; row < this.height; row++) {
			System.out.print(offset);
			if (row < 10) {
				System.out.print(0);
			}
			System.out.print(row + " ║ ");
			for (int col = 0; col < this.width; col++) {
				color(RED, background(WHITE));
				int surroundingCount = this.getSurroundingCount(row, col);

				if (this.flags[row][col]) {
                                        if (showMines && !this.mines[row][col]){ //When the game ends, we show X's on the flags that were placed incorrectly
                                                System.out.print(color(RED, background(BLACK)) + "X");
                                        } else {
                                        System.out.print(color(RED, background(BLACK)) + "⚐\u001b[0m");
                                        }

				} else if (this.viewed[row][col]) {

					if (surroundingCount == 0){
						System.out.print(color(WHITE, background(BLACK)) + " " + RESET);
					} else {
						System.out.print(TerminallyIll.getColor(surroundingCount));
						System.out.print(surroundingCount);
						System.out.print("\u001b[0m");
					}

				} else if (this.mines[row][col] && showMines) {
					System.out.print(color(WHITE, background(BLACK)) + "@" + RESET);

				} else {
					System.out.print(color(WHITE, background(BLACK)) + "-" + RESET);

				}
			}
			System.out.println(color(WHITE, background(BLACK)));
		}
		System.out.println();
	}

	//searches all the tiles in a 1-tile radius and counts the mines and returns that
	public int getSurroundingCount(int row, int col) {
		int count = 0;
		int minX = col - 1;
		int maxX = col + 1;
		int minY = row - 1;
		int maxY = row + 1;

		if (minY == -1) { minY = 0; }
		if (minX == -1) { minX = 0; }
		if (maxX == this.width) { maxX--; }
		if (maxY == this.height) { maxY--; }

		for (int dx = minX; dx <= maxX; dx++) {
			for (int dy = minY; dy <= maxY; dy++) {
				if (this.mines[dy][dx]) {
					count++;
				}
			}
		}
		return count;
	}

	//checks if the tile selected is on a mine or not if you opened it and flags tiles
	//returns true or false indicating that the game is over
	//also checks if you've won the game
	public boolean selectTile(int row, int col) {
		if (row > this.height || col > this.width){
	    		return true;
	    }
		if (flags[row][col]){
			return true;
            }
		if (mines[row][col]){
			showMines();
			displayBoard();
    			return false;
            }
		ArrayList<int[]> seen = new ArrayList<int[]>();
		tileFillHelper(row, col, seen);
    
		if (countViewed() >= (this.width * this.height) - this.minesCount){
			this.won = true;
            		return false;
    		}
    		return true;

	}

	//recursively flood fills
	public void tileFillHelper(int row, int col, ArrayList<int[]> seen) {
		if (row < 0 || row >= this.height || col < 0 || col >= this.width)
			return;

		if (getSurroundingCount(row, col) > 0) {
			this.viewed[row][col] = true;
			return;
		}

		if (!pairInArray(row, col, seen)) { // if we haven't seen this pair
	                int[] toAdd = new int[2];
        	        toAdd[0] = row; toAdd[1] = col;
                	seen.add(toAdd);

			this.viewed[row][col] = true;
			for (int dx = row - 1; dx <= row + 1; dx++) {
				for (int dy = col - 1; dy <= col + 1; dy++) {
					tileFillHelper(dx, dy, seen);
				}
			}
		}
	}

	//checks if we've alr accounted for this or not (in tileFillHelper)
	public boolean pairInArray(int first, int second, ArrayList<int[]> array) {
		for (int[] list : array) {
			if (list[0] == first && list[1] == second) {
				return true;
			}
		}
		return false;
	}

	//flags or unflags a tile depending on what's already been done
	public void flag(int row, int col) {
		if (!viewed[row][col])
			this.flags[row][col] = !this.flags[row][col];
	}
	
	
	public int countViewed() {
		int count = 0;
		for (boolean[] row : this.viewed) {
			for (boolean tile : row) {
				if (tile) { count++; }
			}
		}
		return count;
	}

	public boolean hasWon() { return this.won; }
	
	public boolean isAMine(int row, int col) {
		return this.mines[row][col];
	}

	public void showMines() { this.showMines = true; }

}
