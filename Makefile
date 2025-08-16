JAVAC=javac
JAVA=java
SRC=SoloLevelling

# Serial version classes (in SoloLevelling directory)
SERIAL_CLASSES = $(SRC)/DungeonMap.java $(SRC)/Hunt.java $(SRC)/DungeonHunter.java

# Parallel version classes (in SoloLevelling directory)
PARALLEL_CLASSES = $(SRC)/DungeonMapParallel.java $(SRC)/HuntParallel.java $(SRC)/DungeonHunterParallel.java

# Default arguments
ARGS ?= 20 0.2 0

all: serial parallel

serial:
	$(JAVAC) $(SERIAL_CLASSES)

parallel:
	$(JAVAC) $(PARALLEL_CLASSES)

# Run serial version
run:
	$(JAVAC) $(SERIAL_CLASSES)
	$(JAVA) -cp $(SRC) DungeonHunter $(ARGS)

# Run parallel version
run-parallel:
	$(JAVAC) $(PARALLEL_CLASSES)
	$(JAVA) -cp $(SRC) DungeonHunterParallel $(ARGS)

clean:
	rm -f $(SRC)/*.class

# Test both versions
test-both:
	@echo "=== Serial Version ==="
	$(JAVAC) $(SERIAL_CLASSES)
	$(JAVA) -cp $(SRC) DungeonHunter $(ARGS)
	@echo ""
	@echo "=== Parallel Version ==="
	$(JAVAC) $(PARALLEL_CLASSES)
	$(JAVA) -cp $(SRC) DungeonHunterParallel $(ARGS)