let express = require("express");
const { plantRead, plantUpload } = require("../controllers/plantController");
let router = express.Router();

// const { body } = require("express-validator");

router.post("/plantUpload", plantUpload);

router.get("/plantRead", plantRead);

module.exports = router;
