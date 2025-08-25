import java.io.*;
import java.util.*;

public class SerialProfiler {
    //store the values for testing in a list
    static List<Integer> GRID_SIZES = Arrays.asList(20, 50, 100, 300, 500);
    static List<Double> DENSITIES  = Arrays.asList(0.05, 0.10, 0.20, 0.30, 0.50, 0.60, 0.70, 0.80, 0.90);
    static List<Integer> SEEDS = Arrays.asList(42);

    static String OUTPUT_FILE = "ProfileOutputs/serial_tests_output.txt";
    public static void main(String[] args) throws IOException {
        profile();

    }

    public static void profile()throws IOException{

        // NEW FOLDER
        File profileDir = new File("ProfileOutputs"); // ADD TO FOLDER 
        if (!profileDir.exists()){
            //boolean createdFolder = profileDir.mkdirs();
            System.out.println("Created outputs directory");
        }

        FileWriter fileWriter = new FileWriter(OUTPUT_FILE);
        fileWriter.write("Grid_Size, Density, Seed, Time_ms\n");

        for(int i = 0; i <GRID_SIZES.size(); i++){
            int size = GRID_SIZES.get(i);
            for(int x = 0; x<DENSITIES.size(); x++){
                double density = DENSITIES.get(x);
                for(int y = 0; y < SEEDS.size(); y++){
                    int seed = SEEDS.get(y);

                    //get start op time
                    //long start = System.nanoTime(); ---> dont need -- can access public static timers from DungeonHunter.java
                   // DungeonHunter.main(new String[]{String.valueOf(size), String.valueOf(density), String.valueOf(seed)});

                    //get end system time
                    //long end = System.nanoTime(); ---> dont need -- can access public static timers from DungeonHunter.java
                    //long timeTaken = DungeonHunter.endTime - DungeonHunter.startTime;

                    //add more runs rather then average -- rather than just one run
                    int numberOfRuns = 3;
                    int totalTime = 0;

                    for(int run = 0; run < numberOfRuns; run++){
                        // Clear the static variables before each run to ensure clean state
                        DungeonHunter.startTime = 0;
                        DungeonHunter.endTime = 0;
                        
                        // Capture start time immediately before the call
                        long profilerStartTime = System.currentTimeMillis();
                        
                        DungeonHunter.main(new String[] {String.valueOf(size), String.valueOf(density), String.valueOf(seed)});
                        
                        // Capture end time immediately after the call
                        long profilerEndTime = System.currentTimeMillis();
                        
                        // Use internal timing if available and valid, otherwise fall back to profiler timing
                        long runTime;
                        if (DungeonHunter.endTime > 0 && DungeonHunter.startTime > 0 && 
                            DungeonHunter.endTime >= DungeonHunter.startTime) {
                            runTime = DungeonHunter.endTime - DungeonHunter.startTime;
                        } else {
                            runTime = profilerEndTime - profilerStartTime;
                            System.out.println("Warning: Using fallback timing for run " + (run + 1) + 
                                             " (Internal: " + DungeonHunter.startTime + "-" + DungeonHunter.endTime + ")");
                        }
                        
                        totalTime += runTime;
                    }
                    long averageTime = totalTime/numberOfRuns;

                    fileWriter.write(size + ", " + density + ", " + seed + ", " + averageTime + "\n");
                    System.out.printf("Ran %d %.2f %d -> %d ms%n", size, density, seed, averageTime);
                }
            }
        }
        System.out.println("Profiling complete. Results saved to " + OUTPUT_FILE);

        fileWriter.close();
    }
}