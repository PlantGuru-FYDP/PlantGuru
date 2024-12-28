const admin = require('firebase-admin');
require('dotenv').config();

// Debug logging
console.log('Firebase Config Check:');
console.log('Project ID exists:', !!process.env.FIREBASE_PROJECT_ID);
console.log('Client Email exists:', !!process.env.FIREBASE_CLIENT_EMAIL);
console.log('Private Key exists:', !!process.env.FIREBASE_PRIVATE_KEY);

// Handle GitHub Actions secret format
const privateKey = process.env.FIREBASE_PRIVATE_KEY ? 
    process.env.FIREBASE_PRIVATE_KEY.replace(/\\n/g, '\n')
        .replace(/"/g, '')
        .replace(/^'(.*)'$/, '$1') // Remove any surrounding single quotes
        .trim() : undefined;

// Add more detailed logging for debugging
console.log('Private Key Format Check:');
console.log('Length:', privateKey?.length);
console.log('First char:', privateKey?.[0]);
console.log('Last char:', privateKey?.[privateKey?.length - 1]);
console.log('Contains BEGIN:', privateKey?.includes('BEGIN PRIVATE KEY'));
console.log('Contains END:', privateKey?.includes('END PRIVATE KEY'));

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