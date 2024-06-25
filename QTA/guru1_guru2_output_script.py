import pandas as pd
import numpy as np
import csv
import io
import glob
import os

# output 3 spreadsheets, one per plant + one for soil + one for watering times
# merge guru1 (no plant)
# merge guru2 (plant)

no_plant_output = 'guru1_temp.csv'
plant_output = 'guru2_temp.csv'

no_plant_files = glob.glob("*_guru1.csv")
plant_files = glob.glob("*_guru2.csv")

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

with open('guru1_temp.csv', newline ='') as inFile, open('guru1_mod.csv', 'w', newline ='') as outFile:
  r = csv.reader(inFile);
  w = csv.writer(outFile)

  next(r, None) #skip old stuff
  w.writerow(['soil_temp', 'ext_temp', 'light', 'soil_moisture_1', 'soil_moisture_2', 'humidity', 'timestamp'])
  for row in r:
    w.writerow(row)

with open('guru2_temp.csv', newline ='') as infile, open('guru2_mod.csv', 'w', newline ='') as outfile:
  r = csv.reader(infile);
  w = csv.writer(outfile)

  next(r, None) #skip old stuff
  w.writerow(['soil_temp', 'ext_temp', 'light', 'soil_moisture_1', 'soil_moisture_2', 'humidity', 'timestamp'])
  for row in r:
    w.writerow(row)
