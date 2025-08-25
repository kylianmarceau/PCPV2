import os
import pandas as pd
import matplotlib.pyplot as plt
import numpy as np

output_folder = "AnalysisOutput"
os.makedirs(output_folder, exist_ok=True)


# Load the data
def load_data():
    # Load parallel data
    parallel_data = []
    with open('ProfileOutputs/parallel_tests_output.txt', 'r') as f:
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
    with open('ProfileOutputs/serial_tests_output.txt', 'r') as f:
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
    
    # Graph 2: Parallel vs Serial Execution Time for each density
    ax2 = axes[0, 1]
    densities = speedup_df['density'].unique()
    
    for density in densities:
        data = speedup_df[speedup_df['density'] == density]
        # Plot serial times
        ax2.plot(data['grid_size'], data['serial'], 
                marker='s', label=f'Serial (Density {density})', linewidth=2, alpha=0.7)
        # Plot parallel times
        ax2.plot(data['grid_size'], data['parallel'], 
                marker='o', label=f'Parallel (Density {density})', linewidth=2, alpha=0.7)
    
    ax2.set_xlabel('Grid Size (n)')
    ax2.set_ylabel('Execution Time (ms)')
    ax2.set_title('Execution Time: Serial vs Parallel by Density')
    ax2.legend(bbox_to_anchor=(1.05, 1), loc='upper left')
    ax2.grid(True, alpha=0.3)
    
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
    
    # Hide the bottom right subplot (Graph 4)
    axes[1, 1].set_visible(False)
    
    plt.tight_layout()
    
    # Save to OUTPUT folder
    output_path = os.path.join(output_folder, 'speedup_analysis.png')
    plt.savefig(output_path, dpi=300, bbox_inches='tight')
    plt.show()

def generate_report_data(speedup_df):
    report_content = []
    report_content.append("=== PERFORMANCE ANALYSIS REPORT ===\n")
    
    report_content.append("1. MAXIMUM SPEEDUP ACHIEVED:")
    max_speedup_row = speedup_df.loc[speedup_df['speedup'].idxmax()]
    report_content.append(f"   Maximum speedup: {max_speedup_row['speedup']:.2f}x")
    report_content.append(f"   At grid size: {max_speedup_row['grid_size']}")
    report_content.append(f"   At density: {max_speedup_row['density']}")
    
    report_content.append("\n2. SPEEDUP BY GRID SIZE:")
    for size in sorted(speedup_df['grid_size'].unique()):
        avg_speedup = speedup_df[speedup_df['grid_size'] == size]['speedup'].mean()
        report_content.append(f"   Grid {size}: {avg_speedup:.2f}x average speedup")
    
    report_content.append("\n3. SPEEDUP BY DENSITY:")
    for density in sorted(speedup_df['density'].unique()):
        avg_speedup = speedup_df[speedup_df['density'] == density]['speedup'].mean()
        report_content.append(f"   Density {density}: {avg_speedup:.2f}x average speedup")
    
    report_content.append("\n4. CASES WHERE PARALLEL IS SLOWER (speedup < 1.0):")
    slow_cases = speedup_df[speedup_df['speedup'] < 1.0]
    if len(slow_cases) > 0:
        for _, row in slow_cases.iterrows():
            report_content.append(f"   Grid {row['grid_size']}, Density {row['density']}: {row['speedup']:.2f}x")
    else:
        report_content.append("   None - parallel version always faster!")
    

    
    # Print to console
    for line in report_content:
        print(line)
    
    # Save report to OUTPUT folder
    report_path = os.path.join(output_folder, 'performance_report.txt')
    with open(report_path, 'w') as f:
        f.write('\n'.join(report_content))
    
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
    
    # Save processed data to OUTPUT folder
    csv_path = os.path.join(output_folder, 'speedup_results.csv')
    speedup_df.to_csv(csv_path, index=False)
    
    print(f"\nAll outputs saved to '{output_folder}' folder:")
    print(f"  - speedup_results.csv")
    print(f"  - speedup_analysis.png")
    print(f"  - performance_report.txt")

if __name__ == "__main__":
    main()