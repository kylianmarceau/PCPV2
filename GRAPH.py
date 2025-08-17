import pandas as pd
import matplotlib.pyplot as plt
import numpy as np

# Load the data
def load_data():
    # Load parallel data
    parallel_data = []
    with open('parallel_tests_output.txt', 'r') as f:
        lines = f.readlines()[1:]  # Skip header
        for line in lines:
            parts = line.strip().split(', ')
            if len(parts) == 4:
                grid_size, density, seed, time_ms = parts
                parallel_data.append({
                    'grid_size': int(grid_size),
                    'density': float(density),
                    'seed': int(seed),
                    'time_ms': int(time_ms),
                    'version': 'parallel'
                })
    
    # Load serial data
    serial_data = []
    with open('serial_tests_output.txt', 'r') as f:
        lines = f.readlines()[1:]  # Skip header
        for line in lines:
            parts = line.strip().split(', ')
            if len(parts) == 4:
                grid_size, density, seed, time_ms = parts
                serial_data.append({
                    'grid_size': int(grid_size),
                    'density': float(density),
                    'seed': int(seed),
                    'time_ms': int(time_ms),
                    'version': 'serial'
                })
    
    # Combine data
    all_data = serial_data + parallel_data
    df = pd.DataFrame(all_data)
    return df

def calculate_speedup(df):
    # Calculate average times for each configuration
    avg_times = df.groupby(['grid_size', 'density', 'version'])['time_ms'].mean().reset_index()
    
    # Pivot to have serial and parallel times in separate columns
    pivot_df = avg_times.pivot_table(
        index=['grid_size', 'density'], 
        columns='version', 
        values='time_ms'
    ).reset_index()
    
    # Calculate speedup = serial_time / parallel_time
    pivot_df['speedup'] = pivot_df['serial'] / pivot_df['parallel']
    
    return pivot_df

def create_speedup_graphs(speedup_df):
    # Set up the plotting style
    plt.style.use('default')
    fig, axes = plt.subplots(2, 2, figsize=(15, 10))
    fig.suptitle('Parallel Performance Analysis', fontsize=16)
    
    # Graph 1: Speedup vs Grid Size for different densities
    ax1 = axes[0, 0]
    densities = speedup_df['density'].unique()
    for density in densities:
        data = speedup_df[speedup_df['density'] == density]
        ax1.plot(data['grid_size'], data['speedup'], 
                marker='o', label=f'Density {density}', linewidth=2)
    
    ax1.axhline(y=1.0, color='red', linestyle='--', alpha=0.7, label='No Speedup')
    ax1.set_xlabel('Grid Size')
    ax1.set_ylabel('Speedup (Serial Time / Parallel Time)')
    ax1.set_title('Speedup vs Grid Size')
    ax1.legend()
    ax1.grid(True, alpha=0.3)
    
    # Graph 2: Execution Time Comparison
    ax2 = axes[0, 1]
    # Show times for density 0.1 as example
    density_01_data = speedup_df[speedup_df['density'] == 0.1]
    ax2.plot(density_01_data['grid_size'], density_01_data['serial'], 
            marker='s', label='Serial', linewidth=2)
    ax2.plot(density_01_data['grid_size'], density_01_data['parallel'], 
            marker='o', label='Parallel', linewidth=2)
    ax2.set_xlabel('Grid Size')
    ax2.set_ylabel('Execution Time (ms)')
    ax2.set_title('Execution Time Comparison (Density 0.1)')
    ax2.legend()
    ax2.grid(True, alpha=0.3)
    ax2.set_yscale('log')  # Log scale for better visibility
    
    # Graph 3: Speedup vs Density for different grid sizes
    ax3 = axes[1, 0]
    grid_sizes = speedup_df['grid_size'].unique()
    for size in grid_sizes:
        data = speedup_df[speedup_df['grid_size'] == size]
        ax3.plot(data['density'], data['speedup'], 
                marker='o', label=f'Grid {size}', linewidth=2)
    
    ax3.axhline(y=1.0, color='red', linestyle='--', alpha=0.7)
    ax3.set_xlabel('Search Density')
    ax3.set_ylabel('Speedup')
    ax3.set_title('Speedup vs Search Density')
    ax3.legend()
    ax3.grid(True, alpha=0.3)
    
    # Graph 4: Efficiency Analysis
    ax4 = axes[1, 1]
    # Assuming 4 cores (adjust based on your system)
    num_cores = 4
    speedup_df['efficiency'] = speedup_df['speedup'] / num_cores
    
    for density in densities:
        data = speedup_df[speedup_df['density'] == density]
        ax4.plot(data['grid_size'], data['efficiency'], 
                marker='o', label=f'Density {density}', linewidth=2)
    
    ax4.axhline(y=1.0, color='red', linestyle='--', alpha=0.7, label='Perfect Efficiency')
    ax4.set_xlabel('Grid Size')
    ax4.set_ylabel('Parallel Efficiency')
    ax4.set_title('Parallel Efficiency vs Grid Size')
    ax4.legend()
    ax4.grid(True, alpha=0.3)
    
    plt.tight_layout()
    plt.savefig('speedup_analysis.png', dpi=300, bbox_inches='tight')
    plt.show()

def generate_report_data(speedup_df):
    print("=== PERFORMANCE ANALYSIS REPORT ===\n")
    
    print("1. MAXIMUM SPEEDUP ACHIEVED:")
    max_speedup_row = speedup_df.loc[speedup_df['speedup'].idxmax()]
    print(f"   Maximum speedup: {max_speedup_row['speedup']:.2f}x")
    print(f"   At grid size: {max_speedup_row['grid_size']}")
    print(f"   At density: {max_speedup_row['density']}")
    
    print("\n2. SPEEDUP BY GRID SIZE:")
    for size in sorted(speedup_df['grid_size'].unique()):
        avg_speedup = speedup_df[speedup_df['grid_size'] == size]['speedup'].mean()
        print(f"   Grid {size}: {avg_speedup:.2f}x average speedup")
    
    print("\n3. SPEEDUP BY DENSITY:")
    for density in sorted(speedup_df['density'].unique()):
        avg_speedup = speedup_df[speedup_df['density'] == density]['speedup'].mean()
        print(f"   Density {density}: {avg_speedup:.2f}x average speedup")
    
    print("\n4. CASES WHERE PARALLEL IS SLOWER (speedup < 1.0):")
    slow_cases = speedup_df[speedup_df['speedup'] < 1.0]
    if len(slow_cases) > 0:
        for _, row in slow_cases.iterrows():
            print(f"   Grid {row['grid_size']}, Density {row['density']}: {row['speedup']:.2f}x")
    else:
        print("   None - parallel version always faster!")
    
    print("\n5. THRESHOLD ANALYSIS:")
    print("   Consider testing different THRESHOLD values in your SearchTask:")
    print("   Current threshold appears to be 10 based on your code")
    print("   You may want to test values like 1, 5, 25, 50 for optimization")
    
    return speedup_df

def main():
    print("Loading benchmark data...")
    df = load_data()
    print(f"Loaded {len(df)} data points")
    
    print("\nCalculating speedup metrics...")
    speedup_df = calculate_speedup(df)
    
    print("\nGenerating performance graphs...")
    create_speedup_graphs(speedup_df)
    
    print("\nGenerating report analysis...")
    report_data = generate_report_data(speedup_df)
    
    # Save processed data
    speedup_df.to_csv('speedup_results.csv', index=False)
    print("\nSpeedup results saved to 'speedup_results.csv'")
    print("Performance graphs saved to 'speedup_analysis.png'")

if __name__ == "__main__":
    main()