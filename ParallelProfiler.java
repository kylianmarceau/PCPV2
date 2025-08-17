import java.io.*;
import java.util.*;

public class ParallelProfiler {
    //store the values for testing in a list
    static List<Integer> GRID_SIZES = Arrays.asList(10, 25, 50, 150, 200, 250);
    static List<Double> DENSITIES  = Arrays.asList(0.05, 0.10, 0.20, 0.30);
    static List<Integer> SEEDS = Arrays.asList(1, 2, 3);

    static String OUTPUT_FILE = "parallel_tests_output.txt";
    public static void main(String[] args) throws IOException {
        profile();
    }

    public static void profile()throws IOException{
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
                        DungeonHunterParallel.main(new String[] {String.valueOf(size), String.valueOf(density), String.valueOf(seed)});
                        totalTime += (DungeonHunterParallel.endTime - DungeonHunterParallel.startTime);
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
