//Require all dependencies
let express = require("express");
let app = express();
var bodyParser = require("body-parser");
let cors = require("cors");
const mysql = require("mysql");

app.use(bodyParser.urlencoded({ extended: false }));
app.use(bodyParser.json());
app.use(express.json());
app.use(express.urlencoded({ extended: false }));
app.use(cors());

const db = mysql.createConnection({
  
  // put official values here
  host: config.DB_HOST,
  user: config.DB_USER,
  password: config.DB_PASSWORD,
  database: config.DB_NAME
});

db.connect((err) => {
  if (err) {
      console.error('Database connection failed: ' + err.stack);
      return;
  }
  console.log('Connected to database.');
});

app.use(
  bodyParser.urlencoded({
    extended: true,
  })
);

//handling invalid api
app.use((error, req, res, next) => {
  //console.log(error);
  const statusCode = error.statusCode || 500;
  const message = error.message || error;
  res.status(statusCode).json({ message });
});


app.post('/updateSensorData', (req, res) => { // CHANGE THE ROUTE TO WHATEVER IS DECIDED IN THE END

  // Getting the sensor id and other data fields(in the future there will be more data fields)
  const { sensor_id, value } = req.body;
  // checking if sensor_id is not present or data is null
  if (!sensor_id || value === undefined) {
      return res.status(400).json({ error: 'Invalid request payload' });
  }

  const query = 'INSERT INTO SensorData (sensor_id, value) VALUES (?, ?)';
  db.query(query, [sensor_id, value], (err, results) => {
      if (err) {
          return res.status(500).json({ error: err.message });
      }
      res.status(201).json({ message: 'Data added successfully', id: results.insertId });
  });
});

//ALL APIS
const ALL_ROUTES = require("./api/utilites/all_routes");
app.use(ALL_ROUTES.auth);
app.use(ALL_ROUTES.userInfo);

//Listening to the port
app.listen(3000, () => console.log("Connected to port 3000"));
