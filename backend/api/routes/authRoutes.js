const express = require("express");
const router = express.Router();
const { body } = require("express-validator");
const { tokenVerify } = require("../middlewares/tokenVerify");
const { signUp, login, updateUser } = require("../controllers/authController");

router.post("/signup", [
  body("name")
    .trim()
    .isLength({ min: 2, max: 50 })
    .withMessage("Name must be between 2 and 50 characters"),
  body("email")
    .trim()
    .isEmail()
    .normalizeEmail()
    .withMessage("Please enter a valid email address"),
  body("password")
    .isLength({ min: 6, max: 50 })
    .withMessage("Password must be at least 6 characters long")
    .matches(/^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d@$!%*#?&]/)
    .withMessage("Password must contain at least one letter and one number"),
  body("phoneNumber")
    .optional()
    .matches(/^\d{10}$/)
    .withMessage("Phone number must be 10 digits"),
  body("address")
    .optional()
    .trim()
    .isLength({ max: 200 })
    .withMessage("Address cannot exceed 200 characters")
], signUp);

router.post("/login", [
  body("email")
    .trim()
    .isEmail()
    .normalizeEmail()
    .withMessage("Please enter a valid email address"),
  body("password")
    .notEmpty()
    .withMessage("Password is required")
], login);

router.put("/updateUser", [
  tokenVerify,
  body("email")
    .trim()
    .isEmail()
    .normalizeEmail()
    .withMessage("Please enter a valid email address"),
  body("name")
    .trim()
    .isLength({ min: 2, max: 50 })
    .withMessage("Name must be between 2 and 50 characters"),
  body("phoneNumber")
    .optional()
    .matches(/^\d{10}$/)
    .withMessage("Phone number must be 10 digits"),
  body("address")
    .optional()
    .trim()
    .isLength({ max: 200 })
    .withMessage("Address cannot exceed 200 characters")
], updateUser);

module.exports = router;
