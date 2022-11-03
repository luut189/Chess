public class App {

    public static String OS = System.getProperty("os.name").toLowerCase();
    public static String source = OS.equals("linux") ? "src" : ".";
    
    public static void main(String[] args) throws Exception {
        new Frame("Board");
        Piece.computeData();
    }
}