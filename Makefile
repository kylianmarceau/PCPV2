
JAVAC=javac
JAVA=java
SRC=SoloLevelling
CLASSES_SERIAL = $(SRC)/DungeonMap.java $(SRC)/Hunt.java $(SRC)/DungeonHunter.java

# update so makefile can run serialVersion
CLASSES_PARALLEL = $(SRC)/DungeoMapParallel.java $(SRC)/HuntParallel.java $(SRC)/DungeonHunterParallel.java
# Default arguments (update these if needed)
ARGS ?= 20 0.2 0  # Replace 'default_arguments' with your specific default arguments, if any


all: serial parallel

serial:
	$(JAVAC) $(CLASSES_SERIAL)

parallel:
	$(JAVAC) $(PARALLEL_CLASSES)
	

run: # run serial version
	$(JAVA) -cp $(SRC) DungeonHunter $(ARGS) 

run-parallel:
	$(JAVAC) $(PARALLEL_CLASSES)
	$(JAVA) -cp $(SRC) DungeonHunterParallel $(ARGS)

clean:
	rm -f $(SRC)/*.class
