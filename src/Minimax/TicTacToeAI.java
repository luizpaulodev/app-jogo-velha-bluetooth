package Minimax;

/*
      title: TicTacToe AI-ENGINE 
     author: Kris Cieslak
       date: 03.09.2012
    license: http://creativecommons.org/licenses/by-nc-sa/3.0/deed.en_US
   language: java

   description: 
      Complete game-tree search/decision algorithm.
      NegaMax function without recursion depth checking. 


 Methods:
    private void put(int pos)
    private void clear(int pos)
    private boolean Check(int P)
    private int GameOver()
    private int NegaMax(int p)
    private int PosToBit(int pos)
    private int BitToPos(int bitNum)

    public boolean Move(int pos, int player)
    public int GenerateMove(int Player)
    public int getX()
    public int getO()
    public int getBoard()
    public void NewGame()
    public int isGameOver() 
 */

public class TicTacToeAI {

/*
   BOARD (int) :
   001|002|004
   ---+---+---
   008|016|032
   ---+---+---
   064|128|256

   X - only player X (1) positions
   O - only player O (-1) positions

   X | O = Board

*/
    private int X = 0, O = 0;

/*  -==-=-=-=- put method -==-=-=-=-=-=-=-=-=-=-=-

    Turns on (set 1) specified bit.

    a pos sign determinates the choice of an variable (player).

    "pos" must be in +/-[1,2,4,8,16,32,64,128,256]  
    "pos" > 0 - X move
    "pos" < 0 - O move

 */
    private void put(int pos) {
        X = X | pos & -((pos >> 31) + 1) & ~O;
        O = O | -pos & (pos >> 31) & ~X;
    }

/* -==-=-=-=- clear method -==-=-=-=-=-=-=-=-=-=-=-

    Turns off (set 0) specified bit.
    sign doesn't matter.   

*/
    private void clear(int pos) {
        X = X & ~pos;
        O = O & ~pos;
    }

/* -==-=-=-=- check method -==-=-=-=-=-=-=-=-=-=-=-	

   P - X or Y (not 1/-1) (look at the GameOver method)

   Winning bits:
         Row 1 - 000000111 = 0x007                 
         Row 2 - 000111000 = 0x038 (0x007 << 3)
         Row 3 - 111000000 = 0x1C0 (0x007 << 6)
         Col 1 - 001001001 = 0x049 
         Col 2 - 010010010 = 0x092 (0x049 << 1)
         Col 3 - 100100100 = 0x124 (0x049 << 2)
    Diagonal 1 - 100010001 = 0x111
    Diagonal 2 - 001010100 = 0x054 

*/
    private boolean Check(int P) {
        return  (P & 0x007) == 7 || 
                (P & 0x038) == 0x038 || 
                (P & 0x1C0) == 0x1C0 || 
                (P & 0x049) == 0x49 || 
                (P & 0x092) == 0x92 || 
                (P & 0x124) == 0x124 || 
                (P & 0x111) == 0x111 ||
                (P & 0x054) == 0x54;
    }

/* -==-=-=-=- check method -==-=-=-=-=-=-=-=-=-=-=-
    returns
        2048 - X won
        512  - O won
        1024 - drawn
           0 - game still goes on

*/	
    private int GameOver() {
        return Check(X) ? 2048 : Check(O) ? 512 : ((X | O) & 511) == 511 ? 1024 : 0;
    }



/* -==-=-=-=- NegaMax method -==-=-=-=-=-=-=-=-=-=-=-

    best_value (binary)

          BEST SCORE                    BEST MOVE 
          2048 1024  512 | 256 128 64  32  16   8   4   2   1
            0    0    0  |  0   0   0   0   0   0   0   0   0
            |    |    |     
   Winner   X    0    O


    best_value & 0xfffffe00 - clear move bit (get score bits)

    Worst "best_value" for X - 512 (O wins)
    Worst "best_value" for O - 2048 (X wins)

*/	
    private int NegaMax(int p) {
        int End = GameOver();
        if (End != 0)
            return End;

        int best_value = (p == 1) ? 512 : 2048;
        for (int b = 1; b <= 256; b = b << 1) {
            int move = (~(X | O) & b);
            if (move != 0) {
                put(p * move);
                int s = NegaMax(-p);
                best_value = p * (s & 0xfffffe00) > p * (best_value & 0xfffffe00) ? ((s & 0xfffffe00) | move) : best_value;
                clear(move);
            }

        }
        return best_value;
    }

/* -==-=-=-=- PosToBit/BitToPos method -==-=-=-=-=-=-=-=-=-=-=-

    Standard field numeration (1,2,3,4,5,6,7,8,9)  
    bit position (1,2,4,8,16,32,64,128,256)

*/	
    private int PosToBit(int pos) {
        return pos>=1 && pos<=9 ? 1<<(pos-1) : 0;
    }

    private int BitToPos(int bitNum) {
        int result = 1;
        while ( (bitNum=bitNum>>1) > 0 ) 
            result++;   
        return result;
    }

/*
  -=-= PUBLIC =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=--==-=-=-=-=-=-
 */

/* -==-=-=-=- Move method -==-=-=-=-=-=-=-=-=-=-=-

    player -  1 or -1
    pos in [1,2,3,4,5,6,7,8,9]

*/
    public boolean Move(int pos, int player) {
        int p = PosToBit(pos);
        if ( p!=0  && ( player==1 || player==-1 ) && ((X | O) & p)==0) {
            put(p*player);
            return true;
        } else
            return false; 
    }


 /* -==-=-=-=- GenerateMoveNegaMax method -==-=-=-=-=-=-=-=-=-=-=-
      Opponent = 1, -1
 */
    public int GenerateMove(int Player) {
        return BitToPos((NegaMax(Player) & 511));
    }


// -==-=-=-=- getX -==-=-=-=-=-=-=-=-=-=-=- 	
    public int getX() {
        return X;
    }

// -==-=-=-=- getO -==-=-=-=-=-=-=-=-=-=-=-	
    public int getO() {
        return O;
    }

// -==-=-=-=- getBoard -==-=-=-=-=-=-=-=-=-=-=-
    public int getBoard() {
        return (X|O);
    }

// -==-=-=-=- ClearBoard -==-=-=-=-=-=-=-=-=-=-=-	
    public void NewGame() {
        X=O=0;
    }

    /* 1 - X player won
      -1 - O player won
       2 - drawn
       0 - game still goes on
    */
// -==-=-=-=-= isGameOver =-=-=-=-=-=-=-=-=-=-=-=-=-
    public int isGameOver() {
        return Check(X) ? 1 : Check(O) ? -1 : ((X | O) & 511) == 511 ? 2 : 0;
    }
}
