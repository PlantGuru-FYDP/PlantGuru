module.exports = {
  apps: [{
    name: 'server',
    script: 'server.js',
    env: {
      FIREBASE_PROJECT_ID: process.env.FIREBASE_PROJECT_ID,
      FIREBASE_PRIVATE_KEY: process.env.FIREBASE_PRIVATE_KEY,
      FIREBASE_CLIENT_EMAIL: process.env.FIREBASE_CLIENT_EMAIL,
      RDS_HOSTNAME: process.env.RDS_HOSTNAME,
      RDS_USERNAME: process.env.RDS_USERNAME,
      RDS_PASSWORD: process.env.RDS_PASSWORD,
      RDS_PORT: process.env.RDS_PORT,
      JWT_SECRET: process.env.JWT_SECRET,
    }
  }]
}; 