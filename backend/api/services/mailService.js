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

    async sendMoistureAlert(userEmail, plantName) {
        try {
            console.log(`[EmailService] Sending moisture alert email to ${userEmail} for ${plantName}`);
            
            const mailOptions = {
                from: `"Plant Guru" <${process.env.EMAIL_USER}>`,
                to: userEmail,
                subject: `Moisture Alert: ${plantName} Needs Water`,
                html: 
                    `
                    <p>The soil moisture has fallen below the dry threshold. Please water your plant soon.</p>
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