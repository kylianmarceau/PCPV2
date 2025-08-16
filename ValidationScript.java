import java.io.*;
import java.util.Arrays;

public class ValidationScript {
    static int[] GRID_SIZES = {50, 100, 200, 300, 400, 500, 600, 700};
    static double[] DENSITIES = {0.05, 0.10, 0.20};
    static int[] SEEDS  = {1, 2, 3, 42, 100};

    // For storing timing results
    private static StringBuilder timingResults = new StringBuilder();

    public static void main(String[] args) {
        validateCorrectness();
    }

    public static void validateCorrectness(){
        int totalTests = 0;
        int passedTests = 0;

        // Initialize the timing results file with headers
        initializeTimingFile();

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
                    
                    // Add timing data to results
                    addTimingData(totalTests, gridSize, density, seed, serialResult.time, parallelResult.time, testPassed);
                }
            }
        }
        
        // Write timing results to file
        writeTimingToFile();

        System.out.printf("=== VALIDATION RESULTS: %d/%d test passed ===\n", passedTests, totalTests);
        if (passedTests == totalTests){
            System.out.println("🎉 ALL TESTS PASSED");
        }
        else{
            System.out.println("⚠️ SOME TESTS FAILED");
        }
        System.out.println("Timing results written to 'timing_results.txt'");
    }

    private static void initializeTimingFile() {
        timingResults.append("Grid_Size, Density, Seed, Serial_Time_ms, Parallel_Time_ms\n");
    }

    private static void addTimingData(int testNum, int gridSize, double density, int seed, 
                                     int serialTime, int parallelTime, boolean passed) {
        timingResults.append(String.format("%d, %.2f, %d, %d, %d\n",
                gridSize, density, seed, serialTime, parallelTime));
    }

    private static void writeTimingToFile() {
        try (PrintWriter writer = new PrintWriter(new FileWriter("timing_results.txt"))) {
            writer.print(timingResults.toString());
            
        } catch (IOException e) {
            System.err.println("Error writing timing results to file: " + e.getMessage());
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
            System.err.println("Error running serial version: " + e.getMessage());
            return new SerialResult(Integer.MIN_VALUE, 0.0, 0.0, 0);
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
            return new ParallelResult(Integer.MIN_VALUE, 0.0, 0.0, 0);
        }
        
    }

    private static SerialResult parseSerialOutput(String output){
    // Parse the line: "Dungeon Master (mana XXXXX) found at: x=XX.X y=XX.X"
        try{
            String[] lines = output.split("\n");
            System.out.println("SERIAL =====> "+Arrays.toString(lines));
            int mana = Integer.MIN_VALUE;
            double x = 0.0, y = 0.0;
            int time = 0;
            
            for (String line : lines){
                if (line.contains("Dungeon Master") && line.contains("found at:")){
                    // GET MANA VALUE
                    int manaStart = line.indexOf("(mana ") + 6;
                    int manaEnd = line.indexOf(")", manaStart);
                    mana = Integer.parseInt(line.substring(manaStart, manaEnd));

                    // GET X COORDS
                    int xstart = line.indexOf("x=")+2;
                    int xend = line.indexOf(" ", xstart);
                    x = Double.parseDouble(line.substring(xstart, xend));

                    //GET Y CORDS
                    int ystart = line.indexOf("y=") + 2;
                    y = Double.parseDouble(line.substring(ystart).trim());
                }
                
                // Extract time from the output
                if (line.contains("time:") && line.contains("ms")){
                    int timeStart = line.indexOf("time:") + 5;
                    int timeEnd = line.indexOf("ms", timeStart);
                    String timeStr = line.substring(timeStart, timeEnd).trim();
                    time = Integer.parseInt(timeStr);
                }
            }
            return new SerialResult(mana, x, y, time);
        } catch (Exception e) {
            System.err.println("Error parsing serial output: " + e.getMessage());
        }
        return new SerialResult(Integer.MIN_VALUE, 0.0, 0.0, 0);

    }

    private static ParallelResult parseParallelOutput(String output){
        try{
            String[] lines = output.split("\n");
            System.out.println("PARALLEL ======>"+Arrays.toString(lines));
            int mana = Integer.MIN_VALUE;
            double x = 0.0, y = 0.0;
            int time = 0;
            
            for (String line : lines){
                if (line.contains("Dungeon Master") && line.contains("found at:")){
                    // GET MANA VALUE
                    int manaStart = line.indexOf("(mana ") + 6;
                    int manaEnd = line.indexOf(")", manaStart);
                    mana = Integer.parseInt(line.substring(manaStart, manaEnd));

                    // GET X COORDS
                    int xstart = line.indexOf("x=") +2;
                    int xend = line.indexOf(" ", xstart);
                    x = Double.parseDouble(line.substring(xstart, xend));

                    //GET Y CORDS
                    int ystart = line.indexOf("y=") + 2;
                    y = Double.parseDouble(line.substring(ystart).trim());
                }
                
                // Extract time from the output
                if (line.contains("time:") && line.contains("ms")){
                    int timeStart = line.indexOf("time:") + 5;
                    int timeEnd = line.indexOf("ms", timeStart);
                    String timeStr = line.substring(timeStart, timeEnd).trim();
                    time = Integer.parseInt(timeStr);
                }
            }
            return new ParallelResult(mana, x, y, time);
        } catch (Exception e) {
            System.err.println("Error parsing parallel output: " + e.getMessage());
        }
        return new ParallelResult(Integer.MIN_VALUE, 0.0, 0.0, 0);
        
    }

    private static boolean compareResults(SerialResult serial, ParallelResult parallel){
        return serial.mana == parallel.mana &&  Math.abs(serial.x - parallel.x) < 0.01 && Math.abs(serial.y - parallel.y) < 0.01;
    }

    

    // make helper methods and classes to store results
    static class SerialResult{
        int mana;
        double x;
        double y;
        int time;

        SerialResult(int mana, double x, double y, int time){
            this.mana = mana;
            this.x = x;
            this.y = y;
            this.time = time;
        }
    }

    static class ParallelResult{
        int mana;
        double x;
        double y;
        int time;

        ParallelResult(int mana, double x, double y, int time){
            this.mana = mana;
            this.x = x;
            this.y = y;
            this.time = time;
        }
    }
}