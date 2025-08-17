/* Solo-levelling Hunt for Dungeon Master
 * Reference sequential version 
 * Michelle Kuttel 2025, University of Cape Town
 * author of original Java code adapted with assistance from chatGPT for reframing 
 * and complex power - "mana" - function.
 * Inspired by  "Hill Climbing with Montecarlo"
 * EduHPC'22 Peachy Assignment developed by Arturo Gonzalez Escribano  (Universidad de Valladolid 2021/2022)
 */
/**
 * DungeonHunter.java
 *
 * Main driver for the Dungeon Hunter assignment.
 * This program initializes the dungeon map and performs a series of searches
 * to locate the global maximum.
 *
 * Usage:
 *   java DungeonHunter <gridSize> <numSearches> <randomSeed>
 *
 */

 import java.util.Random; //for the random search locations

 // ADD -> MUST USE FORK?JOIN
 import java.util.concurrent.ForkJoinPool;
 import java.util.concurrent.RecursiveAction;

 class DungeonHunterParallel{
     static final boolean DEBUG=false;
 
     //timers for how long it all takes
     static long startTime = 0;
     static long endTime = 0;
     private static void tick() {startTime = System.currentTimeMillis(); }
     private static void tock(){endTime=System.currentTimeMillis(); }
 
     public static void main(String[] args)  {
         
         double xmin, xmax, ymin, ymax; //dungeon limits - dungeons are square
         DungeonMapParallel dungeon;  //object to store the dungeon as a grid
         
          int numSearches=10, gateSize= 10;		
         HuntParallel [] searches;		// Array of searches
   
         Random rand = new Random();  //the random number generator
           int randomSeed=0;  //set seed to have predictability for testing
         
         if (args.length!=3) {
             System.out.println("Incorrect number of command line arguments provided.");
             System.exit(0);
         }
         
         
         /* Read argument values */
           try {
         gateSize=Integer.parseInt( args[0] );
          if (gateSize <= 0) {
              throw new IllegalArgumentException("Grid size must be greater than 0.");
          }
         
         numSearches = (int) (Double.parseDouble(args[1])*(gateSize*2)*(gateSize*2)*DungeonMapParallel.RESOLUTION);
         
         randomSeed=Integer.parseInt( args[2] );
         if (randomSeed < 0) {
                 throw new IllegalArgumentException("Random seed must be non-negative.");
             }
         else if(randomSeed>0)  rand = new Random(randomSeed);  // BUG FIX
         } catch (NumberFormatException e) {
             System.err.println("Error: All arguments must be numeric.");
             System.exit(1);
         } catch (IllegalArgumentException e) {
             System.err.println("Error: " + e.getMessage());
             System.exit(1);
         }
  
           
         xmin =-gateSize;
         xmax = gateSize;
         ymin = -gateSize;
         ymax = gateSize;
         dungeon = new DungeonMapParallel(xmin,xmax,ymin,ymax,randomSeed); // Initialize dungeon
         
         int dungeonRows=dungeon.getRows();
         int dungeonColumns=dungeon.getColumns();
          searches= new HuntParallel [numSearches];
          
 
 
         for (int i=0;i<numSearches;i++)  //intialize searches at random locations in dungeon
             searches[i]=new HuntParallel(i+1, rand.nextInt(dungeonRows),
                     rand.nextInt(dungeonColumns),dungeon);

        //----------------------parallel implementation FORK JOIN------------------------------------
        // USE FORK JOIN replacement
        ForkJoinPool pool = new ForkJoinPool();
        tick(); // STRAT timer

        SearchTask mainTask = new SearchTask

        static class SearchTask extends RecursiveTask<SearchResult> {
            private HuntParallel[] searches;
            private int startIndex, endIndex;
            private static final int THRESHOLD = 10; // Minimum work unit size
   
            public SearchTask(HuntParallel[] searches, int start, int end) {
                this.searches = searches;
                this.startIndex = start;
                this.endIndex = end;
            }
   
            @Override
            protected SearchResult compute() {
                int workSize = endIndex - startIndex;
                
                if (workSize <= THRESHOLD) {
                    // Base case: do the work directly
                    int localMax = Integer.MIN_VALUE;
                    int localFinder = -1;
                    
                    for (int i = startIndex; i < endIndex; i++) {
                        int result = searches[i].findManaPeak();
                        if (result > localMax) {
                            localMax = result;
                            localFinder = i;
                        }
                        if (DEBUG) {
                            System.out.println("Task: Shadow " + searches[i].getID() + 
                                             " finished at " + result + " in " + searches[i].getSteps());
                        }
                    }
                    return new SearchResult(localMax, localFinder);
                } else {
                    // Recursive case: split the work
                    int mid = startIndex + workSize / 2;
                    SearchTask leftTask = new SearchTask(searches, startIndex, mid);
                    SearchTask rightTask = new SearchTask(searches, mid, endIndex);
                    
                    // Fork the left task to run in parallel
                    leftTask.fork();
                    
                    // Compute the right task in current thread
                    SearchResult rightResult = rightTask.compute();
                    
                    // Join (wait for) the left task
                    SearchResult leftResult = leftTask.join();
                    
                    // Combine results
                    if (leftResult.max > rightResult.max) {
                        return leftResult;
                    } else {
                        return rightResult;
                    }
                }
            }
        }


        //NEW helper to store results
        static class SearchResult {
            int max;
            int finder;
            
            public SearchResult(int max, int finder) {
                this.max = max;
                this.finder = finder;
            }
        }
     }
}