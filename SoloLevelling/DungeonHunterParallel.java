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
 import java.util.concurrent.RecursiveTask;
 
 class DungeonHunterParallel{
     static final boolean DEBUG=false;
 
     //timers for how long it all takes
     static long startTime = 0;
     static long endTime = 0;
     private static void tick() {startTime = System.currentTimeMillis(); }
     private static void tock(){endTime=System.currentTimeMillis(); }

     // Compute an adaptive threshold so that the fork/join splits create roughly
     // (parallelism * tasksPerWorker) leaf tasks. Overrides:
     //   -Ddh.threshold=<int> (forces threshold)
     //   -Ddh.tasksPerWorker=<int> (default 8, clamped [4,16])
     //   -Ddh.smallWorkFactor=<int> (default 2, clamped [1,4]); if
     //        numSearches <= parallelism * smallWorkFactor then avoid splitting
     private static int computeOptimalThreshold(int numSearches, ForkJoinPool pool) {
         String override = System.getProperty("dh.threshold");
         if (override != null) {
             try {
                 int value = Integer.parseInt(override.trim());
                 if (value > 0) return Math.min(numSearches, value);
             } catch (NumberFormatException ignored) { }
         }

         int parallelism = Math.max(1, pool.getParallelism());

         int tasksPerWorker = 8;
         String tpw = System.getProperty("dh.tasksPerWorker");
         if (tpw != null) {
             try {
                 tasksPerWorker = Integer.parseInt(tpw.trim());
             } catch (NumberFormatException ignored) { }
         }
         // clamp to a sensible range
         if (tasksPerWorker < 4) tasksPerWorker = 4;
         if (tasksPerWorker > 16) tasksPerWorker = 16;

         int smallWorkFactor = 2;
         String swf = System.getProperty("dh.smallWorkFactor");
         if (swf != null) {
             try {
                 smallWorkFactor = Integer.parseInt(swf.trim());
             } catch (NumberFormatException ignored) { }
         }
         if (smallWorkFactor < 1) smallWorkFactor = 1;
         if (smallWorkFactor > 4) smallWorkFactor = 4;

         // For tiny workloads, do not create many tasks; execute directly
         if (numSearches <= parallelism * smallWorkFactor) {
             return Math.max(1, numSearches);
         }

         int targetTaskCount = Math.max(1, parallelism * tasksPerWorker);
         int threshold = (int) Math.ceil((double) numSearches / targetTaskCount);
         // Clamp threshold into [1, numSearches]
         if (threshold < 1) threshold = 1;
         if (threshold > numSearches) threshold = numSearches;
         return threshold;
     }
 
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
            int adaptiveThreshold = computeOptimalThreshold(numSearches, pool);
            if (DEBUG) {
                int leafTasks = (int) Math.ceil(numSearches * 1.0 / Math.max(1, adaptiveThreshold));
                System.out.println("Adaptive threshold: " + adaptiveThreshold +
                                   " (parallelism " + pool.getParallelism() +
                                   ", leaf tasks " + leafTasks + ")");
            }
            tick(); // STRAT timer

            SearchTask mainTask = new SearchTask(searches, 0, numSearches, adaptiveThreshold);
            SearchResult result = pool.invoke(mainTask);

            int max = result.max;
            int finder = result.finder;

            pool.shutdown();

            tock();

            System.out.printf("\t dungeon size: %d,\n", gateSize);
            System.out.printf("\t rows: %d, columns: %d\n", dungeonRows, dungeonColumns);
            System.out.printf("\t x: [%f, %f], y: [%f, %f]\n", xmin, xmax, ymin, ymax );
            System.out.printf("\t Number searches: %d\n", numSearches );
    
            /*  Total computation time */
            System.out.printf("\n\t time: %d ms\n",endTime - startTime );
            int tmp=dungeon.getGridPointsEvaluated();
            System.out.printf("\tnumber dungeon grid points evaluated: %d  (%2.0f%s)\n",tmp,(tmp*1.0/(dungeonRows*dungeonColumns*1.0))*100.0, "%");
    
            /* Results*/
            System.out.printf("Dungeon Master (mana %d) found at:  ", max );
            System.out.printf("x=%.1f y=%.1f\n\n",dungeon.getXcoord(searches[finder].getPosRow()), dungeon.getYcoord(searches[finder].getPosCol()) );
            dungeon.visualisePowerMap("visualiseSearch.png", false);
            dungeon.visualisePowerMap("visualiseSearchPath.png", true);
    }

    static class SearchTask extends RecursiveTask<SearchResult> {
        private HuntParallel[] searches;
        private int startIndex, endIndex;
        private int threshold; // Minimum work unit size for base case

        public SearchTask(HuntParallel[] searches, int start, int end, int threshold) {
            this.searches = searches;
            this.startIndex = start;
            this.endIndex = end;
            this.threshold = Math.max(1, threshold);
        }

        @Override
        protected SearchResult compute() {
            int workSize = endIndex - startIndex;
            
            if (workSize <= threshold) {
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
                SearchTask leftTask = new SearchTask(searches, startIndex, mid, threshold);
                SearchTask rightTask = new SearchTask(searches, mid, endIndex, threshold);
                
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

    static class SearchResult {
        int max;
        int finder;
        
        public SearchResult(int max, int finder) {
            this.max = max;
            this.finder = finder;
        }
    }
}