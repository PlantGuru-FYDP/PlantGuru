const connection = require("../../db/connection");

class User {
  constructor(name, email, password, address, phone_number, userId = null) {
    this.name = name;
    this.email = email;
    this.password = password;
    this.address = address;
    this.phone_number = phone_number;
    this.userId = userId;
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
  updateUser() {
    const cmd = "UPDATE Users SET name = ?, email = ?, address = ?, phone_number = ? WHERE user_id = ?";
    return connection.query(cmd, [
      this.name,
      this.email,
      this.address,
      this.phone_number,
      this.userId
    ]);
  }
  static async getUserEmail(userId) {
    const cmd = "Select email from Users where user_id = ?";
    // return the email
    const [rows] = await connection.query(cmd, [userId]);
    return rows[0].email;

  }
}

module.exports = User;
