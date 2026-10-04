const { initializeApp } = require("firebase-admin/app");
const { getDatabase } = require("firebase-admin/database");
const { getMessaging } = require("firebase-admin/messaging");
const { onValueWritten } = require("firebase-functions/v2/database");

initializeApp();

exports.pushOnIssueAlert = onValueWritten(
  {
    ref: "/FloodSystem/issue_alert",
    instance: "floodsense-3b160-default-rtdb",
    region: "asia-southeast1",
  },
  async (event) => {
    const after = event.data.after.val();
    if (!after || after.decision !== "ISSUE_ALERT") {
      return;
    }
    const title = String(after.title || "Flood alert");
    const body = String(after.body || "HIGH flood risk. Move to safer ground.");
    await getMessaging().send({
      topic: "floodsense_citizens",
      notification: { title, body },
      data: {
        alertId: String(after.id || ""),
        title,
        body,
        stationName: String(after.stationName || ""),
        decision: "ISSUE_ALERT",
      },
    });
    // SMS is sent only for this emergency path, never for watch/warning/login.
    await sendEmergencySms(title, body);
  }
);

async function sendEmergencySms(title, body) {
  const token = process.env.TEXT_LK_API_TOKEN || "";
  if (!token) {
    return;
  }
  const db = getDatabase();
  const [usersSnap, configSnap] = await Promise.all([
    db.ref("users").get(),
    db.ref("FloodSystem/sms_config").get(),
  ]);
  // App sends SMS unless via_function is true. Skip here to avoid double texts.
  if (configSnap.child("via_function").val() !== true) {
    return;
  }
  const maxSends = 2;
  const sendsUsed = Number(configSnap.child("sends_used").val() || 0);
  const remaining = Math.max(0, maxSends - sendsUsed);
  if (remaining <= 0) {
    console.log("SMS quota exhausted (2 demo messages)");
    return;
  }
  const senderId = String(
    process.env.TEXT_LK_SENDER_ID || configSnap.child("sender_id").val() || "FloodSense"
  ).slice(0, 11);
  const recipients = new Set();
  usersSnap.forEach((user) => {
    user.child("emergency_contacts").forEach((contact) => {
      const digits = String(contact.child("phone").val() || "").replace(/\D/g, "");
      if (digits.startsWith("94") && digits.length === 11) {
        recipients.add(digits);
      }
    });
  });
  const list = [...recipients].slice(0, remaining);
  if (list.length === 0) {
    return;
  }
  const message = `EMERGENCY FloodSense: ${title}. ${body}`.slice(0, 160);
  const response = await fetch("https://app.text.lk/api/v3/sms/send", {
    method: "POST",
    headers: {
      Authorization: `Bearer ${token}`,
      "Content-Type": "application/json",
      Accept: "application/json",
    },
    body: JSON.stringify({
      recipient: list.join(","),
      sender_id: senderId,
      type: "plain",
      message,
    }),
  });
  if (!response.ok) {
    console.warn("text.lk SMS failed", response.status);
    return;
  }
  await db.ref("FloodSystem/sms_config").update({
    sends_used: sendsUsed + list.length,
    max_sends: maxSends,
  });
}
