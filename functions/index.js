const admin = require("firebase-admin");
const {Timestamp} = require("firebase-admin/firestore");
const {onSchedule} = require("firebase-functions/v2/scheduler");
const logger = require("firebase-functions/logger");

admin.initializeApp();

const db = admin.firestore();
const messaging = admin.messaging();

async function processUnlockNotifications() {
  const now = Timestamp.now();
  const summary = {
    checked: 0,
    notified: 0,
    skippedMissingUserId: 0,
    skippedMissingToken: 0,
    sendFailures: 0,
  };

  const snapshot = await db.collection("capsules")
      .where("isUnlocked", "==", false)
      .where("unlockTime", "<=", now)
      .get();

  summary.checked = snapshot.size;

  if (snapshot.empty) {
    logger.info("No capsules ready to unlock.");
    return summary;
  }

  for (const doc of snapshot.docs) {
    const capsule = doc.data();
    const userId = capsule.userId;

    if (!userId) {
      summary.skippedMissingUserId += 1;
      logger.warn("Skipping capsule without userId", {capsuleId: doc.id});
      continue;
    }

    const userDoc = await db.collection("users").doc(userId).get();
    const fcmToken = userDoc.get("fcmToken");

    if (!fcmToken) {
      summary.skippedMissingToken += 1;
      logger.warn("Skipping user without FCM token", {capsuleId: doc.id, userId});
      continue;
    }

    const title = capsule.title || "Time Capsule";
    const body = `"${title}" is now unlocked.`;

    try {
      await messaging.send({
        token: fcmToken,
        notification: {
          title: "Capsule unlocked",
          body: body,
        },
        data: {
          capsuleId: doc.id,
          title: "Capsule unlocked",
          body: body,
        },
      });

      await doc.ref.update({
        isUnlocked: true,
      });

      summary.notified += 1;
      logger.info("Sent unlock notification", {capsuleId: doc.id, userId});
    } catch (error) {
      summary.sendFailures += 1;
      logger.error("Failed to send unlock notification", {
        capsuleId: doc.id,
        userId,
        error: error.message,
      });
    }
  }

  return summary;
}

exports.sendUnlockNotifications = onSchedule("every 60 minutes", async () => {
  return processUnlockNotifications();
});
