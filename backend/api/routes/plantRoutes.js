let express = require("express");
const { plantRead, plantUpload, plantDelete } = require("../controllers/plantController");
let router = express.Router();

// const { body } = require("express-validator");

router.post("/plantUpload", plantUpload);

router.get("/plantRead", plantRead);

router.delete("/deletePlant", plantDelete);

module.exports = router;
