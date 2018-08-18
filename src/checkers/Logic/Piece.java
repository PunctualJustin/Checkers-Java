
package checkers.Logic;

public class Piece {
	public boolean king;
	public char color;
	public Piece(char cIn){
		color=cIn;
		king=false;
	}
	public Piece(Piece pIn){
		color=pIn.color;
		king=false;
	}
	public String toString(){
		String str=""+color;
		str+= king ? "K" : " ";
		return str;
	}
}
