public class App {

    /*
     * This is used to prevent conflict with Replit IDE, which used Linux as its based
     */
    public static String OS = System.getProperty("os.name").toLowerCase();
    public static String source = OS.equals("linux") ? "src" : ".";

    public static void main(String[] args) throws Exception {
        // Run the main program
        new Frame("Board");

        // Calculate the needed data for move generation
        Piece.computeData();
    }
}