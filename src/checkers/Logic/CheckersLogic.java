package checkers.Logic;

import java.util.ArrayList;
import java.util.Arrays;

public class CheckersLogic {
	
	private Node root;
	private byte gameDepth;
	
	/**
	 * Constructs an instance of the game
	 * @param depth The number of moves ahead the computer will look
	 */
	public CheckersLogic(byte depth) {
		root = new Node();
		gameDepth = depth;
	}
	
	/**
	 * Gets the board with the pieces, their positions and other data
	 * @return The current game board
	 */
	public Board getBoard() {
		return root.nodeBoard;
	}
	
	/**
	 * Returns true if either side has no pieces (or kings)
	 */
	public boolean gameOver() {
		return root.redPieces==0 || root.blackPieces==0;
	}
	
	/**
	 * Indicates whether the player or the computer won
	 * @return a char indicating the color of the winner (B for computer, R for player), or null if the game is not over
	 */
	public char getWinner() {
		char r = '\0';
		if(gameOver()) {
			if(root.redPieces>0) {
				r='R';
			}else {
				r='B';
			}
		}
		return r;
	}
		
	/**
	 * Accepts the human player's move
	 * @param move An array of bytes that describe the indexes of the positions being moved on the board {posY1, posX1, posY2, posX2, ...} 
	 * @return Whether the move is valid and completed
	 * @throws Exception
	 */
	public boolean acceptMove(byte[] move) throws Exception {
		boolean validMove=false;
		if(root.nodeBoard.validateMove('R', move)){
			if(move.length==4 && Math.abs(move[0]-move[2])==1 && Math.abs(move[1]-move[3])==1) {
				root.nodeBoard.movePiece(move);
			}else {
				for(int i=0; i<move.length-3; i=i+2) {
					byte[] moveCpy = {move[i], move[i+1], move[i+2], move[i+3]};
					root.nodeBoard.movePiece(moveCpy);
					byte row = (byte)(move[i+2]+((move[i]-move[i+2])/2));
					byte col = (byte)(move[i+3]+((move[i+1]-move[i+3])/2));
					root.nodeBoard.removePiece(row, col);
				}
			}
			root.nodeBoard.kingPiece(move, 'R');
			root=new Node(root, move);
			validMove=true;
		}
		return validMove;
	}

	/**
	 * returns the last move made
	 * @return An array of bytes that describe the indexes of the positions being moved on the board {posY1, posX1, posY2, posX2, ...}
	 */
	public byte[] getLastMove() {
		return root.move;
	}
	
	/**
	 * The computer calculates and completes a move
	 * @throws Exception
	 */
	public void computerMove() throws Exception {
		addChildren(root, 0, gameDepth);
		root = root.bestChoice;
	}
	
	/**
	 * Checks the validity of all potential moves from the parent and adds the valid ones as children
	 * @param parent
	 * @param colorIn
	 * @throws Exception
	 */
	private void addChildren(Node parent, int height, int maxHeight) throws Exception {
		Board board=parent.nodeBoard;
		char colorIn = parent.color;
		boolean setBreak=false;
		for(int r=0; r<8; r++){
			for(int c=0; c<8; c++){
				if(board.getPieces()[r][c]!=null && board.getPieces()[r][c].color==colorIn){
					if(board.getPieces()[r][c].color=='R' || board.getPieces()[r][c].king){
						byte[] tryMove = {(byte)r,(byte)c,(byte)(r-1),(byte)(c-1)};
						byte[] tryHop = {(byte)r,(byte)c,(byte)(r-2),(byte)(c-2)};
						byte[] delta= {(byte)-1, (byte)-1};
						setBreak=moveOrHop(board, parent, colorIn, tryMove, tryHop, delta, height+1, maxHeight);
						if(setBreak) {
							break;
						}
						tryMove[3] = (byte)(c+1);
						tryHop[3] = (byte)(c+2);
						delta[1] = (byte)+1;
						setBreak=moveOrHop(board, parent, colorIn, tryMove, tryHop, delta, height+1, maxHeight);
					}
					if(board.getPieces()[r][c].color=='B' || board.getPieces()[r][c].king){
						byte[] tryMove = {(byte)r,(byte)c,(byte)(r+1),(byte)(c-1)};
						byte[] tryHop = {(byte)r,(byte)c,(byte)(r+2),(byte)(c-2)};
						byte[] delta = {(byte)+1, (byte)-1};
						setBreak=moveOrHop(board, parent, colorIn, tryMove, tryHop, delta, height+1, maxHeight);
						if(setBreak) {
							break;
						}
						tryMove[3] = (byte)(c+1);
						tryHop[3] = (byte)(c+2);
						delta[1] = (byte)+1;
						setBreak=moveOrHop(board, parent, colorIn, tryMove, tryHop, delta, height+1, maxHeight);
					}
					if(setBreak) {
						break;
					}
				}
			}
			if(setBreak) {
				break;
			}
		}
	}
	
	/**
	 * Checks if a valid move or hop exists and adds it to the parent.
	 * @param board
	 * @param parent
	 * @param colorIn
	 * @param tryMove
	 * @param tryHop
	 * @param delta
	 * @throws Exception
	 */
	private boolean moveOrHop(Board board, Node parent, char colorIn, byte[] tryMove, byte[] tryHop, byte[] delta, int height, int maxHeight) throws Exception{
		if(board.validateMove(colorIn, tryMove)){
			addValidMoveNode(parent, colorIn, tryMove, height, maxHeight);
		}else if(board.validateMove(colorIn, tryHop)){
			addValidHopNode(parent, colorIn, tryHop, height, maxHeight);
		}
		return evaluateAlphaBeta(parent);
	}
	
	private boolean evaluateAlphaBeta(Node node) {
		if(node.alphaPieces>=node.betaPieces) {
			return true;
		}
		return false;
	}
	
	/**
	 * Checks for valid hops in all directions after one hop has been validated
	 * @param board The board from the previous hop
	 * @param parent The originating node
	 * @param colorIn
	 * @param tryHop The previous hop move to build onto
	 * @throws Exception
	 */
	private void addNthHop(Board board, Node parent, char colorIn, byte[] tryHop, int height, int maxHeight) throws Exception {
		int thLen = tryHop.length;
		byte r=tryHop[thLen-2];
		byte c=tryHop[thLen-1];
		byte[] shortHop=new byte[4];
		shortHop[0]=r;
		shortHop[1]=c;
		if(board.getPieces()[r][c].color=='R' || board.getPieces()[r][c].king){
			shortHop[2]=(byte)(r-2);
			shortHop[3]=(byte)(c-2);
			nthHop(board, parent, colorIn, tryHop, shortHop, height, maxHeight);
			shortHop[3]=(byte)(c+2);
			nthHop(board, parent, colorIn, tryHop, shortHop, height, maxHeight);
		}
		if(board.getPieces()[r][c].color=='B' || board.getPieces()[r][c].king){
			shortHop[2]=(byte)(r+2);
			shortHop[3]=(byte)(c-2);
			nthHop(board, parent, colorIn, tryHop, shortHop, height, maxHeight);
			shortHop[3]=(byte)(c+2);
			nthHop(board, parent, colorIn, tryHop, shortHop, height, maxHeight);
		}
	}
	
	/**
	 * Validate multi-hop and add child to parent, if valid
	 * @param board
	 * @param parent
	 * @param colorIn
	 * @param tryHop
	 * @param shortHop
	 * @throws Exception
	 */
	private void nthHop(Board board, Node parent, char colorIn, byte[] tryHop, byte[] shortHop, int height, int maxHeight) throws Exception {
		if(board.validateMove(colorIn, shortHop)){
			int thLen=tryHop.length;
			byte[] newHop = new byte[thLen+2];
			for(int i=0; i<thLen; i++) {
				newHop[i]=tryHop[i];
			}
			newHop[thLen]=shortHop[2];
			newHop[thLen+1]=shortHop[3];
			addValidHopNode(parent, colorIn, newHop, height, maxHeight);
		}
	}
	
	/**
	 * Adds a valid child (move) to the parent
	 * @param parent
	 * @param colorIn
	 * @param tryMove
	 * @throws Exception
	 */
	private void addValidMoveNode(Node parent, char colorIn, byte[] tryMove, int height, int maxHeight) throws Exception{
		Node tempNode=new Node(parent, tryMove);
		if(tempNode.nodeBoard.movePiece(tryMove)){
			tempNode.nodeBoard.kingPiece(tryMove, colorIn);
			//parent.children.add(tempNode);
			//alpha/beta
			if(height==maxHeight) {
				tempNode.evaluate();
				tempNode.passAlphaBeta();
			}else {
				//go deeper
				addChildren(tempNode, height, maxHeight);
				//pass up alpha-beta
				tempNode.passAlphaBeta();
			}
		}else{
			throw new Exception("could not move piece");
		}
	}
	
	/**
	 * Adds a valid child (hop) to the parent and recursively checks for nth hops from the new position
	 * @param parent
	 * @param colorIn
	 * @param tryMove
	 * @throws Exception
	 */
	private void addValidHopNode(Node parent, char colorIn, byte[] tryHop, int height, int maxHeight) throws Exception{
		Node tempNode=new Node(parent, tryHop);
		for(int i=0; i<=tryHop.length-4; i+=2) {
			if(tempNode.nodeBoard.movePiece(tryHop[i], tryHop[i+1], tryHop[i+2], tryHop[i+3])){
				int vDelta=(tryHop[i+2]-tryHop[i])/2;
				int hDelta=(tryHop[i+3]-tryHop[i+1])/2;
				tempNode.nodeBoard.removePiece((byte)(tryHop[i]+vDelta), (byte)(tryHop[i+1]+hDelta));
				if(colorIn=='R'){
					tempNode.blackPieces--;
				}else{
					tempNode.redPieces--;
				}
			} else {
				throw new Exception("could not move piece");
			}
		}
		tempNode.nodeBoard.kingPiece(tryHop, colorIn);
		//parent.children.add(tempNode);
		if(height==maxHeight) {
			tempNode.evaluate();
			tempNode.passAlphaBeta();
		}else {
			//go deeper
			if(tempNode.redPieces>0&&tempNode.blackPieces>0) {
				addChildren(tempNode, height, maxHeight);
			}else {
				tempNode.evaluate();
			}
			//pass up alpha-beta
			tempNode.passAlphaBeta();
		}
		addNthHop(tempNode.nodeBoard, parent, colorIn, tryHop, height, maxHeight);
	}
		
	/**
	 * Nodes contain a given move and the meta data around it, including the move sequence (parent and children). 
	 * 
	 * @author Justin Gaudet
	 *
	 */
	private class Node{
		public Board nodeBoard;
		public ArrayList<Node> children;
		public Node parent;
		public byte[] move;
		public char color;
		public byte redPieces;
		public byte blackPieces;
		public Node bestChoice;
		public byte value;
		public byte alphaPieces;
		public byte betaPieces;
		public byte depth;
		
		public Node(){
			depth=0;
			nodeBoard=new Board();
			redPieces=12;
			blackPieces=12;
			children=new ArrayList<Node>();
			alphaPieces=Byte.MIN_VALUE;
			betaPieces=Byte.MAX_VALUE;
			color='R';
			value= color=='R' ? Byte.MAX_VALUE : Byte.MIN_VALUE;
		}
		
		public Node(Node parentIn, byte[] moveIn){
			depth=(byte)(parentIn.depth+1);
			move=new byte[moveIn.length];
			for(int i=0; i<move.length; i++){
				move[i]=moveIn[i];
			}
			parent=parentIn;
			redPieces=parentIn.redPieces;
			blackPieces=parentIn.blackPieces;
			nodeBoard=new Board(parentIn.nodeBoard);
			children=new ArrayList<Node>();
			alphaPieces=parentIn.alphaPieces;
			betaPieces=parentIn.betaPieces;
			color = parentIn.color=='R' ? 'B' : 'R';
			value= color=='R' ? Byte.MAX_VALUE : Byte.MIN_VALUE;
		}
		
		public void evaluate() {
			byte eval;
			if(blackPieces==0) {
				eval=Byte.MIN_VALUE+1;
			}else if(redPieces==0) {
				eval=Byte.MAX_VALUE-1;
			}else{
				eval = (byte)(blackPieces-redPieces);
			}
			value=eval;
			if(color=='R') {
				betaPieces=eval;
			}else{
				alphaPieces=eval;
			}
		}
		
		public void passAlphaBeta(){
			if(parent.color=='R'){
				if(parent.betaPieces>value){
					parent.betaPieces=value;
					parent.value=alphaPieces;
					parent.bestChoice=this;
				}else if(parent.value>value) {
					parent.value=value;
					parent.bestChoice=this;
				}
			}else{
				if(parent.alphaPieces<value){
					parent.alphaPieces=value;
					parent.value=betaPieces;
					parent.bestChoice=this;
				}else if(parent.value<value) {
					parent.value=value;
					parent.bestChoice=this;
				}
			}
		}
		
		public String toString(){
			String str="Node "+color+depth+" ";
			str+="Value "+value+" ";
			str+="Alpha "+alphaPieces+" Beta "+betaPieces;
			str+=" with move "+Arrays.toString(move);
			return str;
		}
	}
	
	public void debugSimpleOptTest() {
		Board board = root.nodeBoard;
		int red=1;
		int black=3;
		root.redPieces=1;
		root.blackPieces=3;
		for(int r=0; r<8; r++){
			for(int c=0; c<8; c++){
				if(board.getPieces()[r][c]!=null){
					if(board.getPieces()[r][c].color=='R'&& red<12){
						red++;
						board.removePiece((byte)r, (byte)c);
					}else if(board.getPieces()[r][c].color=='B'&& black<12){
						black++;
						board.removePiece((byte)r, (byte)c);
					}
				}
			}
		}
		byte[] move = {7, 7, 5, 7};
		board.movePiece(move);
		byte[] move1 = {2, 2, 1, 7};
		board.movePiece(move1);
	}
	
	public void debugTestKingSetup() {
		Board board = root.nodeBoard;
		byte row=0;
		byte col=4;
		board.removePiece(row, col);
		row = 2;
		col = 6;
		board.removePiece(row, col);
		row = 2;
		col = 2;
		board.removePiece(row, col);
		byte rowFr = 5;
		byte colFr = 1;
		byte[] move = {rowFr, colFr, row, col};
		board.movePiece(move);
		colFr = 3;
		row = 3;
		col = 1;
		move[1] = colFr;
		move[2] = row;
		move[3] = col;
		board.movePiece(move);
		colFr = 5;
		col = 3;
		move[1] = colFr;
		move[3] = col;
		board.movePiece(move);
		colFr = 7;
		row = 4;
		col = 2;
		move[1] = colFr;
		move[2] = row;
		move[3] = col;
		board.movePiece(move);
	}

	public void debugTestDoubleHopRed() {
		Board board = root.nodeBoard;
		byte row=0;
		byte col=2;
		board.removePiece(row, col);
		row = 2;
		board.removePiece(row, col);
		row = 0;
		col = 0;
		board.removePiece(row, col);
		byte rowFr = 2;
		byte colFr = 4;
		row = 3;
		col = 1;
		byte[] move = {rowFr, colFr, row, col};
		board.movePiece(move);
		rowFr = 5;
		colFr = 1;
		row = 4;
		col = 0;
		move[0] = rowFr;
		move[1] = colFr;
		move[2] = row;
		move[3] = col;
		board.movePiece(move);
		rowFr = 5;
		colFr = 7;
		row = 4;
		col = 6;
		move[0] = rowFr;
		move[1] = colFr;
		move[2] = row;
		move[3] = col;
		board.movePiece(move);
		rowFr = 0;
		colFr = 6;
		row = 3;
		col = 5;
		move[0] = rowFr;
		move[1] = colFr;
		move[2] = row;
		move[3] = col;
		board.movePiece(move);
	}

	public void debugTestDoubleHopBlack() {
		Board board = root.nodeBoard;
		byte row=7;
		byte col=5;
		board.removePiece(row, col);
		byte rowFr = 2;
		byte colFr = 4;
		row = 3;
		col = 5;
		byte[] move = {rowFr, colFr, row, col};
		board.movePiece(move);
	}
}

//TODO: Add custom exceptions