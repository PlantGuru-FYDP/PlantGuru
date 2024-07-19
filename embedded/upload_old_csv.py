import csv
import json
from datetime import datetime
import requests



with open("C:\\Users\\natne\\Downloads\\palm_mod_nonull.csv", 'r') as csvdata:
    next(csvdata, None) #skip headers
    reader = csv.DictReader(csvdata, fieldnames=['soil_temp', 'ext_temp', 'light', 'soil_moisture_1', 'soil_moisture_2', 'humidity', 'timestamp'])
    json.dump([row for row in reader], open ('palm.json', 'w+'))
    f = open("palm.json")
    dictObj = json.load(f)
    headers = {"Content-type":"application/json"}
    merged_data= []
    i = 0
    for data in dictObj:
        data.update({"plant_id": 7}) #update per plant created
        ts = float(data['timestamp'])
        new_date = datetime.fromtimestamp(ts/1000, tz=None).strftime('%Y-%m-%dT%H:%M:%SZ')
        data.update({"time_stamp": new_date})
        merged_data.append(data)
        i+= 1
        if i == 50:
            response = requests.post("http://18.191.162.227:3000/api/sensorUpload", data=json.dumps(data), headers=headers) 
            try:
                response.raise_for_status()
            except requests.exceptions.HTTPError as e:
                # Whoops it wasn't a 200
                print("Error: " + str(e))
            i = 0
            merged_data = []

        
        

    #for upload_chunk in dictObj:
        
        

    
    
# with open("C:\\Users\\natne\\Downloads\\impatiens_mod_nonull.csv", 'r') as csvdata:
#     next(csvdata, None) #skip headers
#     reader = csv.DictReader(csvdata, fieldnames=['soil_temp', 'ext_temp', 'light', 'soil_moisture_1', 'soil_moisture_2', 'humidity', 'timestamp'])
#     json.dump([row for row in reader], open ('impatiens.json', 'w+'))
#     f = open("impatiens.json")
#     dictObj = json.load(f)

#     headers = {"Content-type":"application/json"}
#     merged_data= []
#     i = 0
#     for data in dictObj:
#         data.update({"plant_id": 8}) #update per plant created
#         ts = float(data['timestamp'])
#         new_date = datetime.fromtimestamp(ts/1000, tz=None).strftime('%Y-%m-%dT%H:%M:%SZ')
#         data.update({"time_stamp": new_date})
#         merged_data.append(data)
#         i+= 1
#         if i == 50:
#             response = requests.post("http://18.191.162.227:3000/api/sensorUpload", data=json.dumps(data), headers=headers) 
#             try:
#                 response.raise_for_status()
#             except requests.exceptions.HTTPError as e:
#                 # Whoops it wasn't a 200
#                 print("Error: " + str(e))
#             i = 0
#             merged_data = []

with open("C:\\Users\\natne\\Downloads\\guru1_mod.csv", 'r') as csvdata:
    next(csvdata, None) #skip headers
    reader = csv.DictReader(csvdata, fieldnames=['soil_temp', 'ext_temp', 'light', 'soil_moisture_1', 'soil_moisture_2', 'humidity', 'timestamp'])
    json.dump([row for row in reader], open ('soil.json', 'w+'))
    f = open("soil.json")
    dictObj = json.load(f)

    headers = {"Content-type":"application/json"}
    merged_data= []
    i = 0
    for data in dictObj:
        data.update({"plant_id": 9}) #update per plant created
        ts = float(data['timestamp'])
        new_date = datetime.fromtimestamp(ts/1000, tz=None).strftime('%Y-%m-%dT%H:%M:%SZ')
        data.update({"time_stamp": new_date})
        merged_data.append(data)
        i+= 1
        if i == 50:
            response = requests.post("http://18.191.162.227:3000/api/sensorUpload", data=json.dumps(data), headers=headers) 
            try:
                response.raise_for_status()
            except requests.exceptions.HTTPError as e:
                # Whoops it wasn't a 200
                print("Error: " + str(e))
            i = 0
            merged_data = []

