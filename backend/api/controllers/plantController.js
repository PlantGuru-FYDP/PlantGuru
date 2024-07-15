let Plant = require("../models/plantModel");

exports.plantRead = async (req, res) => {
  try {
    const user_id = req.query.user_id;
    const [data] = await Plant.readData(user_id);
    return res.status(200).send(data);
  } catch (err) {
    return res.status(500).send({ message: err });
  }
};

exports.plantUpload = async (req, res) => {
  try {
    let { user_id, plant_name, age, last_watered, next_watering_time } =
      req.body;
    const plant = new Plant(
      user_id,
      plant_name,
      age,
      last_watered,
      next_watering_time
    );

    let checkPlant = await plant.getID();
    if (checkPlant[0].length > 0) {
      return res.status(400).send({ message: "Plant already exists!" });
    }

    await plant.uploadData();
    let id = await plant.getID();
    return res.status(200).send({
      message: "Plant data uploaded",
      plant_id: id[0][0].plant_id,
    });
  } catch (err) {
    return res.status(500).send({ message: err });
  }
};
