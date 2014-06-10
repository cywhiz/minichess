import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;

public class Chess extends JFrame implements ActionListener {
	// Global variables
	static char[] board = {'b','p','k','*','*','*','K','P','B'};
	static char turn = 'w';
	static String moves = "";
	static int maxA = 0;
	static int maxB = 0;
	
	// GUI STUFF ================================================================================
	JPanel pnlGrid = new JPanel(new GridLayout(3,3));
	JPanel pnlFlow = new JPanel(new FlowLayout());
	JTextField txtInput = new JTextField(3);
	JLabel lblStatus = new JLabel();
	JLabel lblTurn = new JLabel("White's turn.");
	JLabel lblCheck = new JLabel();
	JLabel lblWin = new JLabel();
	JLabel lblMoves = new JLabel();
	JButton btnMove = new JButton("MOVE");
	JButton btnReset = new JButton("Reset Game");
	JButton btnSquare[] = new JButton[9];
	Font f = new Font("DIALOG", Font.PLAIN, 50);

	public Chess() {
		for (int i=0; i<=8; i++) {
			btnSquare[i] = new JButton();
			btnSquare[i].setFont(f);
			
			if (i % 2 == 0) {
				btnSquare[i].setBackground(Color.ORANGE);
			}
			else {
				btnSquare[i].setBackground(Color.WHITE);
			}

			btnSquare[i].addActionListener(this);
			pnlGrid.add(btnSquare[i]);
		}

		pnlFlow.add(pnlGrid);
		pnlFlow.add(txtInput);
		pnlFlow.add(btnMove);
		pnlFlow.add(lblTurn);
		pnlFlow.add(btnReset);
		pnlFlow.add(lblStatus);
		pnlFlow.add(lblCheck);
		pnlFlow.add(lblWin);
		pnlFlow.add(lblMoves);

		btnMove.addActionListener(this);
		btnReset.addActionListener(this);
		
		lblStatus.setForeground(Color.RED);
		lblWin.setForeground(Color.BLUE);

		setTitle("MiniChess");
		setResizable(false);
		setSize(300, 400);
		setContentPane(pnlFlow);
		setVisible(true);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		// Display legal moves if they exist
		if (validateMoveExists(turn)) {
			lblMoves.setText("Legal moves = " + moves);
		}
		else {
			lblMoves.setText("NO legal moves");
		}
	}

	public void display() {
		// Display Unicode chess characters
		for (int i=0; i<=8; i++) {
			switch (board[i]) {
				case 'b': btnSquare[i].setText("\u265d"); break;
				case 'p': btnSquare[i].setText("\u265f"); break;
				case 'k': btnSquare[i].setText("\u265a"); break;
				case 'B': btnSquare[i].setText("\u2657"); break;
				case 'P': btnSquare[i].setText("\u2659"); break;
				case 'K': btnSquare[i].setText("\u2654"); break;
				default: btnSquare[i].setText(""); break;
			}
		}
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == btnMove) {
			String temp = txtInput.getText();
			int a, b;
			
			if (!temp.equals("") && (temp.length() == 2)) {
				// Parse player's move input
				try {			
					a = Character.getNumericValue(temp.charAt(0));
					b = Character.getNumericValue(temp.charAt(1));
					
					// Don't do move if game is over
					if  (!isGameOver()) {
						doMove(a, b, turn);		// Player's turn
						doAI();					// Computer's turn
						display();						
					}
				}
				catch (NumberFormatException e1) {
				}
			}
			
			txtInput.setText("");
			
			// Find legal moves
			if (validateMoveExists(turn)) {
				lblMoves.setText("Legal moves = " + moves);
			}
			else {
				lblMoves.setText("NO legal moves");
			}

			// Display message if either king is in check
			if (isCheck('w')) {
				lblCheck.setText("White is in check");
			}
			else if (isCheck('b')) {
				lblCheck.setText("Black is in check");
			}
			else {
				lblCheck.setText("");
			}
			
			// Display message if game is over
			if  (isGameOver()) {
				lblMoves.setText("NO legal moves");
				lblStatus.setText("");
			}
		}
		else if (e.getSource() == btnReset) {
			// Reset the game board
			board = new char[] {'b','p','k','*','*','*','K','P','B'};
			turn = 'w';
			txtInput.setText("");
			lblStatus.setText("");
			lblTurn.setText("White's turn.");
			lblCheck.setText("");
			lblWin.setText("");
			lblMoves.setText("");
			
			if (validateMoveExists(turn)) {
				lblMoves.setText("Legal moves = " + moves);
			}
			else {
				lblMoves.setText("NO legal moves");
			}
			
			display();			
		}
		else {
			// Enter move in the box when player click on the chess pieces
			for (int i=0; i<=8; i++) {
				if (e.getSource() == btnSquare[i]) {
					String output = txtInput.getText() + i;
					txtInput.setText(output);
					display();
				}
			}
		}
	}

	// MINIMAX AI ================================================================================
	
	public void doAI() {
		// Make a copy of the game board before performing minimax algorithm, then restore the board after
		char[] tempBoard = board.clone();
		System.out.println(minimax(2));
		board = tempBoard.clone();
		
		// Do the actual move
		doMove(maxA, maxB, 'b');
	}
	
	public int minimax(int depth) {
		int a, b, value;
		int max = -9999;
		char[] tempBoard = board.clone();
		
		// Evaluate node value when the algorithm reaches terminal node
		if (depth == 0) {
			return eval();
		}
		
		// Find valid moves and store them in the "moves" global variable
		if (validateMoveExists(turn)) {
			// Split valid moves into an array of individual moves
			String[] temp = moves.split(" ");
			
			for (int i=0; i<temp.length; i++) {
				a = Character.getNumericValue(temp[i].charAt(0));	// Start location of current move
				b = Character.getNumericValue(temp[i].charAt(1));	// End location of current move

				// Do move
				doMove(a, b, turn);
				
				// Calculate value of each node, alternating min and max at different levels
				value = -minimax (depth-1);
				
				// Replace the maximum value if current value is greater than maximum
				if (value > max) {
					max = value;
					
					// The best move is the node at the root level
					if (depth == 2) {
						maxA = a;
						maxB = b;
					}
				}
				
				// Restore board when no more valid moves are found
				board = tempBoard.clone();
			}
		}
		
		// Return maximum value 
		return max;
	}
	
	// Part of minimax, used to calculate the value of each node
	public int eval() {
		int white = 0;
		int black = 0;
		
		// Count the number of pieces
		for (int i=0; i<=8; i++) {
			if (Character.isUpperCase(board[i])) {
				white++;
			}
			else if (Character.isLowerCase(board[i])) {
				black++;
			}
		}
		
		// Return the difference between white and black pieces (higher = better for black)
		return (black - white);
	}
	
	// FUNCTIONS ================================================================================

	public void swapTurn() {
		// Swap turns depending on the current player
		if (turn == 'w') {
			turn = 'b';
			lblTurn.setText("Black's turn.");
		}
		else {
			turn = 'w';
			lblTurn.setText("White's turn.");
		}
	}

	public void doMove(int a, int b, char colour) {
		// Perform move if it is a valid move and the king is not moving into check
		if (validateMove(a, b, colour) && !isMovingIntoCheck(a, b, colour)) {
			board[b] = board[a];
			board[a] = '*';
			pawnToBishop();
			swapTurn();
			lblStatus.setText("");
		}
		else {
			lblStatus.setText("Invalid move");
		}
	}

	public void pawnToBishop() {
		// Change all pawns in rows 1 and 7 to bishops
		for (int i=0; i<=8; i++) {
			int row = i / 3 + 1;

			// Row 1
			if ((board[i] == 'P') && (row == 1)) {
				board[i] = 'B';
			}
			// Row 3
			else if ((board[i] == 'p') && (row == 3)) {
				board[i] = 'b';
			}
		}
	}

	public boolean validateMove(int a, int b, char colour) {
		int r1, c1, r2, c2, dr, dc;
			
		// Convert start and end positions to rows and columns
		r1 = a / 3 + 1;		// Start row
		c1 = a % 3 + 1;		// Start column
		r2 = b / 3 + 1;		// End row
		c2 = b % 3 + 1;		// End column
		dr = r2 - r1;		// Change of squares between start and end rows
		dc = c2 - c1;		// Change of squares between start and end columns
		
		// Invalid move if source location is a black piece or destination is a white piece
		if (colour == 'w') {
			if (Character.isLowerCase(board[a]) || Character.isUpperCase(board[b])) {
				return false;
			}
		}
		// Invalid move if source location is a white piece or destination is a black piece
		else if (colour == 'b') {
			if (Character.isUpperCase(board[a]) || Character.isLowerCase(board[b])) {
				return false;
			}
		}
		
		// Check source location to see if a chess piece exists
		switch (board[a]) {
		case '*':
			// Invalid move if source location is an empty piece
			return false;
		case 'P':
			// White pawns can move one square up on the same row if the square is unoccupied
			if ((board[b] == '*') && (c1 == c2) && (dr == -1)) {
				return true;
			}
			// White pawns can also capture opponents diagonally up one square
			else if (Character.isLowerCase(board[b]) && (dr == -1) && (Math.abs(dc) == 1)) {
				return true;
			}
			else {
				return false;
			}
		case 'p':
			// Black pawns can move one square down on the same row if the square is unoccupied
			if ((board[b] == '*') && (c1 == c2) && (dr == 1)) {
				return true;
			}
			// Black pawns can also capture opponents diagonally down one square
			else if (Character.isUpperCase(board[b]) && (dr == 1) && (Math.abs(dc) == 1)) {
				return true;
			}
			else {
				return false;
			}
		case 'K':
		case 'k':
			// Kings can only move one square in any direction
			if ((dr >= -1) && (dr <= 1) && (dc >= -1) && (dc <= 1)) {
				return true;
			}
			else {
				return false;
			}
		case 'B':
		case 'b':
			// Bishops can move diagonally 1 or 2 spaces in any direction
			// Check if moving diagonally
			if (Math.abs(dc) == Math.abs(dr)) {
				// Moving diagonally by 1 space
				if (Math.abs(dc) == 1) {
					return true;
				}
				// Moving diagonally by 2 spaces
				else if (Math.abs(dc) == 2) {
					// Invalid move if center piece is blocked
					if (board[4] != '*') {
						return false;
					}
					else {
						return true;
					}
				}
			}
			else
				return false;
		default:
			return false;
		}
	}
	
	public boolean validateMoveExists(char colour) {
		int x, y;
		boolean cond1, cond2;
		
		moves = "";
		
		// Go through the whole chess board
		for (int i=0; i<=8; i++) {
			// Find pieces of the given colour
			cond1 = (colour == 'w') && Character.isUpperCase(board[i]);
			cond2 = (colour == 'b') && Character.isLowerCase(board[i]);
			
			if (board[i] != '*') {
				if (cond1 || cond2) {
					x = i;
					
					for (int j=0; j<=8; j++) {
						// Destination and source squares must be different
						if (i != j) {
							y = j;
							
							// Add to the list of valid moves for the current player
							if (validateMove(x, y, colour) && !isMovingIntoCheck(x, y, colour)) {
								moves += x + "" + y + " ";
							}
						}
					}
				}
			}
		}
		
		return (!moves.equals(""));
	}

	public boolean isMovingIntoCheck(int a, int b, char colour) {
		// Make a temp copy of the current board
		char[] tempBoard = board.clone();

		// Do the move first
		if (board[a] == 'k' || board[a] == 'K') {
			if (validateMove(a, b, colour)) {
				board[b] = board[a];
				board[a] = '*';
				
				// Check if the move will result in a check
				if (isCheck(turn)) {
					// "Undo" the move
					board = tempBoard.clone();
					return true;
				}
				// "Undo" the move
				board = tempBoard.clone();
			}
		}
		return false;
	}

	public boolean isCheck(char colour) {
		String temp = new String(board);
		
		// Find the kings
		int wk = temp.indexOf("K");
		int bk = temp.indexOf("k");

		// Loop through the chess board to find opponent pieces
		for (int i=0; i<=8; i++) {
			// Check if any black pieces can reach the white king
			if ((colour == 'w') && Character.isLowerCase(board[i])) {
				if(validateMove(i, wk, 'b')) {
					return true;
				}
			}
			// Check if any white pieces can reach the black king
			else if ((colour == 'b') && Character.isUpperCase(board[i])) {
				if(validateMove(i, bk, 'w')) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean isWin(char colour) {
		String temp = new String(board);

		// Check to see if board matches any winning patterns
		if ((colour == 'w') && temp.equals("BB*K****k")) {
			return true;
		}
		else if ((colour == 'b') && temp.equals("*pk*b*KP*")) {
			return true;
		}
		else {
			return false;
		}
	}

	public boolean isTie(char colour) {
		// Sort board
		char[] tempBoard = board.clone();
		Arrays.sort(tempBoard);
		String temp2 = new String(tempBoard);

		// Check if the current player can make any moves and see if board matches any tie patterns
		return (!validateMoveExists(colour) || temp2.equals("******Kbk") || temp2.equals("******BKk") || temp2.equals("******Kkp") ||
			temp2.equals("******KPk") || temp2.equals("*****BKbk") || temp2.equals("*******Kk"));
	}
	
	public boolean isGameOver() {		
		// Check if the game is over (tie, stalemate, or either side wins)
		if (isTie(turn)) {
			lblWin.setText("Tie game or stalemate");
		}
		if (isWin('w')) {
			lblWin.setText("Checkmate! White wins");
		}
		else if (isWin('b')) {
			lblWin.setText("Checkmate! Black wins");
		}
		
		return (isTie(turn) || isWin('w') || isWin('b'));
	}

	public static void main(String[] args) {
		// Create chess board object and displays it
		Chess c = new Chess();
		c.display();
	}
}