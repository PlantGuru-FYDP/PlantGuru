const jwt = require("jsonwebtoken");
require("dotenv").config();

exports.provision = (req, res) => {
  try {
    let token = jwt.sign(
      { plant_id: req.body.plant_id },
      process.env.JWT_SECRET
    );
    return res.status(200).send({
      message: "Provisioning successful",
      token: token,
    });
  } catch (err) {
    return res.status(500).send({ message: "Internal server error" + err });
  }
};
