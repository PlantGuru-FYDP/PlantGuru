const nodemailer = require('nodemailer');

class EmailService {
    constructor() {
        this.transporter = nodemailer.createTransport({
            service: 'gmail',
            auth: {
                user: process.env.EMAIL_USER,
                pass: process.env.EMAIL_APP_PASSWORD
            }
        });
    }

    async sendMoistureAlert(threshold, soil_moisture, userEmail, plantName) {
        try {
            console.log(`[EmailService] Sending moisture alert email to ${userEmail} for ${plantName}`);
            
            const mailOptions = {
                from: `No reply @ Plant Guru <${process.env.EMAIL_USER}>`,
                to: userEmail,
                subject: `Moisture Alert: ${plantName} Needs Water`,
                html: 
                    `
                    <p>The soil moisture has fallen below the dry threshold of ${threshold}. Please water your plant soon.</p>
                    <p>Current soil moisture: ${soil_moisture}</p>
                    `
            };

            const result = await this.transporter.sendMail(mailOptions);
            console.log(`[EmailService] Email sent successfully to ${userEmail}`);
            return result;
        } catch (error) {
            console.error('[EmailService] Error sending email:', error);
            throw error;
        }
    }
}

module.exports = new EmailService();