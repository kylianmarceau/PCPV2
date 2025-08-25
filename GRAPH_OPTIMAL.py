import pandas as pd
import matplotlib.pyplot as plt

# Load the data from the files, skipping initial spaces in column names
parallel_data = pd.read_csv('ProfileOutputs/parallel_tests_output.txt', skipinitialspace=True)
serial_data = pd.read_csv('ProfileOutputs/serial_tests_output.txt', skipinitialspace=True)

# Filter data for density 0.60
parallel_density_060 = parallel_data[parallel_data['Density'] == 0.10]
serial_density_060 = serial_data[serial_data['Density'] == 0.10]

# Merge the data on Grid_Size to calculate speedup
merged_data = pd.merge(parallel_density_060, serial_density_060, on='Grid_Size', suffixes=('_parallel', '_serial'))

# Calculate speedup (serial time / parallel time)
merged_data['Speedup'] = merged_data['Time_ms_serial'] / merged_data['Time_ms_parallel']

# Create the plot
plt.figure(figsize=(10, 6))
plt.plot(merged_data['Grid_Size'], merged_data['Speedup'], 'bo-', linewidth=2, markersize=8)
plt.xlabel('Grid Size')
plt.ylabel('Speedup (Serial Time / Parallel Time)')
plt.title('Speedup for Density 0.10 vs Grid Size')
plt.grid(True, alpha=0.3)

# Add value labels to each point
for i, (x, y) in enumerate(zip(merged_data['Grid_Size'], merged_data['Speedup'])):
    plt.annotate(f'{y:.2f}', (x, y), textcoords="offset points", xytext=(0,10), ha='center')

plt.tight_layout()
plt.savefig('speedup_density_0.10.png', dpi=300)
plt.show()

# Print the data for verification
print("Data used for plotting:")
print(merged_data[['Grid_Size', 'Time_ms_parallel', 'Time_ms_serial', 'Speedup']])

# --- Additional plot: Speedup vs Number of Searches for Density 0.60 ---
# Number of searches formula from Java code:
# numSearches = density * (gridSize*2) * (gridSize*2) * DungeonMap.RESOLUTION
# With DungeonMap.RESOLUTION = 5 and density fixed at 0.60

RESOLUTION = 5
DENSITY_FIXED = 0.10
merged_data['Num_Searches'] = (DENSITY_FIXED * (merged_data['Grid_Size'] * 2) * (merged_data['Grid_Size'] * 2) * RESOLUTION).astype(int)

plt.figure(figsize=(10, 6))
plt.plot(merged_data['Num_Searches'], merged_data['Speedup'], 'ro-', linewidth=2, markersize=8)
plt.xlabel('Number of Searches (density 0.10)')
plt.ylabel('Speedup (Serial Time / Parallel Time)')
plt.title('Speedup for Density 0.10 vs Number of Searches')
plt.grid(True, alpha=0.3)

for x, y in zip(merged_data['Num_Searches'], merged_data['Speedup']):
    plt.annotate(f'{y:.2f}', (x, y), textcoords="offset points", xytext=(0,10), ha='center')

plt.tight_layout()
plt.savefig('speedup_density_0.10_vs_searches.png', dpi=300)
plt.show()