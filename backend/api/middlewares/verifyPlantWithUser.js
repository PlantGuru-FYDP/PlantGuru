let Plant = require("../models/plantModel");

// Verify if plant id belongs to the user and only allows
exports.verifyPlantWithUser = async (req, res, next) => {
  try {
    // We get this from exctracting the token in the tokenVerify middleware
    const user_id = req.user_id;
    const plants = await Plant.readData(user_id);
    let belongsToUser = false;
    for (let plant of plants[0]) {
      if (plant.plant_id === req.body.plant_id) {
        belongsToUser = true;
        break;
      }
    }
    if (belongsToUser) {
      return next();
    }
    return res.status(400).send({ error: "Plant ID does not belong to user!" });
  } catch (err) {
    return res.status(500).send({ message: err });
  }
};
