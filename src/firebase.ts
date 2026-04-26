import { initializeApp } from "firebase/app";
import { getAuth, GoogleAuthProvider } from "firebase/auth";
import { initializeFirestore } from "firebase/firestore";

const firebaseConfig = {
  apiKey: "AIzaSyDFO83bcPZaci2yk5GNDdYLqszj1IVA-QU",
  authDomain: "omash-3951c.firebaseapp.com",
  projectId: "omash-3951c",
  storageBucket: "omash-3951c.firebasestorage.app",
  messagingSenderId: "887507635237",
  appId: "1:887507635237:web:75cd99ac2d25bafb3203cc",
  measurementId: "G-CPLEVY2YRT"
};

const app = initializeApp(firebaseConfig);
export const auth = getAuth(app);
export const db = initializeFirestore(app, {
  experimentalForceLongPolling: true
});
export const googleProvider = new GoogleAuthProvider();
