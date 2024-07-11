const connection = require("../../db/connection");

class User {
  constructor(name, email, password, address, phone_number) {
    this.name = name;
    this.email = email;
    this.password = password;
    this.address = address;
    this.phone_number = phone_number;
  }
  createUser() {
    const cmd =
      "INSERT INTO Users (name, email, password, address, phone_number) VALUES (?, ?, ?, ?, ?)";
    return connection.query(cmd, [
      this.name,
      this.email,
      this.password,
      this.address,
      this.phone_number,
    ]);
  }
  findUser() {
    const cmd = "Select * from Users where email = ?";
    return connection.query(cmd, [this.email]);
  }
  getId() {
    const cmd = "Select user_id from Users where email = ?";
    return connection.query(cmd, [this.email]);
  }
}

module.exports = User;
