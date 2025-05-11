/**
 * Import function triggers from their respective submodules:
 *
 * const {onCall} = require("firebase-functions/v2/https");
 * const {onDocumentWritten} = require("firebase-functions/v2/firestore");
 *
 * See a full list of supported triggers at https://firebase.google.com/docs/functions
 */

const {onRequest} = require("firebase-functions/v2/https");
const logger = require("firebase-functions/logger");
const {onDocumentWritten} = require("firebase-functions/v2/firestore");
const functions = require('firebase-functions');
const admin = require('firebase-admin');
const fetch = require('node-fetch');

admin.initializeApp();

exports.analyzeHealthData = functions.https.onRequest(async (req, res) => {
  try {

    const userId = req.query.userId;

    if (!userId) {
      return res.status(400).send('Missing userId parameter.');
    }

    const healthDataSnapshot = await admin.database().ref(`/healthData/${userId}`).once('value');

    const healthData = healthDataSnapshot.val();

    if (!healthData) {
      return res.status(404).send(`No health data found for user with ID: ${userId}.`);
    }

    const prompt = `Analyze the following health data of the user: ${JSON.stringify(healthData)}. Summarize the main trends and provide 2-3 recommendations. Provide the answer in Slovak.`;

    const geminiApiKey = functions.config().gemini.api_key;
    const geminiApiUrl = 'https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key=' + geminiApiKey;

    const geminiResponse = await fetch(geminiApiUrl, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        contents: [{
          parts: [{
            text: prompt
          }]
        }]
      }),
    });

    const geminiResult = await geminiResponse.json();

    if (!geminiResponse.ok || !geminiResult.candidates || geminiResult.candidates.length === 0 || !geminiResult.candidates[0].content || !geminiResult.candidates[0].content.parts || geminiResult.candidates[0].content.parts.length === 0) {
      console.error('Error calling Gemini API:', geminiResult);
      return res.status(500).send('Error processing response from AI.');
    }

    const recommendations = geminiResult.candidates[0].content.parts[0].text;

    await admin.database().ref(`/recommendations/${userId}`).set({ recommendations: recommendations });

    return res.status(200).send(`Recommendations were generated and saved for user with ID: ${userId}: ${recommendations}`);

  } catch (error) {
    console.error('Error analyzing health data:', error);
    return res.status(500).send(`Error analyzing health data: ${error.message}`);
  }
});


// Create and deploy your first functions
// https://firebase.google.com/docs/functions/get-started

// exports.helloWorld = onRequest((request, response) => {
//   logger.info("Hello logs!", {structuredData: true});
//   response.send("Hello from Firebase!");
// });
