const jwt = require("jsonwebtoken");
require("dotenv").config();

exports.tokenVerify = (req, res, next) => {
    try {
        const token = req.headers.authorization?.split(" ")[1];
        if (!token) {
            return res.status(401).send({ message: "No token provided" });
        }

        const decoded = jwt.verify(token, process.env.JWT_SECRET);
        if (!decoded.user_id) {
            return res.status(401).send({ message: "Invalid token format" });
        }

        req.user_id = decoded.user_id;
        next();
    } catch (err) {
        console.error("[tokenVerify] Error:", err);
        return res.status(401).send({ message: "Invalid token" });
    }
};
