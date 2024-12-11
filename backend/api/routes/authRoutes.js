const express = require("express");
const router = express.Router();
const { body } = require("express-validator");
const { tokenVerify } = require("../middlewares/tokenVerify");
const { signUp, login, updateUser } = require("../controllers/authController");

router.post("/signup", [
  body("email").isEmail(),
  body("password").isLength({ min: 6 }),
  body("name").notEmpty()
], signUp);

router.post("/login", [
  body("email").isEmail(),
  body("password").notEmpty()
], login);

router.put("/updateUser", [
  tokenVerify,
  body("email").isEmail(),
  body("name").notEmpty()
], updateUser);

module.exports = router;
