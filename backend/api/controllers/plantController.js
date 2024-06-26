let Plant = require("../models/plantModel");

// TODO: There needs to be functionality here to verify that token we send belongs to user
// We verify in the middleware
exports.plantRead = (req, res) => {
  try {
    const user_id = req.user.user_id;
    const data = Plant.readData(user_id);
    return res.status(200).send(data);
  } catch (err) {
    return res.status(500).send({ message: err });
  }
};

exports.plantUpload = async (req, res) => {
  try {
    const { user_id, plant_name, age, last_watered, next_watering_time } =
      req.body;
    const values = [user_id, plant_name, age, last_watered, next_watering_time];

    await Plant.uploadData(values);

    return res.status(200).send({ message: "Plant data uploaded" });
  } catch (err) {
    return res.status(500).send({ message: err });
  }
};
