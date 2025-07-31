#!/bin/bash

# Experiment script for PFSP implementation exercise

# Clean previous results
rm -f results.txt

# Define instance files (adjust paths as needed)
INSTANCES_50="../instances/Benchmarks/ta051"

# Number of runs per instance
RUNS=10

# Function to run experiments
run_experiment() {
    local instances=$1
    local size=$2
    
    echo "Running experiments for $size jobs..."
    
    # Iterative Improvement algorithms
    for init in "--random" "--rz"; do
        for rule in "--first" "--best"; do
            for neigh in "--transpose" "--exchange" "--insert"; do
                algorithm="ii${init}_${rule}_${neigh}"
                algorithm=${algorithm//--/}
                
                echo "Running $algorithm on $size jobs..."
                
                for instance in $instances; do
                    for ((i=1; i<=$RUNS; i++)); do
                        seed=$((12345 + i))
                        ./pfsp $instance ii $rule $neigh $init --seed $seed
                    done
                done
            done
        done
    done
    
    # VND algorithms
    for order in "--order1" "--order2"; do
        algorithm="vnd_rz_${order}"
        algorithm=${algorithm//--/}
        
        echo "Running $algorithm on $size jobs..."
        
        for instance in $instances; do
            ./pfsp $instance vnd $order --rz
        done
    done
}

# Run experiments for all instance sizes
run_experiment "$INSTANCES_50" 50

echo "All experiments completed. Results saved to results.txt"