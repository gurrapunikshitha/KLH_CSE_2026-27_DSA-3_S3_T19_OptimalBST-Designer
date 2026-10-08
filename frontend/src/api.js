// All communication with the Java backend lives here.
// The frontend never computes the DP; it only sends input and displays the response.

// Override with VITE_API_URL in a .env file if the backend runs elsewhere.
const API_BASE = import.meta.env.VITE_API_URL ?? 'http://localhost:8080';

/**
 * Calls POST /api/obst/solve.
 * Resolves with the solve response, or rejects with an Error whose `messages` is a list
 * of strings to show to the user (validation errors from the backend, or a connection problem).
 */
export async function solveObst(keys, frequencies) {
  let response;
  try {
    response = await fetch(`${API_BASE}/api/obst/solve`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ keys, frequencies }),
    });
  } catch {
    throw withMessages([
      `Could not reach the backend at ${API_BASE}. Start it with "mvn spring-boot:run" in the backend folder.`,
    ]);
  }

  const body = await response.json().catch(() => null);
  if (!response.ok) {
    throw withMessages(body?.errors ?? [`The server returned an error (HTTP ${response.status}).`]);
  }
  return body;
}

function withMessages(messages) {
  const error = new Error(messages.join(' '));
  error.messages = messages;
  return error;
}
