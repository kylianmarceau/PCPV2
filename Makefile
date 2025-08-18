JAVAC=javac
JAVA=java
SRC=SoloLevelling

# Serial version classes (in SoloLevelling directory)
SERIAL_CLASSES = $(SRC)/DungeonMap.java $(SRC)/Hunt.java $(SRC)/DungeonHunter.java

# Parallel version classes (in SoloLevelling directory)
PARALLEL_CLASSES = $(SRC)/DungeonMapParallel.java $(SRC)/HuntParallel.java $(SRC)/DungeonHunterParallel.java

# Profiler classes
PROFILER_CLASSES = SerialProfiler.java ParallelProfiler.java

# Default arguments
ARGS ?= 20 0.2 0

all: serial parallel profilers

serial:
	$(JAVAC) $(SERIAL_CLASSES)

parallel:
	$(JAVAC) $(PARALLEL_CLASSES)

profilers:
	$(JAVAC) -cp $(SRC) $(PROFILER_CLASSES)

# Run serial version
run:
	$(JAVAC) $(SERIAL_CLASSES)
	$(JAVA) -cp $(SRC) DungeonHunter $(ARGS)

# Run parallel version
run-parallel:
	$(JAVAC) $(PARALLEL_CLASSES)
	$(JAVA) -cp $(SRC) DungeonHunterParallel $(ARGS)

# Run serial profiler
profile-serial:
	$(JAVAC) $(SERIAL_CLASSES)
	$(JAVAC) -cp $(SRC) SerialProfiler.java
	$(JAVA) -cp .:$(SRC) SerialProfiler

# Run parallel profiler
profile-parallel:
	$(JAVAC) $(PARALLEL_CLASSES)
	$(JAVAC) -cp $(SRC) ParallelProfiler.java
	$(JAVA) -cp .:$(SRC) ParallelProfiler

# Run both profilers
profile-both: profile-serial profile-parallel

clean:
	rm -f $(SRC)/*.class *.class
	rm -rf ProfileOutputs

# Test both versions
test-both:
	@echo "=== Serial Version ==="
	$(JAVAC) $(SERIAL_CLASSES)
	$(JAVA) -cp $(SRC) DungeonHunter $(ARGS)
	@echo ""
	@echo "=== Parallel Version ==="
	$(JAVAC) $(PARALLEL_CLASSES)
	$(JAVA) -cp $(SRC) DungeonHunterParallel $(ARGS)

.PHONY: all serial parallel profilers run run-parallel profile-serial profile-parallel profile-both clean test-both