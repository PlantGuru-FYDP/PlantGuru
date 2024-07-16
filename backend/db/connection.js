const mysql = require("mysql2/promise");
require("dotenv").config();
let connection;

try {
  connection = mysql.createPool({
    host: process.env.RDS_HOSTNAME,
    user: process.env.RDS_USERNAME,
    password: process.env.RDS_PASSWORD,
    port: process.env.RDS_PORT,
    database: "PlantGuruDB",
  });
} catch (err) {
  console.log(err);
}

console.log("Connected to the database");

module.exports = connection;
