let bcrypt = require("bcrypt");
const User = require("../models/userModel");
const jwt = require("jsonwebtoken");
require("dotenv").config();
const NotificationSettings = require('../models/notificationSettingsModel');
const { DEFAULT_USER_SETTINGS } = require('../constants/defaultSettings');

exports.signUp = async (req, res) => {
  try {
    let { name, email, password, address, phoneNumber } = req.body;
    let newUser = new User(name, email, password, address, phoneNumber);

    let checkUser = await newUser.findUser();
    if (checkUser[0].length > 0) {
      return res
        .status(400)
        .send({ message: "User already exists, try logging in" });
    }

    let salt = await bcrypt.genSalt(10);
    newUser.password = await bcrypt.hash(newUser.password, salt);

    await newUser.createUser();
    let id = await newUser.getId();
    const user_id = id[0][0].user_id;

    const defaultUserSettings = {
      userId: user_id,
      email: email,
      ...DEFAULT_USER_SETTINGS
    };
    
    await NotificationSettings.createUserSettings(user_id, defaultUserSettings);
    console.log(`[signUp] Created default notification settings for user_id: ${user_id}`);
    
    const token = jwt.sign({ user_id }, process.env.JWT_SECRET);

    return res.status(200).send({
      message: "User created successfully",
      user_id: user_id,
      token: token,
      user: {
        name,
        email,
        address,
        phone_number: phoneNumber
      }
    });
  } catch (err) {
    console.error('[signUp] Error:', err);
    return res.status(500).send({ message: "Internal server error: " + err.message });
  }
};

exports.login = async (req, res) => {
  try {
    let { email, password } = req.body;
    let newUser = new User("", email, "", "", "");

    let checkUser = await newUser.findUser();
    if (checkUser[0].length === 0) {
      return res
        .status(400)
        .send({ message: "User does not exist, consider signing up" });
    }

    let checkPassword = await bcrypt.compare(
      password,
      checkUser[0][0].password
    );
    if (!checkPassword) {
      return res
        .status(400)
        .send({ message: "Incorrect password, try again!" });
    }

    const user_id = checkUser[0][0].user_id;
    const token = jwt.sign({ user_id }, process.env.JWT_SECRET);

    return res.status(200).send({
      message: "User logged in successfully",
      token: token,
      user_id: user_id,
      user: {
        name: checkUser[0][0].name,
        email: checkUser[0][0].email,
        address: checkUser[0][0].address,
        phone_number: checkUser[0][0].phone_number
      }
    });
  } catch (err) {
    return res.status(500).send({ message: `Internal server error: ${err.message}` });
  }
};

exports.updateUser = async (req, res) => {
  try {
    const user_id = req.user_id;
    const { name, email, address, phone_number } = req.body;
    
    if (!user_id) {
      return res.status(400).send({ message: "Authentication required" });
    }

    const user = new User(name, email, null, address, phone_number, user_id);
    await user.updateUser();

    return res.status(200).send({ 
      message: "User updated successfully",
      user: {
        name,
        email,
        address,
        phone_number
      }
    });
  } catch (err) {
    return res.status(500).send({ message: "Internal server error: " + err.message });
  }
};
