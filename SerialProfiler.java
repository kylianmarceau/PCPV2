import java.io.*;
import java.util.*;

public class SerialProfiler {
    //store the values for testing in a list
    static List<Integer> GRID_SIZES = Arrays.asList(100, 200, 500, 1000);
    static List<Double> DENSITIES  = Arrays.asList(0.05, 0.10, 0.20);
    static List<Integer> SEEDS = Arrays.asList(1, 2, 3);

    static String OUTPUT_FILE = "serial_tests_output.txt";
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
                    DungeonHunter.main(new String[]{String.valueOf(size), String.valueOf(density), String.valueOf(seed)});

                    //get end system time
                    //long end = System.nanoTime(); ---> dont need -- can access public static timers from DungeonHunter.java
                    long timeTaken = DungeonHunter.endTime - DungeonHunter.startTime;

                    fileWriter.write(size + ", " + density + ", " + seed + ", " + timeTaken + "\n");
                    System.out.printf("Ran %d %.2f %d -> %d ms%n", size, density, seed, timeTaken);
                }
            }
        }
        System.out.println("Profiling complete. Results saved to " + OUTPUT_FILE);

        fileWriter.close();
    }
}
