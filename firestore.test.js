const { initializeTestEnvironment, assertFails, assertSucceeds } = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;

before(async () => {
  const rules = fs.readFileSync("firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: "demo-no-project",
    firestore: {
      rules,
      host: "127.0.0.1",
      port: 8085,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("unauthenticated user cannot read or write bookmarks", async () => {
  const unauthedDb = testEnv.unauthenticatedContext().firestore();
  const docRef = unauthedDb.collection("users").doc("user123").collection("bookmarks").doc("bm1");

  await assertFails(docRef.get());
  await assertFails(
    docRef.set({
      userId: "user123",
      title: "Test News",
      url: "https://banglatoday24.xyz/test",
    })
  );
});

test("user can create and read their own bookmarks", async () => {
  const aliceDb = testEnv.authenticatedContext("alice").firestore();
  const aliceDoc = aliceDb.collection("users").doc("alice").collection("bookmarks").doc("bm1");

  await assertSucceeds(
    aliceDoc.set({
      userId: "alice",
      title: "Bangla Today Breaking News",
      url: "https://banglatoday24.xyz/breaking-news",
    })
  );

  await assertSucceeds(aliceDoc.get());
});

test("user cannot read or write other user's bookmarks", async () => {
  const aliceDb = testEnv.authenticatedContext("alice").firestore();
  const aliceDoc = aliceDb.collection("users").doc("alice").collection("bookmarks").doc("bm1");
  await aliceDoc.set({
    userId: "alice",
    title: "Alice News",
    url: "https://banglatoday24.xyz/alice",
  });

  const bobDb = testEnv.authenticatedContext("bob").firestore();
  const bobsAccessToAlice = bobDb.collection("users").doc("alice").collection("bookmarks").doc("bm1");

  await assertFails(bobsAccessToAlice.get());
  await assertFails(
    bobsAccessToAlice.set({
      userId: "alice",
      title: "Hacked",
      url: "https://banglatoday24.xyz/hacked",
    })
  );
});
