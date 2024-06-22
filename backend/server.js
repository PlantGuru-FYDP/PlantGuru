//Require all dependencies
let express = require("express");
let app = express();
var bodyParser = require("body-parser");
var cookieParser = require("cookie-parser");
let cors = require("cors");

app.use(bodyParser.urlencoded({ extended: false }));
app.use(bodyParser.json());
app.use(express.json());
app.use(express.urlencoded({ extended: false }));
app.use(cookieParser());
app.use(cors());

app.use(
  bodyParser.urlencoded({
    extended: true,
  })
);

app.get("/test", (req, res) => {
  res.send("Should change automatically now");
});

//handling invalid api
app.use((error, req, res, next) => {
  //console.log(error);
  const statusCode = error.statusCode || 500;
  const message = error.message || error;
  res.status(statusCode).json({ message });
});

//ALL APIS
const ALL_ROUTES = require("./api/utilites/all_routes");
app.use(ALL_ROUTES.dataUpload);
// Expose other endpoints as they become available

//Listening to the port
app.listen(3000, () => console.log("Connected to port 3000"));
