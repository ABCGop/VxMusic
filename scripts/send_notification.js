#!/usr/bin/env node
/**
 * Firebase Cloud Messaging - Notification Sender (Node.js)
 * Simple script to send notifications to VxMusic users
 * 
 * Setup:
 *   1. npm install firebase-admin
 *   2. Download serviceAccountKey.json from Firebase Console
 *   3. Place it in the same directory as this script
 *   4. Run: node send_notification.js
 */

const admin = require('firebase-admin');
const readline = require('readline');

// Initialize Firebase Admin SDK
try {
  const serviceAccount = require('./serviceAccountKey.json');
  admin.initializeApp({
    credential: admin.credential.cert(serviceAccount)
  });
  console.log('✓ Firebase initialized successfully');
} catch (error) {
  console.error('✗ Error initializing Firebase:', error.message);
  console.log('\nMake sure you have:');
  console.log('1. Downloaded serviceAccountKey.json from Firebase Console');
  console.log('2. Placed it in the same directory as this script');
  console.log('3. Run: npm install firebase-admin');
  process.exit(1);
}

// Utility to create readline interface
const rl = readline.createInterface({
  input: process.stdin,
  output: process.stdout
});

// Promisify question
const question = (query) => new Promise((resolve) => rl.question(query, resolve));

/**
 * Send notification to all users via 'all_users' topic
 */
async function sendToAllUsers(title, body, imageUrl = null) {
  const message = {
    notification: {
      title: title,
      body: body,
      ...(imageUrl && { imageUrl: imageUrl })
    },
    data: {
      type: 'announcement',
      timestamp: Date.now().toString()
    },
    topic: 'all_users'
  };

  try {
    const response = await admin.messaging().send(message);
    console.log('✓ Successfully sent to all users:', response);
    return response;
  } catch (error) {
    console.error('✗ Error sending notification:', error);
    return null;
  }
}

/**
 * Send announcement to all users
 */
async function sendAnnouncement(title, body) {
  console.log('\nSending announcement...');
  console.log('Title:', title);
  console.log('Body:', body);

  const message = {
    notification: {
      title: title,
      body: body
    },
    data: {
      type: 'announcement'
    },
    topic: 'all_users'
  };

  const response = await admin.messaging().send(message);
  console.log('✓ Sent successfully:', response);
}

/**
 * Send update notification
 */
async function sendUpdateNotification(version, features) {
  console.log(`\nSending update notification for v${version}...`);

  const message = {
    notification: {
      title: `VxMusic v${version} Available!`,
      body: `New features: ${features}`
    },
    data: {
      type: 'update_available',
      version: version,
      click_action: 'UPDATE'
    },
    topic: 'all_users'
  };

  const response = await admin.messaging().send(message);
  console.log('✓ Sent successfully:', response);
}

/**
 * Send new feature announcement
 */
async function sendNewFeature(title, body, deepLink = null) {
  console.log('\nSending new feature notification...');

  const data = {
    type: 'new_feature',
    click_action: 'FEATURE'
  };

  if (deepLink) {
    data.deep_link = deepLink;
  }

  const message = {
    notification: {
      title: title,
      body: body
    },
    data: data,
    topic: 'all_users'
  };

  const response = await admin.messaging().send(message);
  console.log('✓ Sent successfully:', response);
}

/**
 * Send notification to specific topic
 */
async function sendToTopic(topic, title, body, notificationType = 'custom') {
  console.log(`\nSending to topic '${topic}'...`);

  const message = {
    notification: {
      title: title,
      body: body
    },
    data: {
      type: notificationType
    },
    topic: topic
  };

  const response = await admin.messaging().send(message);
  console.log('✓ Sent successfully:', response);
}

/**
 * Send notification to specific device
 */
async function sendToDevice(token, title, body) {
  console.log(`\nSending to device: ${token.substring(0, 20)}...`);

  const message = {
    notification: {
      title: title,
      body: body
    },
    data: {
      type: 'announcement'
    },
    token: token
  };

  const response = await admin.messaging().send(message);
  console.log('✓ Sent successfully:', response);
}

/**
 * Interactive mode for sending notifications
 */
async function interactiveMode() {
  console.log('\n' + '='.repeat(60));
  console.log('VxMusic Notification Sender');
  console.log('='.repeat(60));

  let running = true;

  while (running) {
    console.log('\n📱 What would you like to send?');
    console.log('1. Announcement to all users');
    console.log('2. Update notification');
    console.log('3. New feature announcement');
    console.log('4. Send to specific topic');
    console.log('5. Send to specific device');
    console.log('6. Exit');

    const choice = await question('\nEnter choice (1-6): ');

    try {
      switch (choice.trim()) {
        case '1': {
          const title = await question('Enter title: ');
          const body = await question('Enter body: ');
          await sendAnnouncement(title.trim(), body.trim());
          break;
        }
        case '2': {
          const version = await question('Enter version (e.g., 2.0): ');
          const features = await question('Enter features: ');
          await sendUpdateNotification(version.trim(), features.trim());
          break;
        }
        case '3': {
          const title = await question('Enter title: ');
          const body = await question('Enter body: ');
          const deepLink = await question('Enter deep link (optional, press Enter to skip): ');
          await sendNewFeature(title.trim(), body.trim(), deepLink.trim() || null);
          break;
        }
        case '4': {
          const topic = await question('Enter topic name: ');
          const title = await question('Enter title: ');
          const body = await question('Enter body: ');
          await sendToTopic(topic.trim(), title.trim(), body.trim());
          break;
        }
        case '5': {
          const token = await question('Enter device FCM token: ');
          const title = await question('Enter title: ');
          const body = await question('Enter body: ');
          await sendToDevice(token.trim(), title.trim(), body.trim());
          break;
        }
        case '6':
          console.log('\n👋 Goodbye!');
          running = false;
          break;
        default:
          console.log('❌ Invalid choice. Please try again.');
      }
    } catch (error) {
      console.error('Error:', error.message);
    }
  }

  rl.close();
  process.exit(0);
}

// Main execution
if (require.main === module) {
  const args = process.argv.slice(2);

  if (args.length > 0) {
    if (args[0] === '--example') {
      // Send example notification
      console.log('Sending example notification...');
      sendAnnouncement(
        'Welcome to VxMusic!',
        'Enjoy unlimited music streaming'
      ).then(() => process.exit(0));
    } else if (args[0] === '--update') {
      // Send update notification
      sendUpdateNotification('2.0', 'Offline mode, better UI, bug fixes')
        .then(() => process.exit(0));
    } else {
      console.log(`Unknown argument: ${args[0]}`);
      console.log('Usage: node send_notification.js [--example|--update]');
      process.exit(1);
    }
  } else {
    // Interactive mode
    interactiveMode();
  }
}

module.exports = {
  sendToAllUsers,
  sendAnnouncement,
  sendUpdateNotification,
  sendNewFeature,
  sendToTopic,
  sendToDevice
};
