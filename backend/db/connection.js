const mysql = require("mysql2/promise");
require("dotenv").config();

let pool = null;

async function initializeConnection() {
  try {
    pool = mysql.createPool({
      host: process.env.RDS_HOSTNAME,
      user: process.env.RDS_USERNAME,
      password: process.env.RDS_PASSWORD,
      port: process.env.RDS_PORT,
      database: "PlantGuruDB",
    });

    // test connection real
    await pool.getConnection();
    console.log("Connected to the database");
    return pool;
  } catch (err) {
    console.log("Failed to connect to the database:", err);
    throw err;
  }
}

const connectionPromise = initializeConnection();

module.exports = {
  query: async (...args) => {
    const connection = await connectionPromise;
    return connection.query(...args);
  }
};
