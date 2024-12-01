let bcrypt = require("bcrypt");
const User = require("../models/userModel");
const jwt = require("jsonwebtoken");
require("dotenv").config();

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
    return res.status(200).send({
      message: "User created with successfully",
      user_id: id[0][0].user_id,
    });
  } catch (err) {
    return res.status(500).send({ message: "Internal server error" + err });
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
    let id = await newUser.getId();
    let token = jwt.sign({ user_id: id[0][0].user_id }, process.env.JWT_SECRET);
    return res.status(200).send({
      message: "User logged in successfully",
      token: token,
      user_id: id[0][0].user_id,
    });
  } catch (err) {
    return res.status(500).send({ message: `Internal server error: ${err.message}` });
  }
};

exports.deleteUser = async (req, res) => {
  console.log("You have hit the delete User endpoint");
};

exports.updateUser = async (req, res) => {
  try {
    const { name, email, address, phone_number, user_id } = req.body;
    
    if (!user_id) {
      return res.status(400).send({ message: "User ID is required" });
    }

    const user = new User(name, email, null, address, phone_number);
    user.userId = user_id;
    
    await user.updateUser();
    return res.status(200).send({ message: "User updated successfully" });
  } catch (err) {
    return res.status(500).send({ message: "Internal server error: " + err.message });
  }
};
