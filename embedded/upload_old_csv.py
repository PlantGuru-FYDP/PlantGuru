import csv
import json
import requests



with open("C:\\Users\\natne\\Downloads\\palm_mod_nonull.csv", 'r') as csvdata:
    next(csvdata, None) #skip headers
    reader = csv.DictReader(csvdata, fieldnames=['soil_temp', 'ext_temp', 'light', 'soil_moisture_1', 'soil_moisture_2', 'humidity', 'timestamp'])
    json.dump([row for row in reader], open ('palm.json', 'w+'))
    f = open("palm.json")
    dictObj = json.load(f)

    for data in dictObj:
        data.update({"plant_id": 6}) #update per plant created
    headers = {"Content-type":"application/json"}
    #response = requests.post("http://18.191.162.227:3000/api/sensorUpload", data=json_data, headers=headers)

with open("C:\\Users\\natne\\Downloads\\impatiens_mod_nonull.csv", 'r') as csvdata:
    next(csvdata, None) #skip headers
    reader = csv.DictReader(csvdata, fieldnames=['soil_temp', 'ext_temp', 'light', 'soil_moisture_1', 'soil_moisture_2', 'humidity', 'timestamp'])
    json.dump([row for row in reader], open ('impatiens.json', 'w+'))
    f = open("impatiens.json")
    dictObj = json.load(f)

    for data in dictObj:
        data.update({"plant_id": 6}) #update per plant created
    headers = {"Content-type":"application/json"}
    #response = requests.post("http://18.191.162.227:3000/api/sensorUpload", data=json_data, headers=headers)

with open("C:\\Users\\natne\\Downloads\\guru1_mod.csv", 'r') as csvdata:
    next(csvdata, None) #skip headers
    reader = csv.DictReader(csvdata, fieldnames=['soil_temp', 'ext_temp', 'light', 'soil_moisture_1', 'soil_moisture_2', 'humidity', 'timestamp'])
    json.dump([row for row in reader], open ('soil.json', 'w+'))
    f = open("soil.json")
    dictObj = json.load(f)

    for data in dictObj:
        data.update({"plant_id": 6}) #update per plant created
    headers = {"Content-type":"application/json"}
    #response = requests.post("http://18.191.162.227:3000/api/sensorUpload", data=json_data, headers=headers)


