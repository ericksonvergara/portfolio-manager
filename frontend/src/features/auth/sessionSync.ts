const CHANNEL = 'portfolio-manager:auth'
const LOGOUT = 'logout'

/**
 * Tells the other tabs of this browser that the user logged out. Each tab keeps its own access
 * token in memory, and the backend only revokes the one sent with the logout, so the other tabs
 * have to drop theirs.
 */
export function announceLogout() {
  if (!('BroadcastChannel' in window)) {
    return
  }
  const channel = new BroadcastChannel(CHANNEL)
  channel.postMessage(LOGOUT)
  channel.close()
}

/** Calls the handler when another tab logs out; returns the function that stops listening. */
export function onLogoutInAnotherTab(handler: () => void): () => void {
  if (!('BroadcastChannel' in window)) {
    return () => undefined
  }
  const channel = new BroadcastChannel(CHANNEL)
  channel.onmessage = (event: MessageEvent) => {
    if (event.data === LOGOUT) {
      handler()
    }
  }
  return () => channel.close()
}
