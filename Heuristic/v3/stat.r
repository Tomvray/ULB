# Statistical analysis script for PFSP implementation exercise

# Load required libraries
library(tidyverse)
library(rstatix)

# Read results data
results <- read.csv("results.txt", header = FALSE, 
                   col.names = c("algorithm", "instance_size", "deviation", "time"))

# Clean and prepare data
results <- results %>%
  mutate(
    init_method = ifelse(grepl("random", algorithm), "random", "rz"),
    pivoting_rule = ifelse(grepl("first", algorithm), "first", "best"),
    neighborhood = case_when(
      grepl("transpose", algorithm) ~ "transpose",
      grepl("exchange", algorithm) ~ "exchange",
      grepl("insert", algorithm) ~ "insert",
      TRUE ~ NA_character_
    ),
    vnd_order = case_when(
      grepl("order1", algorithm) ~ "transpose_exchange_insert",
      grepl("order2", algorithm) ~ "transpose_insert_exchange",
      TRUE ~ NA_character_
    ),
    algorithm_type = ifelse(grepl("vnd", algorithm), "vnd", "ii")
  )

# Function to perform Wilcoxon test and print results
perform_test <- function(data, group1, group2, description) {
  test_data <- data %>%
    filter(algorithm %in% c(group1, group2)) %>%
    mutate(algorithm = factor(algorithm))
  
  test_result <- wilcox.test(deviation ~ algorithm, data = test_data, paired = TRUE)
  
  cat("\n", description, "\n")
  cat("Comparing:", group1, "vs", group2, "\n")
  cat("p-value:", test_result$p.value, "\n")
  if (test_result$p.value < 0.05) {
    cat("Conclusion: Significant difference at 0.05 level\n")
    
    # Calculate effect size (rank-biserial correlation)
    effect_size <- wilcox_effsize(test_data, deviation ~ algorithm, paired = TRUE)
    cat("Effect size (rank-biserial correlation):", effect_size$effsize, "\n")
    
    # Calculate median deviations
    medians <- test_data %>%
      group_by(algorithm) %>%
      summarise(median_dev = median(deviation))
    cat("Median deviations:\n")
    print(medians)
  } else {
    cat("Conclusion: No significant difference at 0.05 level\n")
  }
}

# 1. Compare initial solutions (RZ vs random)
# For each combination of pivoting rule and neighborhood
for (rule in c("first", "best")) {
  for (neigh in c("transpose", "exchange", "insert")) {
    group1 <- paste("ii_random", rule, neigh, sep = "_")
    group2 <- paste("ii_rz", rule, neigh, sep = "_")
    desc <- paste("Initial solution comparison (", rule, "improvement,", neigh, "neighborhood)")
    
    perform_test(results, group1, group2, desc)
  }
}

# 2. Compare pivoting rules (first vs best improvement)
# For each combination of init method and neighborhood
for (init in c("random", "rz")) {
  for (neigh in c("transpose", "exchange", "insert")) {
    group1 <- paste("ii", init, "first", neigh, sep = "_")
    group2 <- paste("ii", init, "best", neigh, sep = "_")
    desc <- paste("Pivoting rule comparison (", init, "init,", neigh, "neighborhood)")
    
    perform_test(results, group1, group2, desc)
  }
}

# 3. Compare neighborhoods
# For each combination of init method and pivoting rule
for (init in c("random", "rz")) {
  for (rule in c("first", "best")) {
    # Transpose vs Exchange
    group1 <- paste("ii", init, rule, "transpose", sep = "_")
    group2 <- paste("ii", init, rule, "exchange", sep = "_")
    desc <- paste("Neighborhood comparison (", init, "init,", rule, "improvement): transpose vs exchange")
    perform_test(results, group1, group2, desc)
    
    # Transpose vs Insert
    group1 <- paste("ii", init, rule, "transpose", sep = "_")
    group2 <- paste("ii", init, rule, "insert", sep = "_")
    desc <- paste("Neighborhood comparison (", init, "init,", rule, "improvement): transpose vs insert")
    perform_test(results, group1, group2, desc)
    
    # Exchange vs Insert
    group1 <- paste("ii", init, rule, "exchange", sep = "_")
    group2 <- paste("ii", init, rule, "insert", sep = "_")
    desc <- paste("Neighborhood comparison (", init, "init,", rule, "improvement): exchange vs insert")
    perform_test(results, group1, group2, desc)
  }
}

# 4. Compare VND orderings
perform_test(results, "vnd_rz_order1", "vnd_rz_order2", 
             "VND neighborhood ordering comparison")

# 5. Compare VND with single neighborhood algorithms
for (neigh in c("exchange", "insert")) {
  group1 <- paste("ii_rz_first", neigh, sep = "_")
  group2 <- "vnd_rz_order1"
  desc <- paste("VND vs single neighborhood (", neigh, ")")
  perform_test(results, group1, group2, desc)
}

# Generate summary tables
summary_stats <- results %>%
  group_by(algorithm_type, init_method, pivoting_rule, neighborhood, vnd_order, instance_size) %>%
  summarise(
    avg_deviation = mean(deviation),
    median_deviation = median(deviation),
    avg_time = mean(time),
    .groups = "drop"
  )

# Print summary tables
cat("\nSummary statistics by algorithm and instance size:\n")
print(summary_stats, n = Inf)

# Save summary to file
write.csv(summary_stats, "summary_stats.csv", row.names = FALSE)

# Create plots
ggplot(filter(results, algorithm_type == "ii"), 
       aes(x = algorithm, y = deviation, fill = as.factor(instance_size))) +
  geom_boxplot() +
  theme(axis.text.x = element_text(angle = 90, hjust = 1)) +
  labs(title = "Performance of Iterative Improvement Algorithms",
       x = "Algorithm", y = "Deviation from best known (%)",
       fill = "Instance size")

ggsave("ii_performance.png", width = 12, height = 8)

ggplot(filter(results, algorithm_type == "vnd"), 
       aes(x = algorithm, y = deviation, fill = as.factor(instance_size))) +
  geom_boxplot() +
  theme(axis.text.x = element_text(angle = 90, hjust = 1)) +
  labs(title = "Performance of VND Algorithms",
       x = "Algorithm", y = "Deviation from best known (%)",
       fill = "Instance size")

ggsave("vnd_performance.png", width = 8, height = 6)