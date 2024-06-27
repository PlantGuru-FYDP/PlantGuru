import pandas as pd
import numpy as np
import glob

# combined csvs
no_plant_files = glob.glob("*_guru1.csv")
plant_files = glob.glob("*_guru2.csv")

dfs_no_plant = [pd.read_csv(no_plant) for no_plant in no_plant_files]
dfs_plant = [pd.read_csv(plant) for plant in plant_files]

merged_no_plant = pd.concat(dfs_no_plant, ignore_index=True)
merged_plant = pd.concat(dfs_plant, ignore_index=True)

# drop duplicates
merged_no_plant.drop_duplicates(inplace=True)
merged_plant.drop_duplicates(inplace=True)

new_columns = ['soil_temp', 'ext_temp', 'light', 'soil_moisture_1', 'soil_moisture_2', 'humidity', 'time_stamp']
merged_no_plant.columns = new_columns
merged_plant.columns = new_columns

# Interpolate data
merged_no_plant = merged_no_plant.sort_values(by='time_stamp').reset_index(drop=True)
merged_plant = merged_plant.sort_values(by='time_stamp').reset_index(drop=True)

merged_no_plant["time_stamp"] = pd.to_datetime(merged_no_plant["time_stamp"]).astype(int)
merged_plant["time_stamp"] = pd.to_datetime(merged_plant["time_stamp"]).astype(int) 

merged_plant["light"] = np.interp(merged_plant["time_stamp"], merged_no_plant["time_stamp"], merged_no_plant["light"])
merged_plant['ext_temp'] = np.interp(merged_plant["time_stamp"], merged_no_plant["time_stamp"], merged_no_plant['ext_temp'])
merged_plant['humidity'] = np.interp(merged_plant["time_stamp"], merged_no_plant["time_stamp"], merged_no_plant['humidity'])

# Filter data based on time_stamps
start_unix_time_stamp = 1718397185960  # Example time_stamp for start
end_unix_time_stamp = 1718398320711    # Example time_stamp for end

filtered_no_temp = merged_plant[(merged_plant['time_stamp'] >= start_unix_time_stamp) & (merged_plant['time_stamp'] <= end_unix_time_stamp)].copy()
filtered = merged_plant[merged_plant['time_stamp'] >= end_unix_time_stamp].copy()

palm_columns = ['soil_temp', 'ext_temp', 'light', 'soil_moisture_1', 'soil_moisture_2', 'humidity', 'time_stamp']
impatiens_columns = ['soil_temp', 'ext_temp', 'light', 'soil_moisture_1', 'soil_moisture_2', 'humidity', 'time_stamp']

filtered_no_temp_palm = filtered_no_temp[palm_columns]
filtered_no_temp_palm['soil_temp'] = np.nan  # Null incorrect temp values
filtered_temp_palm = filtered[palm_columns]  # Correct temp values for palm

filtered_temp_impatiens = filtered_no_temp[impatiens_columns]
filtered_no_temp_impatiens = filtered[impatiens_columns]
filtered_no_temp_impatiens['soil_temp'] = np.nan  # Null incorrect soil temp values

filtered_palm = pd.concat([filtered_no_temp_palm, filtered_temp_palm], ignore_index=True)
filtered_impatiens = pd.concat([filtered_temp_impatiens, filtered_no_temp_impatiens], ignore_index=True)

filtered_palm['soil_moisture_2'] = np.nan
filtered_impatiens['soil_moisture_1'] = np.nan

filtered_palm.to_csv('palm.csv', index=False)
filtered_impatiens.to_csv('impatiens.csv', index=False)
merged_no_plant.to_csv('soil.csv', index=False)
