const admin = require('firebase-admin');
const nodemailer = require('nodemailer');
const serviceAccount = require('serviceAccountKey.json'); 

// Initialise le SDK admin
admin.initializeApp({
  credential: admin.credential.cert(serviceAccount),
});

// Configure ton email d'envoi (ex: Gmail)
const transporter = nodemailer.createTransport({
  service: 'gmail',
  auth: {
    user: 'rihemaridhi507@gmail.com',
    pass: 'la lune123', // voir "mot de passe application" Gmail
  },
});

async function sendResetEmails() {
  const users = await admin.auth().listUsers(1000); // jusqu’à 1000 utilisateurs
  for (const user of users.users) {
    const email = user.email;
    if (email) {
      try {
        const link = await admin.auth().generatePasswordResetLink(email);
        await transporter.sendMail({
          from: '"Admin Parking App" <tonemail@gmail.com>',
          to: email,
          subject: 'Réinitialisation de votre mot de passe',
          html: `<p>Bonjour,<br/>Cliquez sur ce lien pour réinitialiser votre mot de passe : <a href="${link}">Réinitialiser</a></p>`,
        });
        console.log(`Envoyé à : ${email}`);
      } catch (err) {
        console.error(` Échec pour ${email} : ${err.message}`);
      }
    }
  }
}

sendResetEmails();
