export const imageGenConfig = {
  // Developer toggle: set to 'mock' to avoid hitting backend; set to 'real' to call API.
  mode: 'mock',
  mockUrl: 'https://placehold.co/640x640/EEE/222?text=Mock+image',
  pollIntervalMs: 2500,
  maxPolls: 150, // ~6.25 minutes at 2.5s interval
}
