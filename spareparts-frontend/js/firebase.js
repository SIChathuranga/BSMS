import { initializeApp } from 'https://www.gstatic.com/firebasejs/10.12.2/firebase-app.js';
import { getAuth, GoogleAuthProvider, signInWithPopup } from 'https://www.gstatic.com/firebasejs/10.12.2/firebase-auth.js';

// Firebase config for your project
const firebaseConfig = {
  apiKey: "AIzaSyCeQB04PkgVJ7lXveCMF0g83uIJtZr49wA",
  authDomain: "bsms-project.firebaseapp.com",
  projectId: "bsms-project",
  storageBucket: "bsms-project.appspot.com",
  messagingSenderId: "889312176803",
  appId: "1:889312176803:web:a1aa50b15058ffd7f66d16",
  measurementId: "G-L740JC7MME"
};

export const app = initializeApp(firebaseConfig);
export const auth = getAuth(app);
export const provider = new GoogleAuthProvider();

export async function loginWithGoogle() {
  await signInWithPopup(auth, provider);
}

export async function authorizedFetch(url, options = {}) {
  const headers = { ...(options.headers || {}) };
  const user = auth.currentUser;
  if (user) headers.Authorization = `Bearer ${await user.getIdToken()}`;
  return fetch(url, { ...options, headers });
}
