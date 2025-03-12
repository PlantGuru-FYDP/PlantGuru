let express = require("express");
const { plantRead, plantUpload, plantDelete, updatePlant, getAllPlants } = require("../controllers/plantController");
let router = express.Router();

// const { body } = require("express-validator");

router.post("/plantUpload", plantUpload);

router.get("/plantRead", plantRead);

router.delete("/deletePlant", plantDelete);

router.put("/updatePlant", updatePlant);

router.get("/allPlants", getAllPlants);

module.exports = router;
