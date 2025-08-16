import java.io.*;
import java.util.Arrays;

public class ValidationScript {
    static int[] GRID_SIZES = {50, 100, 200};
    static double[] DENSITIES = {0.05, 0.10, 0.20 };
    static int[] SEEDS  = {0, 1, 2, 3, 42, 100};

    public static void main(String[] args) {
        validateCorrectness();
    }

    public static void validateCorrectness(){
        int totalTests = 0;
        int passedTests = 0;

        for (int gridSize : GRID_SIZES){
            for (double density : DENSITIES){
                for (int seed : SEEDS){
                    totalTests++;
                    System.out.printf("Test %d: Grid=%d, Density=%.2f, Seed=%d\n", totalTests, gridSize, density, seed);
                    
                    // RUN SERIAL
                    SerialResult serialResult = runSerial(gridSize, density, seed);

                    // RUN PARALLEL
                    ParallelResult parallelResult = runParallel(gridSize, density, seed);

                    // COMPARE results and see if the parallel implementation passes the tests
                    boolean testPassed = compareResults(serialResult, parallelResult);

                    if (testPassed){
                        passedTests ++;
                        System.out.println("=======✅✅TEST PASSED✅✅=======");
                    }
                    else{
                        System.out.println("=======❌❌TEST FAILED❌❌=======");
                        System.out.printf("    Serial:   Mana=%d, X=%.1f, Y=%.1f\n", 
                                        serialResult.mana, serialResult.x, serialResult.y);
                        System.out.printf("    Parallel: Mana=%d, X=%.1f, Y=%.1f\n", 
                                        parallelResult.mana, parallelResult.x, parallelResult.y);
                    }
                    System.out.println();

                }
            }
        }
        System.out.printf("=== VALIDATION RESULTS: %d/%d test passed ===\n", passedTests, totalTests);
        if (passedTests == totalTests){
            System.out.println("🎉 ALL TESTS PASSED");
        }
        else{
            System.out.println("⚠️ SOME TESTS FAILED");
        }
    }

    private static SerialResult runSerial(int gridSize, double density, int seed){

        // ADD TRY
        try{
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream originalOutput = System.out;
            System.setOut(new PrintStream(baos));
    
            // run the serial evrsion
            DungeonHunter.main(new String[]{String.valueOf(gridSize), String.valueOf(density), String.valueOf(seed)});
    
            System.setOut(originalOutput);
            String output = baos.toString();
    
            return parseSerialOutput(output);
        } catch (Exception e) {
            System.err.println("Error running parallel version: " + e.getMessage());
            return new SerialResult(Integer.MIN_VALUE, 0.0, 0.0);
        }

        
    }

    private static ParallelResult runParallel(int gridSize, double density, int seed){

        // EDIT --> add try catch otherwise- constant error when running
        try{
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream originalOutput = System.out;
            System.setOut(new PrintStream(baos));

            // run the parallel evrsion
            DungeonHunterParallel.main(new String[]{String.valueOf(gridSize), String.valueOf(density), String.valueOf(seed)});

            System.setOut(originalOutput);
            String output = baos.toString();

            return parseParallelOutput(output);
        } catch (Exception e) {
            System.err.println("Error running parallel version: " + e.getMessage());
            return new ParallelResult(Integer.MIN_VALUE, 0.0, 0.0);
        }
        
    }

    private static SerialResult parseSerialOutput(String output){
    // Parse the line: "Dungeon Master (mana XXXXX) found at: x=XX.X y=XX.X"
        try{
            String[] lines = output.split("\n");
            System.out.println("SERIAL =====> "+Arrays.toString(lines));
            for (String line : lines){
                if (line.contains("Dungeon Master") && line.contains("found at:")){
                    // GET MANA VALUE
                    int manaStart = line.indexOf("(mana ") + 6;
                    int manaEnd = line.indexOf(")", manaStart);
                    int mana = Integer.parseInt(line.substring(manaStart, manaEnd));

                    // GET X COORDS
                    int xstart = line.indexOf("x=")+2;
                    int xend = line.indexOf(" ", xstart);
                    double x = Double.parseDouble(line.substring(xstart, xend));

                    //GET Y CORDS
                    int ystart = line.indexOf("y=") + 2;
                    
                    double y = Double.parseDouble(line.substring(ystart).trim());

                    return new SerialResult(mana, x, y);
                }
            }
        } catch (Exception e) {
            System.err.println("Error parsing serial output: " + e.getMessage());
        }
        return new SerialResult(Integer.MIN_VALUE, 0.0, 0.0);

    }

    private static ParallelResult parseParallelOutput(String output){
        try{
            String[] lines = output.split("\n");
            System.out.println("PARALLEL ======>"+Arrays.toString(lines));
            for (String line : lines){
                if (line.contains("Dungeon Master") && line.contains("found at:")){
                    // GET MANA VALUE
                    int manaStart = line.indexOf("(mana ") + 6;
                    int manaEnd = line.indexOf(")", manaStart);
                    int mana = Integer.parseInt(line.substring(manaStart, manaEnd));

                    // GET X COORDS
                    int xstart = line.indexOf("x=") +2;
                    int xend = line.indexOf(" ", xstart);
                    double x = Double.parseDouble(line.substring(xstart, xend));

                    //GET Y CORDS
                    int ystart = line.indexOf("y=") + 2;
                    
                    double y = Double.parseDouble(line.substring(ystart).trim());

                    return new ParallelResult(mana, x, y);
                }
            }
        } catch (Exception e) {
            System.err.println("Error parsing parallel output: " + e.getMessage());
        }
        return new ParallelResult(Integer.MIN_VALUE, 0.0, 0.0);
        
    }

    private static boolean compareResults(SerialResult serial, ParallelResult parallel){
        return serial.mana == parallel.mana &&  Math.abs(serial.x - parallel.x) < 0.01 && Math.abs(serial.y - parallel.y) < 0.01;
    }

    

    // make helper methods and classes to store results
    static class SerialResult{
        int mana;
        double x;
        double y;

        SerialResult(int mana, double x, double y){
            this.mana = mana;
            this.x = x;
            this.y = y;
        }
    }

    static class ParallelResult{
        int mana;
        double x;
        double y;

        ParallelResult(int mana, double x, double y){
            this.mana = mana;
            this.x = x;
            this.y = y;
        }
    }
}
