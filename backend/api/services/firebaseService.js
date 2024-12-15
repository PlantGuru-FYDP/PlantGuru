const admin = require('firebase-admin');
require('dotenv').config();

// Debug logging
console.log('Firebase Config Check:');
console.log('Project ID exists:', !!process.env.FIREBASE_PROJECT_ID);
console.log('Client Email exists:', !!process.env.FIREBASE_CLIENT_EMAIL);
console.log('Private Key exists:', !!process.env.FIREBASE_PRIVATE_KEY);
console.log('Private Key starts with:', process.env.FIREBASE_PRIVATE_KEY?.substring(0, 20));

const privateKey = process.env.FIREBASE_PRIVATE_KEY ? 
    process.env.FIREBASE_PRIVATE_KEY.replace(/\\n/g, '\n') : undefined;

if (!privateKey) {
    console.error('Firebase private key is missing or malformed');
    process.exit(1);
}

try {
    admin.initializeApp({
        credential: admin.credential.cert({
            projectId: process.env.FIREBASE_PROJECT_ID,
            clientEmail: process.env.FIREBASE_CLIENT_EMAIL,
            privateKey: privateKey
        })
    });
    console.log('Firebase initialized successfully');
} catch (error) {
    console.error('Firebase initialization error:', error);
    process.exit(1);
}

const messaging = admin.messaging();

module.exports = { admin, messaging };