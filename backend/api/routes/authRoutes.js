let express = require("express");
let router = express.Router();
const { body } = require("express-validator");
// let { tokenVerify } = require("../middlewares/tokenVerify.js");

let { signUp, login, deleteUser, updateUser } = require("../controllers/authController");

router.post("/signup", signUp);

router.post("/login", login);

router.get("/deleteUser", deleteUser);

router.put("/updateUser", updateUser);

module.exports = router;
