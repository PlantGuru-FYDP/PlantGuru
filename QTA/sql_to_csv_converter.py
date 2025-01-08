import re
import csv
import pandas as pd
import os
from typing import List, Dict, Set

class SQLDumpConverter:
    def __init__(self, dump_file: str, output_dir: str = 'csv_output'):
        """
        Initialize the converter with paths for input and output.
        
        Args:
            dump_file (str): Path to the SQL dump file
            output_dir (str): Directory where CSV files will be saved
        """
        self.dump_file = dump_file
        self.output_dir = output_dir
        self.table_data: Dict[str, List[List[str]]] = {}
        self.headers: Dict[str, List[str]] = {}
        
        # Create output directory if it doesn't exist
        if not os.path.exists(output_dir):
            os.makedirs(output_dir)

    def _extract_table_name(self, line: str) -> str:
        """Extract table name from CREATE TABLE or INSERT INTO statement."""
        match = re.search(r'(?:CREATE TABLE|INSERT INTO) `?(\w+)`?', line)
        return match.group(1) if match else None

    def _extract_column_names(self, create_statement: str) -> List[str]:
        """Extract column names from CREATE TABLE statement."""
        # Remove newlines and extra spaces
        create_statement = ' '.join(create_statement.split())
        # Find content between first ( and last )
        columns_section = re.search(r'\((.*)\)', create_statement).group(1)
        
        columns = []
        for column_def in columns_section.split(','):
            # Extract just the column name (first word in definition)
            column_name = column_def.strip().split()[0].replace('`', '')
            if not column_name.startswith('PRIMARY') and not column_name.startswith('KEY'):
                columns.append(column_name)
        
        return columns

    def _parse_insert_values(self, line: str) -> List[List[str]]:
        """Parse VALUES section of INSERT statement into list of records."""
        values_match = re.search(r'VALUES\s*(.*);', line)
        if not values_match:
            return []

        values_str = values_match.group(1)
        values = []
        current_value = []
        current_str = ''
        in_string = False
        in_parentheses = False

        for char in values_str:
            if char == "'" and not in_string:
                in_string = True
            elif char == "'" and in_string:
                in_string = False
            elif char == '(' and not in_string:
                in_parentheses = True
                current_value = []
            elif char == ')' and not in_string:
                in_parentheses = False
                current_value.append(current_str.strip("'"))
                current_str = ''
                values.append(current_value)
            elif char == ',' and not in_string:
                if in_parentheses:
                    current_value.append(current_str.strip("'"))
                    current_str = ''
            else:
                if in_parentheses:
                    current_str += char

        return values

    def convert(self):
        """Convert SQL dump to CSV files."""
        current_table = None
        create_statement = ''
        in_create = False

        with open(self.dump_file, 'r', encoding='utf-8') as f:
            for line in f:
                line = line.strip()
                
                # Skip comments and empty lines
                if not line or line.startswith('--') or line.startswith('/*'):
                    continue

                # Handle CREATE TABLE
                if 'CREATE TABLE' in line:
                    current_table = self._extract_table_name(line)
                    create_statement = line
                    in_create = True
                    continue

                if in_create:
                    create_statement += ' ' + line
                    if line.endswith(';'):
                        self.headers[current_table] = self._extract_column_names(create_statement)
                        self.table_data[current_table] = []
                        in_create = False
                    continue

                # Handle INSERT statements
                if 'INSERT INTO' in line:
                    table_name = self._extract_table_name(line)
                    values = self._parse_insert_values(line)
                    if table_name in self.table_data:
                        self.table_data[table_name].extend(values)

        # Write to CSV files
        for table_name, data in self.table_data.items():
            csv_path = os.path.join(self.output_dir, f'{table_name}.csv')
            with open(csv_path, 'w', newline='', encoding='utf-8') as csvfile:
                writer = csv.writer(csvfile)
                writer.writerow(self.headers[table_name])
                writer.writerows(data)

    def get_dataframes(self) -> Dict[str, pd.DataFrame]:
        """
        Load all CSV files into pandas DataFrames.
        
        Returns:
            Dict[str, pd.DataFrame]: Dictionary mapping table names to their DataFrames
        """
        dataframes = {}
        for table_name in self.table_data.keys():
            csv_path = os.path.join(self.output_dir, f'{table_name}.csv')
            dataframes[table_name] = pd.read_csv(csv_path)
        return dataframes

def main():
    # Example usage
    converter = SQLDumpConverter('QTA/database_dump.sql')
    converter.convert()
    
    # Get all tables as DataFrames
    dataframes = converter.get_dataframes()
    
    # Print info about each DataFrame
    for table_name, df in dataframes.items():
        print(f"\nTable: {table_name}")
        print(f"Shape: {df.shape}")
        print("\nColumns:")
        print(df.columns.tolist())
        print("\nFirst few rows:")
        print(df.head())
        print("-" * 80)

if __name__ == "__main__":
    main() 