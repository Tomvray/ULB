# Define the compiler
CC = gcc

# Define the flags
CFLAGS = -Wall -g

# Define the target executable
TARGET = pfsp

# Define the source files
SRCS = main.c Utils.c Algorithms.c Initial_solution.c

# Define the object files
OBJS = $(SRCS:.c=.o)

# Default target
all: $(TARGET)

# Link the object files to create the executable
$(TARGET): $(OBJS)
	$(CC) $(CFLAGS) -o $(TARGET) $(OBJS)

# Compile the source files into object files
%.o: %.c
	$(CC) $(CFLAGS) -c $< -o $@

#Test
test: $(TARGET)
	./my_program --first --insert --random-init 
# Clean up the build files
clean:
	rm -f $(OBJS)

# Clean up the build files and the executable
fclean: clean
	rm -f $(TARGET)

# Rebuild the executable
re: fclean all

# Phony targets
.PHONY: all clean