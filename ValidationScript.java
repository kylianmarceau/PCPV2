import java.io.*;

public class ValidationScript {
    static int[] GRID_SIZES = {50, 100, 200, 500};
    static double[] DENSITIES = {0.05, 0.10, 0.20 };
    static int[] SEEDS  = {1, 2, 3, 42, 100};

    public static void validateCorrectness(){
        int totalTests = 0;
        int passedTests = 0;

        for (int gridSize : GRID_SIZES){
            for (double density : DENSITIES){
                for (int seed : SEEDS){
                    totalTests++;
                    System.out.printf("Test %d: Grid=%d, Density=%.2f, Seed=%d\n", totalTests, gridSize, density, seed);
                    
                }
            }
        }
    }

    private static SerialResult runSerial(int gridSize, double density, int seed){
        try{

        }
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
