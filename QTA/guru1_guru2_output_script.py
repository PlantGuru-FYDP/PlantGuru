import pandas as pd
import numpy as np
import csv
import io
import glob
import os

# output 3 spreadsheets, one per plant + one for soil + one for watering times
# merge guru1 (no plant)
# merge guru2 (plant)

no_plant_output = 'QTA\guru1_temp.csv'
plant_output = 'QTA\guru2_temp.csv'

no_plant_files = glob.glob("QTA\*_guru1.csv")
plant_files = glob.glob("QTA\*_guru2.csv")

dfs_no_plant = []
dfs_plant = []

for no_plant in no_plant_files :
    df_no_plant = pd.read_csv(no_plant)
    dfs_no_plant.append(df_no_plant)

for plant in plant_files :
    df_plant = pd.read_csv(plant)
    dfs_plant.append(df_plant)

merged_no_plant = pd.concat(dfs_no_plant, ignore_index=True)
merged_plant = pd.concat(dfs_plant, ignore_index=True)

merged_no_plant.to_csv(no_plant_output, index=False)
merged_plant.to_csv(plant_output, index=False)

with open('QTA\guru1_temp.csv', newline ='') as inFile, open('QTA\guru1_mod.csv', 'w', newline ='') as outFile:
  r = csv.reader(inFile)
  w = csv.writer(outFile)

  next(r, None) #skip old stuff
  w.writerow(['soil_temp', 'ext_temp', 'light', 'soil_moisture_1', 'soil_moisture_2', 'humidity', 'timestamp'])
  for row in r:
    w.writerow(row)

with open('QTA\guru2_temp.csv', newline ='') as infile, open('QTA\guru2_mod.csv', 'w', newline ='') as outfile:
  r = csv.reader(infile)
  w = csv.writer(outfile)

  next(r, None) #skip old stuff
  w.writerow(['soil_temp', 'ext_temp', 'light', 'soil_moisture_1', 'soil_moisture_2', 'humidity', 'timestamp'])
  for row in r:
    w.writerow(row)


df1 = pd.read_csv('QTA\guru1_mod.csv')

df2 = pd.read_csv('QTA\guru2_mod.csv')
df1 = df1.sort_values(by='timestamp').reset_index(drop=True)
df2 = df2.sort_values(by='timestamp').reset_index(drop=True)
df2['light'] = np.interp(df2['timestamp'], df1['timestamp'], df1['light'])
df2['ext_temp'] = np.interp(df2['timestamp'], df1['timestamp'], df1['ext_temp'])
df2['humidity'] = np.interp(df2['timestamp'], df1['timestamp'], df1['humidity'])

df2.to_csv('QTA\guru2_mod_nonull.csv', index = False)



# 1718397185960 when we inserted into plant
# Input CSV file path
guru2_file = 'QTA\guru2_mod_nonull.csv'

# Output CSV file path where filtered columns will be copied
impatiens = 'QTA\impatiens_mod_nonull.csv'
palm = 'QTA\palm_mod_nonull.csv'

# Unix timestamp criteria (replace with your desired Unix timestamp)
start_unix_timestamp = 1718397185960  # Unix timestamp for '2021-06-21 00:00:00 UTC'
end_unix_timestamp = 1718398320711
# Columns to copy (replace with your actual column names)
palm_columns = ['soil_temp','ext_temp', 'light', 'soil_moisture_1', 'soil_moisture_2', 'humidity', 'timestamp']
impatiens_columns = ['soil_temp', 'ext_temp', 'light', 'soil_moisture_1', 'soil_moisture_2', 'humidity', 'timestamp']

# Read the original CSV file into a DataFrame
df = pd.read_csv(guru2_file)

# Filter rows based on Unix timestamp criteria
filtered_no_temp = df[ (df['timestamp'].astype(int) >= start_unix_timestamp) & (df['timestamp'].astype(int) <= end_unix_timestamp)].copy()
filtered = df[ df['timestamp'].astype(int) >= end_unix_timestamp].copy()

# Select specific columns
filtered_no_temp_palm = filtered_no_temp[palm_columns]
filtered_no_temp_palm ['soil_temp'] = np.nan # null incorrect temp values
filtered_temp_palm = filtered[palm_columns] # correct temp values for palm

filtered_temp_impatiens = filtered_no_temp[impatiens_columns]
filtered_no_temp_impatiens = filtered[impatiens_columns]
filtered_no_temp_impatiens ['soil_temp'] = np.nan # null incorrect soil temp values

filtered_palm = pd.concat([filtered_no_temp_palm,filtered_temp_palm], ignore_index=True)
filtered_impatiens = pd.concat([filtered_temp_impatiens,filtered_no_temp_impatiens], ignore_index=True)

filtered_palm ['soil_moisture_2'] = np.nan
filtered_impatiens['soil_moisture_1'] = np.nan

# Write filtered columns to the output CSV file
filtered_palm.to_csv(palm, index=False)
filtered_impatiens.to_csv(impatiens, index=False)


