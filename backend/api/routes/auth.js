let express = require("express");
let router = express.Router();
const { body } = require("express-validator");
let { tokenVerify } = require("../middlewares/tokenVerify");

let { signUp, login, verifyOtp, deleteUser } = require("../controllers/auth");

router.post(
  "/signup",
  [
    body("emailAddress")
      .notEmpty()
      .isEmail()
      .withMessage("Please enter a valid email address."),
    body("name").notEmpty().withMessage("Please enter your name."),
    body("password").notEmpty().withMessage("Please enter a password."),
  ],
  signUp
);

router.post(
  "/login",
  [
    body("user")
      .notEmpty()
      .withMessage("Please enter valid email address or mobile number"),
    body("password").notEmpty().withMessage("Please enter valid Password"),
  ],
  login
);

router.get("/deleteUser", tokenVerify, deleteUser);

module.exports = router;
