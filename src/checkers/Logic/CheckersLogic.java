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
	 * Returns false if either side has no pieces (or kings)
	 */
	public boolean bothHavePieces() {
		return root.redPieces>0 && root.blackPieces>0;
	}
	
	/**
	 * Accepts the human player's move
	 * @param move An array of bytes that describe the indexes of the positions being moved on the board {posY1, posX1, posY2, posX2, ...} 
	 * @return Whether the move is valid and completed
	 * @throws Exception
	 */
	public boolean acceptMove(byte[] move) throws Exception {
		boolean validMove=false;
		if(root.children.size()>0){
			for(Node childNode : root.children){
				if(Arrays.equals(childNode.move, move)){
					root=childNode;
					root.parent=null;
					validMove=true;
					break;
				}
			}
		}else{
			if(root.nodeBoard.validateMove('R', move) && root.nodeBoard.movePiece(move)){
				validMove=true;
			}
		}

		return validMove;
	}
	
	/**
	 * The computer calculates and completes a move
	 * @throws Exception
	 */
	public void computerMove() throws Exception {
		//computer makes move
		buildGameTree(root, 'B');
		byte iBLeafPieces=0;
		byte iRLeafPieces=13;
		int[] bestIndex=new int[root.children.size()];
		int bestCount=0;
		for(int i=0; i<root.children.size(); i++){
			Node childNode=root.children.get(i);
			if(childNode.inductiveRedLeafPieces<iRLeafPieces || (childNode.inductiveRedLeafPieces==iRLeafPieces && childNode.inductiveBlackLeafPieces>iBLeafPieces)){
				iRLeafPieces = childNode.inductiveRedLeafPieces;
				iBLeafPieces = childNode.inductiveBlackLeafPieces;
				bestIndex[0] = i;
				bestCount = 1;
			}else if(childNode.inductiveRedLeafPieces==iRLeafPieces && childNode.inductiveBlackLeafPieces==iBLeafPieces){
				bestIndex[bestCount] = i;
				bestCount++;
			}
		}

		root=root.children.get(bestIndex[(int)Math.floor(Math.random()*bestCount)]);
	}
	
	/**
	 * returns the last move made
	 * @return An array of bytes that describe the indexes of the positions being moved on the board {posY1, posX1, posY2, posX2, ...}
	 */
	public byte[] getLastMove() {
		return root.move;
	}
	
	/**
	 * Determines the next possible moves to be made by each player
	 * @param root The root Node
	 * @param color 'R' or 'B' for red or black respectively. Human player is red.
	 * @throws Exception
	 */
	private void buildGameTree(Node root, char color) throws Exception{
		boolean outOfMemory = false;
		
		byte height = getHeight(root, (byte)0);
		color = height%2==0 ? 'B' : 'R';
		while(!outOfMemory && height<gameDepth) {
			outOfMemory = addAtHeight(root, color, (byte)0, height);
			if(!outOfMemory) {
				height++;
				color = color=='R' ? 'B' : 'R';
			}
		}
		if(outOfMemory) {
			System.gc();
		}
	}
	
	/**
	 * Gets the current height of the game tree (recursive)
	 * @param node current node (call on root)
	 * @param The current height the method is called at.
	 * @return The height of the game tree
	 */
	private byte getHeight(Node node, byte height) {
		if(node.children.size()>0) {
			byte maxHeight=0;
			byte currHeight=0;
			for(Node child : node.children) {
				currHeight = getHeight(child, (byte)(height+1));
				if(currHeight>maxHeight) {
					maxHeight=currHeight;
				}
			}
			return maxHeight;
		}
		return height;
	}

	/**
	 * Finds the valid children (moves) for the node at the given height
	 * @param parent The parent Node
	 * @param colorIn 'R' or 'B' for red or black respectively. Human player is red.
	 * @param level The current height the method is called at.
	 * @param height The height at which to find valid moves and add children
	 * @return Whether the program ran out of memory
	 * @throws Exception
	 */
	private boolean addAtHeight(Node parent, char colorIn, byte level, byte height) throws Exception{
		if(level<height) {
			boolean outOfMemory = false;
			int childIndex = 0;
			int numChildren = parent.children.size();
			for(; childIndex<numChildren; childIndex++) {
				outOfMemory = addAtHeight(parent.children.get(childIndex), colorIn, (byte)(level+1), height);
				if(outOfMemory) {
					if(height==level) {
						//parent.children=new ArrayList<Node>();
						parent.children=null;
					}else{
						for(childIndex-=1; childIndex>=0; childIndex--) {
							destroyUnevenChildren(parent.children.get(childIndex), (byte)(level+1), height);
						}
					}
					return true;
				}
			}
		}else if(level==height){
			try {
				addChildren(parent, colorIn);
			}catch(OutOfMemoryError e) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Checks the validity of all potential moves from the parent and adds the valid ones as children
	 * @param parent
	 * @param colorIn
	 * @throws Exception
	 */
	private void addChildren(Node parent, char colorIn) throws Exception {
		Board board=parent.nodeBoard;
		for(int r=0; r<8; r++){
			for(int c=0; c<8; c++){
				if(board.getPieces()[r][c]!=null && board.getPieces()[r][c].color==colorIn){
					if(board.getPieces()[r][c].color=='R' || board.getPieces()[r][c].king){
						byte[] tryMove = {(byte)r,(byte)c,(byte)(r-1),(byte)(c-1)};
						byte[] tryHop = {(byte)r,(byte)c,(byte)(r-2),(byte)(c-2)};
						byte[] delta= {(byte)-1, (byte)-1};
						moveOrHop(board, parent, colorIn, tryMove, tryHop, delta);
						tryMove[3] = (byte)(c+1);
						tryHop[3] = (byte)(c+2);
						delta[1] = (byte)+1;
						moveOrHop(board, parent, colorIn, tryMove, tryHop, delta);
					}
					if(board.getPieces()[r][c].color=='B' || board.getPieces()[r][c].king){
						byte[] tryMove = {(byte)r,(byte)c,(byte)(r+1),(byte)(c-1)};
						byte[] tryHop = {(byte)r,(byte)c,(byte)(r+2),(byte)(c-2)};
						byte[] delta = {(byte)+1, (byte)-1};
						moveOrHop(board, parent, colorIn, tryMove, tryHop, delta);
						tryMove[3] = (byte)(c+1);
						tryHop[3] = (byte)(c+2);
						delta[1] = (byte)+1;
						moveOrHop(board, parent, colorIn, tryMove, tryHop, delta);
					}
				}
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
	private void moveOrHop(Board board, Node parent, char colorIn, byte[] tryMove, byte[] tryHop, byte[] delta) throws Exception{
		if(board.validateMove(colorIn, tryMove)){
			addValidMoveNode(parent, colorIn, tryMove);
		}else if(board.validateMove(colorIn, tryHop)){
			addValidHopNode(parent, colorIn, tryHop);
		}
	}
	
	/**
	 * Checks for valid hops in all directions after one hop has been validated
	 * @param board The board from the previous hop
	 * @param parent The originating node
	 * @param colorIn
	 * @param tryHop The previous hop move to build onto
	 * @throws Exception
	 */
	private void addNthHop(Board board, Node parent, char colorIn, byte[] tryHop) throws Exception {
		int thLen = tryHop.length;
		byte r=tryHop[thLen-2];
		byte c=tryHop[thLen-1];
		byte[] shortHop=new byte[4];
		shortHop[0]=r;
		shortHop[1]=c;
		if(board.getPieces()[r][c].color=='R' || board.getPieces()[r][c].king){
			shortHop[2]=(byte)(r-2);
			shortHop[3]=(byte)(c-2);
			nthHop(board, parent, colorIn, tryHop, shortHop);
			shortHop[3]=(byte)(c+2);
			nthHop(board, parent, colorIn, tryHop, shortHop);
		}
		if(board.getPieces()[r][c].color=='B' || board.getPieces()[r][c].king){
			shortHop[2]=(byte)(r+2);
			shortHop[3]=(byte)(c-2);
			nthHop(board, parent, colorIn, tryHop, shortHop);
			shortHop[3]=(byte)(c+2);
			nthHop(board, parent, colorIn, tryHop, shortHop);
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
	private void nthHop(Board board, Node parent, char colorIn, byte[] tryHop, byte[] shortHop) throws Exception {
		if(board.validateMove(colorIn, shortHop)){
			int thLen=tryHop.length;
			byte[] newHop = new byte[thLen+2];
			for(int i=0; i<thLen; i++) {
				newHop[i]=tryHop[i];
			}
			newHop[thLen]=shortHop[2];
			newHop[thLen+1]=shortHop[3];
			addValidHopNode(parent, colorIn, newHop);
		}
	}
	
	/**
	 * Adds a valid child (move) to the parent
	 * @param parent
	 * @param colorIn
	 * @param tryMove
	 * @throws Exception
	 */
	private void addValidMoveNode(Node parent, char colorIn, byte[] tryMove) throws Exception{
		Node tempNode=new Node(parent, tryMove);
		if(tempNode.nodeBoard.movePiece(tryMove)){
			tempNode.nodeBoard.kingPiece(tryMove, colorIn);
			parent.children.add(tempNode);
			tempNode.inductiveRedLeafPieces=tempNode.redPieces;
			tempNode.inductiveBlackLeafPieces=tempNode.blackPieces;
			trackLeafPieces(tempNode, colorIn);
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
	private void addValidHopNode(Node parent, char colorIn, byte[] tryHop) throws Exception{
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
		parent.children.add(tempNode);
		tempNode.inductiveRedLeafPieces=tempNode.redPieces;
		tempNode.inductiveBlackLeafPieces=tempNode.blackPieces;
		trackLeafPieces(tempNode, colorIn);
		addNthHop(tempNode.nodeBoard, parent, colorIn, tryHop);
	}
	
	/**
	 * Destroys all of the leafs of this subtree from the indicated level
	 * @param node The current Node
	 * @param level The current height the method is called at.
	 * @param destroyLevel The height at which to remove children
	 */
	private void destroyUnevenChildren(Node node, byte level, byte destroyLevel) {
		if(level<destroyLevel) {
			if(node.children!=null) {
				int numChildren = node.children.size();
				for(int childIndex = 0; childIndex<numChildren; childIndex++) {
					destroyUnevenChildren(node.children.get(childIndex), (byte)(level+1), destroyLevel);
				}
			}
		}else if(level==destroyLevel){
			node.children=null;
			//System.gc();
		}
		
	}

	private static void trackLeafPieces(Node cNode, char color){
		while(cNode.parent!=null){
			Node pNode=cNode.parent;
			if(color=='R'){
				if((cNode.inductiveBlackLeafPieces<pNode.inductiveBlackLeafPieces) || (cNode.inductiveBlackLeafPieces==pNode.inductiveBlackLeafPieces && cNode.inductiveRedLeafPieces>pNode.inductiveRedLeafPieces)){
					color = 'B';
				}else{
					break;
				}
			}else{
				if((cNode.inductiveRedLeafPieces<pNode.inductiveRedLeafPieces) || (cNode.inductiveRedLeafPieces==pNode.inductiveRedLeafPieces && cNode.inductiveBlackLeafPieces>pNode.inductiveBlackLeafPieces)){
					color = 'R';
				}else{
					break;
				}
			}
			pNode.inductiveRedLeafPieces=cNode.inductiveRedLeafPieces;
			pNode.inductiveBlackLeafPieces=cNode.inductiveBlackLeafPieces;
			cNode=pNode;
		}
	}
	
	/**
	 * Nodes contain a given move and the meta data around it, including the move sequence (parent and children). 
	 * 
	 * @author Justin
	 *
	 */
	private class Node{
		public Board nodeBoard;
		public ArrayList<Node> children;
		public Node parent;
		public byte[] move;
		public byte redPieces;
		public byte blackPieces;
		public byte inductiveRedLeafPieces;
		public byte inductiveBlackLeafPieces;
		
		public Node(){
			nodeBoard=new Board();
			redPieces=12;
			blackPieces=12;
			children=new ArrayList<Node>();
			inductiveRedLeafPieces=12;
			inductiveBlackLeafPieces=12;
		}
		
		public Node(Node parentIn, byte[] moveIn){
			move=new byte[moveIn.length];
			for(int i=0; i<move.length; i++){
				move[i]=moveIn[i];
			}
			parent=parentIn;
			redPieces=parentIn.redPieces;
			blackPieces=parentIn.blackPieces;
			nodeBoard=new Board(parentIn.nodeBoard);
			children=new ArrayList<Node>();
			inductiveRedLeafPieces=12;
			inductiveBlackLeafPieces=12;
		}
		
		public String toString(){
			String str="Node\n";
			str+="\tnodeBoard != null : "+(nodeBoard!=null)+"\n";
			str+="\tHas parent : "+(parent!=null)+"\n";
			str+="\tno move : ";
			if(move!=null){
				str+=move[0]+", "+move[1]+", "+move[2]+", "+move[3]+"\n";
			}else{
				str+="none\n";
			}
			str+="\tNumber of children : "+children.size()+"\n";
			str+="\tredPieces : "+redPieces+"\n";
			str+="\tblackPieces : "+blackPieces+"\n";
			str+="\tinductiveRedLeafPieces : "+inductiveRedLeafPieces+"\n";
			str+="\tinductiveBlackLeafPieces : "+inductiveBlackLeafPieces+"";
			return str;
		}
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


//TODO: Make stale mate a secondary option rather than an equal one
//TODO: pass up the leastblack rather than the most black leaf nodes
//TODO: Add custom exceptions