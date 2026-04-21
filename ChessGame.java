import java.awt.*;
import javax.swing.*;

/**
 * GUI Chess Game (PvP)
 * Features:
 * - Intro menu screen
 * - Click-to-move GUI
 * - Full piece movement rules
 * - Check detection (basic)
 * - Castling
 * - Game over screen
 */
public class ChessGame {

    static Piece[][] board;
    static JButton[][] buttons; 
    static String currentPlayer;
    static int selectedR = -1, selectedC = -1;
    static Color light = new Color(240, 217, 181);
    static Color dark = new Color(181, 136, 99);
    static Color highlight = new Color(255, 255, 100); // yellow

    static JFrame frame;
    static JPanel boardPanel;
    static String getImagePath(Piece p) {
    String color = p.color.equals("White") ? "w" : "b";

    if (p instanceof Pawn) return "chesspiece/Chess_" + color + "pawn.png";
    if (p instanceof Rook) return "chesspiece/Chess_" + color + "rook.png";
    if (p instanceof Knight) return "chesspiece/Chess_" + color + "knight.png";
    if (p instanceof Bishop) return "chesspiece/Chess_" + color + "bishop.png";
    if (p instanceof Queen) return "chesspiece/Chess_" + color + "queen.png";
    if (p instanceof King) return "chesspiece/Chess_" + color + "king.png";

    return "";
}
    static abstract class Piece {
        String color;
        boolean moved = false;

        public Piece(String color) {
            this.color = color;
        }

        abstract boolean isValidMove(Piece[][] board, int fr, int fc, int tr, int tc);

        public String toString() {
            return color.charAt(0) + "?";
        }
    }

    static class Pawn extends Piece {
        public Pawn(String color) { super(color); }
        boolean isValidMove(Piece[][] board, int fr, int fc, int tr, int tc) {
            int dir = color.equals("White") ? -1 : 1;
            if (fc == tc && board[tr][tc] == null) {
                if (tr - fr == dir) return true;
                if (!moved && tr - fr == 2 * dir && board[fr + dir][fc] == null) return true;
            }
            if (Math.abs(tc - fc) == 1 && tr - fr == dir && board[tr][tc] != null) {
                return !board[tr][tc].color.equals(color);
            }
            return false;
        }
        public String toString() { return color.charAt(0) + "P"; }
    }

    static class Rook extends Piece {
        public Rook(String color) { super(color); }
        boolean isValidMove(Piece[][] board, int fr, int fc, int tr, int tc) {
            if (fr != tr && fc != tc) return false;
            int dr = Integer.compare(tr, fr);
            int dc = Integer.compare(tc, fc);
            int r = fr + dr, c = fc + dc;
            while (r != tr || c != tc) {
                if (board[r][c] != null) return false;
                r += dr; c += dc;
            }
            return true;
        }
        public String toString() { return color.charAt(0) + "R"; }
    }

    static class Knight extends Piece {
        public Knight(String color) { super(color); }
        boolean isValidMove(Piece[][] board, int fr, int fc, int tr, int tc) {
            int dr = Math.abs(fr - tr);
            int dc = Math.abs(fc - tc);
            return (dr == 2 && dc == 1) || (dr == 1 && dc == 2);
        }
        public String toString() { return color.charAt(0) + "N"; }
    }

    static class Bishop extends Piece {
        public Bishop(String color) { super(color); }
        boolean isValidMove(Piece[][] board, int fr, int fc, int tr, int tc) {
            if (Math.abs(fr - tr) != Math.abs(fc - tc)) return false;
            int dr = Integer.compare(tr, fr);
            int dc = Integer.compare(tc, fc);
            int r = fr + dr, c = fc + dc;
            while (r != tr) {
                if (board[r][c] != null) return false;
                r += dr; c += dc;
            }
            return true;
        }
        public String toString() { return color.charAt(0) + "B"; }
    }

    static class Queen extends Piece {
        public Queen(String color) { super(color); }
        boolean isValidMove(Piece[][] board, int fr, int fc, int tr, int tc) {
            return new Rook(color).isValidMove(board, fr, fc, tr, tc) ||
                   new Bishop(color).isValidMove(board, fr, fc, tr, tc);
        }
        public String toString() { return color.charAt(0) + "Q"; }
    }

    static class King extends Piece {
        public King(String color) { super(color); }
        boolean isValidMove(Piece[][] board, int fr, int fc, int tr, int tc) {
            if (Math.abs(fr - tr) <= 1 && Math.abs(fc - tc) <= 1) return true;
            if (!moved && fr == tr && Math.abs(tc - fc) == 2) return true;
            return false;
        }
        public String toString() { return color.charAt(0) + "K"; }
    }

    static void showMenu() {
        JFrame menu = new JFrame("Chess Menu");
        menu.setSize(300, 200);
        menu.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        menu.setLayout(new BorderLayout());

        JLabel title = new JLabel("Chess Game", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 20));

        JButton start = new JButton("Start Game");
        start.addActionListener(e -> {
            menu.dispose();
            startGame();
        });

        menu.add(title, BorderLayout.CENTER);
        menu.add(start, BorderLayout.SOUTH);
        menu.setVisible(true);
    }

    static boolean hasAnyLegalMove(String color) {
    for (int fr = 0; fr < 8; fr++) {
        for (int fc = 0; fc < 8; fc++) {
            Piece p = board[fr][fc];

            if (p != null && p.color.equals(color)) {
                for (int tr = 0; tr < 8; tr++) {
                    for (int tc = 0; tc < 8; tc++) {

                        // can't capture own piece
                        if (board[tr][tc] != null &&
                            board[tr][tc].color.equals(color)) continue;

                        if (p.isValidMove(board, fr, fc, tr, tc)) {

                            // simulate move
                            Piece backup = board[tr][tc];
                            board[tr][tc] = p;
                            board[fr][fc] = null;

                            boolean stillInCheck = isInCheck(color);

                            // undo move
                            board[fr][fc] = p;
                            board[tr][tc] = backup;

                            if (!stillInCheck) return true;
                        }
                    }
                }
            }
        }
    }
    return false;
}

    static void showGameOver(String winner) {
        JFrame over = new JFrame("Game Over");
        over.setSize(300, 200);
        over.setLayout(new BorderLayout());

        JLabel msg = new JLabel(winner + " wins", SwingConstants.CENTER);
        msg.setFont(new Font("Arial", Font.BOLD, 18));

        JButton restart = new JButton("Play Again");
        restart.addActionListener(e -> {
            over.dispose();
            frame.dispose();
            showMenu();
        });

        over.add(msg, BorderLayout.CENTER);
        over.add(restart, BorderLayout.SOUTH);
        over.setVisible(true);
    }

    static void setupBoard() {
        board = new Piece[8][8];
        for (int i = 0; i < 8; i++) {
            board[1][i] = new Pawn("Black");
            board[6][i] = new Pawn("White");
        }
        board[0][0]=new Rook("Black"); board[0][7]=new Rook("Black");
        board[7][0]=new Rook("White"); board[7][7]=new Rook("White");
        board[0][1]=new Knight("Black"); board[0][6]=new Knight("Black");
        board[7][1]=new Knight("White"); board[7][6]=new Knight("White");
        board[0][2]=new Bishop("Black"); board[0][5]=new Bishop("Black");
        board[7][2]=new Bishop("White"); board[7][5]=new Bishop("White");
        board[0][3]=new Queen("Black");
        board[7][3]=new Queen("White");
        board[0][4]=new King("Black");
        board[7][4]=new King("White");
        currentPlayer = "White";
    }

    static void startGame() {
        setupBoard();

        frame = new JFrame("Chess Game");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 600);

        boardPanel = new JPanel(new GridLayout(8, 8));
        buttons = new JButton[8][8];

        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                JButton btn = new JButton();
                btn.setFocusable(false);

                // 🔥 THESE TWO LINES FIX YOUR ISSUE
                btn.setOpaque(true);
                btn.setBorderPainted(false);

                Color light = new Color(240, 217, 181);
                Color dark = new Color(181, 136, 99);

                if ((r + c) % 2 == 0)
                    btn.setBackground(light);
                else
                    btn.setBackground(dark);

                btn.setFont(new Font("Arial", Font.BOLD, 20));

                int rr = r, cc = c;
                btn.addActionListener(e -> handleClick(rr, cc));

                buttons[r][c] = btn;
                boardPanel.add(btn);
                btn.setFocusPainted(false);
            }
        }

        updateBoard();
        frame.add(boardPanel);
        frame.setVisible(true);
    }

    static void updateBoard() {
    for (int r = 0; r < 8; r++) {
        for (int c = 0; c < 8; c++) {

            if ((r + c) % 2 == 0)
                buttons[r][c].setBackground(light);
            else
                buttons[r][c].setBackground(dark);
            if (r == selectedR && c == selectedC) {
                buttons[r][c].setBackground(highlight);
            }

            buttons[r][c].setText("");

            if (board[r][c] == null) {
                buttons[r][c].setIcon(null);
            } else {
                String path = getImagePath(board[r][c]);
                ImageIcon icon = new ImageIcon(path);
                Image img = icon.getImage().getScaledInstance(60, 60, Image.SCALE_SMOOTH);
                buttons[r][c].setIcon(new ImageIcon(img));
            }
        }
    }
}

    static boolean isInCheck(String color) {
        int kr=-1,kc=-1;
        for(int r=0;r<8;r++)for(int c=0;c<8;c++){
            Piece p=board[r][c];
            if(p instanceof King && p.color.equals(color)){kr=r;kc=c;}
        }
        String enemy=color.equals("White")?"Black":"White";
        for(int r=0;r<8;r++)for(int c=0;c<8;c++){
            Piece p=board[r][c];
            if(p!=null && p.color.equals(enemy)){
                if(p.isValidMove(board,r,c,kr,kc))return true;
            }
        }
        return false;
    }

    static void handleClick(int r,int c){

        if(selectedR==r&&selectedC==c){selectedR=selectedC=-1;return;}

        if(selectedR==-1){
            Piece p=board[r][c];
            if(p!=null&&p.color.equals(currentPlayer)){
                selectedR = r;
                selectedC = c;
                updateBoard();
            }
            return;
        }

        Piece p=board[selectedR][selectedC];
        Piece backup=board[r][c];

        if(p==null){selectedR=selectedC=-1;return;}

        if(backup!=null&&backup.color.equals(currentPlayer)){
            selectedR=selectedC=-1;return;
        }

        if(!p.isValidMove(board,selectedR,selectedC,r,c)){
            selectedR=selectedC=-1;return;
        }

        if(p instanceof King && Math.abs(c-selectedC)==2){
            if(c==6){board[r][5]=board[r][7];board[r][7]=null;}
            else{board[r][3]=board[r][0];board[r][0]=null;}
        }

        board[r][c]=p;
        board[selectedR][selectedC]=null;

        if(isInCheck(currentPlayer)){
            board[selectedR][selectedC]=p;
            board[r][c]=backup;
            selectedR=selectedC=-1;
            return;
        }

        p.moved=true;

        String opponent = currentPlayer.equals("White") ? "Black" : "White";

        if (isInCheck(opponent) && !hasAnyLegalMove(opponent)) {
            updateBoard();
            showGameOver(currentPlayer); // winner
            return;
        }

        currentPlayer=currentPlayer.equals("White")?"Black":"White";
        selectedR=selectedC=-1;
        updateBoard();
    }

    public static void main(String[] args){
        showMenu();
    }
}
