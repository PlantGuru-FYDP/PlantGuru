let { getData, uploadData } = require("../utilites/helper");
let bcrypt = require("bcrypt");

// const jwt = require("jsonwebtoken");

// Ideally, we should verify the user when signing up by sending an email to verify but for this project might be overkill

exports.signUp = async (req, res) => {
  try {
    // let reqData = req.body;
    // cmd = `Select * from Users where email = '${reqData.emailAddress}'`;
    // [errMsg, result] = getData(cmd);
    // if (errMsg) {
    //   return res.status(500).send({
    //     message:
    //       "Something went wrong when looking up existing users in signup, look into it! " +
    //       errMsg,
    //   });
    // }
    // if (result) {
    //   return res
    //     .status(409)
    //     .send({ message: "The user already exists, try logging in" });
    // }
    // const { name, emailAddress, password, address, phone_number } = reqData;
    // // Hash the password
    // //let salt = await bcrypt.genSalt(10);
    // //hashedPass = await bcrypt.hash(password, salt);
    // const values = [name, address, phone_number, emailAddress, password];
    // cmd = `INSERT INTO Users (name, address, phone_number, emailAddress, password) VALUES ?`;
    // [errMsg] = uploadData(cmd, values);
    // if (errMsg) {
    //   return res.status(500).send({ message: errMsg });
    // }
    // return res.status(201).send({ message: "User created successfully!" });
  } catch (err) {
    return res.status(500).send({ message: "Internal server error" + err });
  }
};

exports.login = async (req, res) => {
  console.log("You have hit the login endpoint");
};

exports.deleteUser = async (req, res) => {
  console.log("You have hit the delete User endpoint");
};
