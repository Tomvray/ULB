#!/bin/bash

mkdir -p "instances/Results"

# Define test configurations using bash arrays
pivots=("--first" "--best")
neighborhoods=("--transpose" "--exchange" "--insert")
inits=("--random-init" "--srz")

# Count total number of files
total_files=$(find "instances/Benchmarks" -type f | wc -l)
current_file=0

for file in "instances/Benchmarks"/*; do
    if [[ -f "$file" ]]; then
        current_file=$((current_file + 1))
        filename=$(basename "$file")
        result_file="instances/Results/${filename}_results.csv"
        
        echo "Testing $filename [$current_file/$total_files]"
        
        # Create CSV header with runs as columns
        echo "Pivot,Neighborhood,Init,Run1_Cost,Run1_Time,Run2_Cost,Run2_Time,Run3_Cost,Run3_Time,Run4_Cost,Run4_Time,Run5_Cost,Run5_Time,Run6_Cost,Run6_Time,Run7_Cost,Run7_Time,Run8_Cost,Run8_Time,Run9_Cost,Run9_Time,Run10_Cost,Run10_Time,Avg_Cost,Avg_Time,Min_Cost,Max_Cost" > "$result_file"
        
        for pivot in "${pivots[@]}"; do
            for neighborhood in "${neighborhoods[@]}"; do
                for init in "${inits[@]}"; do
                    echo "  Running: ${pivot} ${neighborhood} ${init}"
                    
                    # Arrays to store results
                    run_costs=()
                    run_times=()
                    total_cost=0
                    total_time=0
                    valid_runs=0
                    
                    # Run 10 times with different seeds
                    for run in {1..10}; do
                        echo "    Run $run/10"
                        
                        # Generate a unique seed for this run
                        seed=$(($(date +%s%N) + run + current_file * 1000))
                        
                        # Capture start time in milliseconds
                        start_time=$(date +%s%3N)
                        
                        # Run the program with seed as environment variable
                        output=$(RANDOM_SEED=$seed ./pfsp "$pivot" "$neighborhood" "$init" "$file" 2>&1)
                        exit_code=$?
                        
                        # Capture end time in milliseconds
                        end_time=$(date +%s%3N)
                        
                        if [ $exit_code -eq 0 ]; then
                            # Extract cost from output
                            cost=$(echo "$output" | grep "^cost " | grep -o '[0-9]\+')
                            
                            # Extract time from output
                            program_time=$(echo "$output" | grep "time after Iterative_algo" | grep -o '[0-9]*\.*[0-9]*')
                            
                            if [[ -n "$cost" && "$cost" =~ ^[0-9]+$ ]]; then
                                # Use program's reported time if available
                                if [[ -n "$program_time" ]]; then
                                    execution_time="$program_time"
                                else
                                    execution_time_ms=$((end_time - start_time))
                                    execution_time=$(awk "BEGIN {printf \"%.6f\", $execution_time_ms/1000}")
                                fi
                                
                                # Store results
                                run_costs+=("$cost")
                                run_times+=("$execution_time")
                                
                                total_cost=$((total_cost + cost))
                                time_ms=$(awk "BEGIN {printf \"%.0f\", $execution_time * 1000}")
                                total_time=$((total_time + time_ms))
                                valid_runs=$((valid_runs + 1))
                                
                                echo "    Cost: $cost, Time: ${execution_time}s (seed: $seed)"
                            else
                                echo "    Warning: Could not extract cost from output"
                                run_costs+=("ERROR")
                                run_times+=("ERROR")
                            fi
                        else
                            echo "    Error: Program failed with exit code $exit_code"
                            run_costs+=("ERROR")
                            run_times+=("ERROR")
                        fi
                    done
                    
                    # Prepare CSV line with all run results
                    csv_line="${pivot},${neighborhood},${init}"
                    
                    # Add all individual runs (ensure we have exactly 10 entries)
                    for i in {0..9}; do
                        if [ $i -lt ${#run_costs[@]} ]; then
                            csv_line="${csv_line},${run_costs[$i]},${run_times[$i]}"
                        else
                            csv_line="${csv_line},ERROR,ERROR"
                        fi
                    done
                    
                    # Calculate and add statistics
                    if [ $valid_runs -gt 0 ]; then
                        avg_cost=$((total_cost / valid_runs))
                        avg_time=$(awk "BEGIN {printf \"%.6f\", $total_time/($valid_runs*1000)}")
                        
                        # Calculate min and max cost
                        min_cost=$(printf '%s\n' "${run_costs[@]}" | grep -v ERROR | sort -n | head -1)
                        max_cost=$(printf '%s\n' "${run_costs[@]}" | grep -v ERROR | sort -n | tail -1)
                        
                        if [[ -z "$min_cost" ]]; then min_cost="ERROR"; fi
                        if [[ -z "$max_cost" ]]; then max_cost="ERROR"; fi
                        
                        csv_line="${csv_line},${avg_cost},${avg_time},${min_cost},${max_cost}"
                        echo "    Average: Cost=$avg_cost, Time=${avg_time}s (${valid_runs}/10 successful runs)"
                        echo "    Min: $min_cost, Max: $max_cost"
                    else
                        csv_line="${csv_line},ERROR,ERROR,ERROR,ERROR"
                        echo "    Error: No successful runs for this configuration"
                    fi
                    
                    # Write the complete line to CSV file
                    echo "$csv_line" >> "$result_file"
                done
            done
        done
        
        echo "Results saved to: $result_file"
        echo "Completed file $current_file/$total_files: $filename"
        echo "----------------------------------------"
    fi
done

echo "All tests completed! Processed $current_file/$total_files files."