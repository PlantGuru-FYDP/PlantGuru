const jwt = require("jsonwebtoken");
require('dotenv').config();

exports.tokenVerify = (req, res, next) => {
  const token = req.headers.authorization?.split(' ')[1]; // Bearer TOKEN

  if (!token) {
    return res.status(401).send({ message: 'No token provided' });
  }

  try {
    const decoded = jwt.verify(token, process.env.JWT_SECRET);
    req.user_id = decoded.user_id;
    next();
  } catch (err) {
    return res.status(403).send({ message: 'Invalid token' });
  }
};
