//Require all dependencies
let express = require("express");
let app = express();
var bodyParser = require("body-parser");
let cors = require("cors");

app.use(bodyParser.urlencoded({ extended: false }));
app.use(bodyParser.json());
app.use(express.json());
app.use(express.urlencoded({ extended: false }));
app.use(cors());

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

//ALL APIS
// const ALL_ROUTES = require("./api/utilites/all_routes");
// app.use(ALL_ROUTES.auth);
// app.use(ALL_ROUTES.userInfo);

//Listening to the port
app.listen(3000, () => console.log("Connected to port 3000"));
