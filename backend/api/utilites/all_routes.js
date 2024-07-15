const ALL_ROUTES = {
  sensor: require("../routes/sensorRoutes"),
  watering: require("../routes/wateringEventRoutes"),
  plant: require("../routes/plantRoutes"),
  auth: require("../routes/authRoutes"),
  provisioning: require("../routes/provisioningRoute"),
};

module.exports = ALL_ROUTES;
