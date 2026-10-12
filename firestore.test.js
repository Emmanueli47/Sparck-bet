const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

beforeEach(async () => {
  await testEnv.clearFirestore();
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

test("unauthenticated access is rejected for user profiles", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
});

test("alice can create and read her own user account", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const aliceDoc = aliceDb.collection("users").doc(ALICE_UID);
  
  await assertSucceeds(aliceDoc.set({
    id: ALICE_UID,
    balance: 1000.0,
    totalWagered: 0.0,
    totalWon: 0.0,
    betsPlaced: 0,
    betsWon: 0,
  }));

  await assertSucceeds(aliceDoc.get());
});

test("bob cannot read or update alice's user account", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await aliceDb.collection("users").doc(ALICE_UID).set({
    id: ALICE_UID,
    balance: 1000.0,
    totalWagered: 0.0,
    totalWon: 0.0,
    betsPlaced: 0,
    betsWon: 0,
  });

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).update({
    balance: 5000.0
  }));
});

test("alice can create and read her bets", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const betDoc = aliceDb.collection("users").doc(ALICE_UID).collection("bets").doc("bet_1");
  
  await assertSucceeds(betDoc.set({
    id: "bet_1",
    userId: ALICE_UID,
    betType: "SINGLE",
    selectionsSummary: "Real Madrid to Win",
    stake: 25.0,
    totalOdds: 1.85,
    potentialPayout: 46.25,
    status: "ACTIVE",
  }));

  await assertSucceeds(betDoc.get());
});

test("bob cannot access alice's bets", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await aliceDb.collection("users").doc(ALICE_UID).collection("bets").doc("bet_1").set({
    id: "bet_1",
    userId: ALICE_UID,
    betType: "SINGLE",
    selectionsSummary: "Real Madrid",
    stake: 25.0,
    totalOdds: 1.85,
    potentialPayout: 46.25,
    status: "ACTIVE",
  });

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("bets").doc("bet_1").get());
});
