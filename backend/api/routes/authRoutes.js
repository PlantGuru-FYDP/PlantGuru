let express = require("express");
let router = express.Router();
const { body } = require("express-validator");
// let { tokenVerify } = require("../middlewares/tokenVerify.js");

let { signUp, login, deleteUser } = require("../controllers/authController");

router.post(
  "/signup",
  [
    body("emailAddress")
      .notEmpty()
      .isEmail()
      .withMessage("Please enter a valid email address."),
    body("name").notEmpty().withMessage("Please enter your name."),
    body("password").notEmpty().withMessage("Please enter a password."),
    body("address").notEmpty().withMessage("Please enter your address."),
    body("phone_number")
      .notEmpty()
      .withMessage("Please enter your phone number."),
  ],
  signUp
);

router.post(
  "/login",
  // [
  //   body("user")
  //     .notEmpty()
  //     .withMessage("Please enter valid email address or mobile number"),
  //   body("password").notEmpty().withMessage("Please enter valid Password"),
  // ],
  login
);

router.get("/deleteUser", deleteUser);

module.exports = router;
