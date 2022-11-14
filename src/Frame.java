import javax.swing.JFrame;

public class Frame extends JFrame {

    // Simple setup for the JFrame
    Frame(String title) {
        Board board = new Board(400, 400);

        this.setTitle(title);
        this.add(board);
        this.pack();
        this.setLocationRelativeTo(null);
        this.setResizable(false);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);
    }
}