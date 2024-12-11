//Require all dependencies
const express = require("express");
const app = express();
const bodyParser = require("body-parser");
const cookieParser = require("cookie-parser");
const cors = require("cors");

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

//ALL APIS
const ALL_ROUTES = require("./api/utilites/all_routes");

// TODO remove these routes to another file as they will grow
app.use("/api", ALL_ROUTES.sensor);
app.use("/api", ALL_ROUTES.watering);
app.use("/api", ALL_ROUTES.plant);
app.use("/api", ALL_ROUTES.auth);
app.use("/api", ALL_ROUTES.provisioning);
app.use("/api", ALL_ROUTES.ml);
app.use("/api", ALL_ROUTES.notification);
app.use("/api", ALL_ROUTES.insights);
app.use("/api", ALL_ROUTES.projections);
app.use("/api", ALL_ROUTES.notificationSettings);

app.get("/test", (req, res) => {
  res.send("Should change automatically now");
});

//handling invalid api
app.use((error, req, res, next) => {
  const statusCode = error.statusCode || 500;
  const message = error.message || error;
  res.status(statusCode).json({ message });
});

//Listening to the port
app.listen(3000, () => console.log("Listening on port 3000"));
