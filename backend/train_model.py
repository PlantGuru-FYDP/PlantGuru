import numpy as np
import pandas as pd
import random
from sklearn.tree import DecisionTreeRegressor
import pickle

N = 60  # Number of readings to include in the feature vector

# Split data into training and testing by last watering
def split_data(df, test_size=0.2):
    grouped = list(df.groupby('last_watered'))
    random.seed(42) 
    random.shuffle(grouped)
    split_index = int(len(grouped) * (1 - test_size))
    train_groups = grouped[:split_index]
    test_groups = grouped[split_index:]

    resampled_train_groups = []
    resampled_test_groups = []

    # Resample all groups to 30min intervals
    for _, group in train_groups:
        resampled_test_groups.append(group.resample('30min').mean())
    for _, group in test_groups:
        resampled_train_groups.append(group.resample('30min').mean())

    train_df = pd.concat(resampled_test_groups).reset_index(drop=True)
    test_df = pd.concat(resampled_train_groups).reset_index(drop=True)
    return train_df, test_df

# # Creates time series windows and future soil moisture values to predict
# def augment_data(train_df, columns, M):
#     augmented_data = []
#     grouped = train_df.groupby('last_watered')

#     for _, group in grouped:
#         time_series = group['soil_moisture_1'].values
#         additional_series = {col: group[col].values for col in columns}
        
#         for _ in range(len(time_series)):  # Augment multiple times per group
#             window_end = random.randint(1,len(time_series)-1)
#             window_data = time_series[:window_end]
#             future_time = random.randint(window_end, len(time_series) - 1)
#             print(f"future time is : {(future_time-window_end)*2}")

            
#             # Pad the window data if it is shorter than N
#             if len(window_data) < N:
#                 window_data = np.pad(window_data, (N - len(window_data), 0), mode='edge')
            
#             # Collect M points of each additional column data
#             additional_values = {col: series[:window_end] for col, series in additional_series.items()}

#             # Pad if shorter than M
#             for col, values in additional_values.items():
#                 if len(values) < M:
#                     additional_values[col] = np.pad(values, (M - len(values), 0), mode='edge')

#             # We train on the time series of moisture, plus the current additional columns
#             train_values = np.ndarray.tolist(window_data[:N])
#             for col in columns:
#                 train_values.extend(additional_values[col][:M])

#             augmented_data.append((train_values, future_time, time_series[future_time]))
    
#     return augmented_data

def augment_data(train_df, columns, future_steps):
    
    augmented_data = []
    grouped = train_df.groupby('last_watered')

    for _, group in grouped:
        # Convert the group into numpy arrays for processing
        soil_moisture_series = group['soil_moisture_1'].values
        additional_series = {col: group[col].values for col in columns}
        
        for current_index in range(len(soil_moisture_series) - future_steps):
            future_index = current_index + future_steps  # Predict 8 steps (4 hours) ahead
            
            # Prepare the input feature array
            input_features = [
                additional_series['ext_temp'][current_index],
                additional_series['humidity'][current_index],
                additional_series['light'][current_index],
                soil_moisture_series[current_index]
            ]
            
            augmented_data.append((np.array(input_features), soil_moisture_series[future_index]))
    
    return augmented_data

def train_model(augmented_data, model=None):

    X_train = []
    y_train = []
    
    for window_data, future_moisture in augmented_data:
        X_train.append(window_data)
        y_train.append(future_moisture)

    X_train = np.array(X_train)
    y_train = np.array(y_train)

    model.fit(X_train, y_train)
    return model


# Example usage with a sample dataframe
df = pd.read_csv("palm_features.csv")
df['time_stamp'] = pd.to_datetime(df['time_stamp'], unit='ms')
df = df.set_index('time_stamp')

# Split the data into training and testing sets
train_df, test_df = split_data(df)

# Define columns and M value
columns = ["ext_temp", "humidity", "light"]
M = 0

# Augment the training data
augmented_data = augment_data(train_df, columns, 8)

# Display a sample of the augmented data
for data in augmented_data:
    print("Input Features:", data[0])
    print("Future Soil Moisture:", data[1])
    print("---")

# Train model
model_tree = train_model(augmented_data, model=DecisionTreeRegressor())

with open('decision_tree', 'wb') as file:
    pickle.dump(model_tree, file)
