package checkers.Logic;

//TODO: change how pieces are handled. make the pieces array a 1d array with locations stored in the piece instead of 2d

public class Board{
	private Piece[][] pieces;
	private static final boolean debug=true;
	
	public Board(){
		pieces=new Piece[8][8];
		for(int r=0; r<3; r++){
			for(int c=0; c<8; c++){
				if(c%2==r%2){
					pieces[r][c]=new Piece('B');
				}
			}
		}
		for(int r=5; r<8; r++){
			for(int c=0; c<8; c++){
				if(c%2==r%2){
					pieces[r][c]=new Piece('R');
				}
			}
		}
	}
	
	public Board(Board boardIn){
		pieces=new Piece[8][8];
		for(int i=0; i<8; i++){
			for(int j=0; j<8; j++){
				pieces[i][j]=boardIn.pieces[i][j];
			}
		}
	}
	
	public Piece[][] getPieces(){
		return pieces;
	}
	
	public boolean validateMove(char colorIn, byte[] moveIn) throws Exception{
		return validateMove(colorIn, moveIn, pieces);
	}
	
	private boolean validateMove(char colorIn, byte[] moveIn, Piece[][] piecesIn) throws Exception{
		//if the position being moved from is empty or contains a piece that is not of the user's color or
		//if the move is to the same position as the current position
		if(piecesIn[moveIn[0]][moveIn[1]]==null || 
				piecesIn[moveIn[0]][moveIn[1]].color!=colorIn || 
				(moveIn[0]==moveIn[2] && moveIn[1]==moveIn[3])){
			return false;
		}
		
		byte vDelta = (byte)(moveIn[2]-moveIn[0]);
		byte hDelta = (byte)(moveIn[3]-moveIn[1]);

		//if the move is within the bounds of the board and
		//if the move would be to an empty space
		if(moveIn[2]>=0 && moveIn[2]<8 && moveIn[3]>=0 && moveIn[3]<8 && piecesIn[moveIn[2]][moveIn[3]]==null){
			//if the piece is traveling in the wrong direction up or down the board
			if(!piecesIn[moveIn[0]][moveIn[1]].king &&
					((colorIn=='R' && vDelta > 0) ||
					 (colorIn=='B' && vDelta < 0))) {
				return false;
			}
			
			//if moving 1 diagonal space and there are only 2 positions in moveIn, test move
			if((vDelta == -1 || vDelta == 1) && (hDelta == -1 || hDelta == 1) && moveIn.length==4) {
				return true;
				
			//if moving 2 diagonal spaces, test jump
			}else if((vDelta == -2 || vDelta == 2) && (hDelta == -2 || hDelta == 2)) {
				//if there is a piece in that position and it is not of the same color
				if(piecesIn[moveIn[0]+(vDelta/2)][moveIn[1]+(hDelta/2)] != null && piecesIn[moveIn[0]+(vDelta/2)][moveIn[1]+(hDelta/2)].color!=colorIn){
					//if there are no further hops intended
					if(moveIn.length==4) {
						return true;
					}else{
						int d = moveIn[5]-moveIn[3];
						if(d == -2 || d == 2) {
							byte[] checkMove=new byte[moveIn.length-2];
							for(int i=2; i<moveIn.length; i++) {
								checkMove[i-2]=moveIn[i];
							}
							Board tempBoard=new Board(this);
							tempBoard.movePiece(moveIn);
							return validateMove(colorIn, checkMove, tempBoard.getPieces());
						}
					}
				}
			}
		}
		return false;
	}
	
	public boolean movePiece(byte rFr, byte cFr, byte rTo, byte cTo) {
		byte[] move = {rFr, cFr, rTo, cTo};
		return movePiece(move);
	}
	
	public boolean movePiece(byte[] move){
		boolean success=false;
		if(addPiece(move[2], move[3], pieces[move[0]][move[1]])){
			if(removePiece(move[0], move[1])){
				success=true;
			}else{
				removePiece(move[2], move[3]);
			}
		}else{
			if(debug){
				System.out.println("failed addPiece");
			}
		}
		return success;
	}

	public boolean kingPiece(byte[] move, char colorIn) {
		boolean success=false;
		byte lastRow=move[move.length-2];
		byte lastCol=move[move.length-1];
		if((colorIn=='R' && lastRow==0) || (colorIn=='B' && lastRow==7)) {
			
			pieces[lastRow][lastCol]=new Piece(pieces[lastRow][lastCol]);
			pieces[lastRow][lastCol].king = true;
			success=true;
		}
		return success;
	}
	
	public boolean removePiece(byte rowIn, byte colIn){
		boolean success=false;
		if(pieces[rowIn][colIn]!=null){
			pieces[rowIn][colIn]=null;
			success=true;
		}
		return success;
	}
	
	public boolean addPiece(byte rowIn, byte colIn, Piece pieceIn){
		boolean success=false;
		if(pieces[rowIn][colIn]==null){
			pieces[rowIn][colIn]=pieceIn;
			success=true;
		}
		return success;
	}
	
	public String toString(){
		String str="    1  2  3  4  5  6  7  8\n";
		for(int r=0; r<17; r++){
			if(r%2==1){
				str+=(char)(65+(r/2))+" |";
				for(int c=0; c<8; c++){
					if(pieces[(int)(r/2)][c]!=null){
						str+=pieces[(int)(r/2)][c].toString();
						str+="|";
					}else{
						str+="  |";
					}
				}
				str+="\n";
			}else{
				str+="   -- -- -- -- -- -- -- --\n";
			}
		}
		return str;
	}
	
	
}
