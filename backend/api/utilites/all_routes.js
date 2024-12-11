const ALL_ROUTES = {
  sensor: require("../routes/sensorRoutes"),
  watering: require("../routes/wateringEventRoutes"),
  plant: require("../routes/plantRoutes"),
  auth: require("../routes/authRoutes"),
  provisioning: require("../routes/provisioningRoute"),
  ml: require("../routes/mlRoute"),
  insights: require("../routes/insightsRoutes"),
  notification: require("../routes/notificationRoutes"),
  projections: require("../routes/projectionsRoutes"),
  notificationSettings: require("../routes/notificationSettingsRoutes"),
};

module.exports = ALL_ROUTES;
