# Green Button — PRD

## Problem Statement

Discord and similar VOIP tools are bloated with features most friend groups never use — text channels, bots, roles, activity feeds, nitro. Users who only want to hop on a voice call with friends are forced to navigate a complex UI and accept heavy resource usage just to talk. There is no lightweight, purpose-built desktop VOIP app that makes "are you in the call or not?" the entire product.

## Solution

Green Button is a desktop VOIP app for small friend groups. It does one thing: voice calls. Users create named groups, invite friends by username, and drop in or out of that group's persistent voice room at any time. A floating always-on-top overlay shows a green or red avatar — green means your mic is live, red means muted. Push-to-talk is the default. No text, no bots, no subscriptions.

## User Stories

1. As a new user, I want to create an account with a username and password, so that I can identify myself to my friends.
2. As a registered user, I want to log in with my username and password, so that I can access my groups and friends.
3. As a user, I want my session to persist via JWT so that I don't have to log in every time I open the app.
4. As a user, I want to upload a profile picture, so that my friends can recognise me in the overlay.
5. As a user without a profile picture, I want to see my initials displayed as an avatar, so that I have a visual identity without uploading anything.
6. As a user, I want to search for friends by username and send them a friend request, so that I can connect with people I know.
7. As a user, I want to receive and accept or decline friend requests, so that I control who is in my network.
8. As a user, I want to see my friends list on the right side of the app, so that I can see who is online or offline at a glance.
9. As a user, I want to create a named group and invite friends to it, so that my regular group of people has a persistent home.
10. As a user, I want to see my groups listed on the left side of the app, so that I can quickly navigate between them.
11. As a user, I want each group to have exactly one persistent voice room, so that there is always a place to go without creating or scheduling anything.
12. As a user, I want to click a group to join its voice room, so that joining a call requires one action.
13. As a user, I want to see who is currently in a group's voice room in the center of the app, so that I know if anyone is around before I join.
14. As a user, I want to see a visual speaking indicator on group members who are talking, so that I can tell who is speaking in a group call.
15. As a user, I want push-to-talk enabled by default, so that my mic is never accidentally live.
16. As a user, I want to configure a hotkey for push-to-talk, so that I can use whichever key is comfortable for me.
17. As a user, I want a toggle mode where I click once to unmute and click again to mute, so that I can choose the interaction style I prefer.
18. As a user in a call, I want a floating always-on-top overlay in the top-right corner of my screen, so that I can see my mic status and control it while using other apps.
19. As a user, I want the overlay to show my avatar with a green hue when my mic is live and a red hue when muted, so that I can read my mic state at a glance without focusing the app.
20. As a user, I want to leave a voice room from the overlay without switching back to the main app, so that I can end the call without interrupting my workflow.
21. As a user, I want to dismiss or minimise the overlay when I'm not in a call, so that it doesn't clutter my screen.
22. As a self-hoster, I want a Docker Compose file that starts both the backend and the SFU together, so that I or any friend can run the server with one command.
23. As a self-hoster, I want to configure the server address in the client app, so that my friend group can point to our own instance.
24. As a group member, I want to leave a group, so that I can remove myself from groups I no longer want to be in.
25. As a group creator, I want to invite additional friends to an existing group, so that the group can grow over time.

## Implementation Decisions

- **Backend**: Java Spring Boot. Handles user auth, friend relationships, group membership, and WebRTC signaling. Stateless REST API + WebSocket for real-time presence.
- **Auth**: Spring Security with JWT. Username + password only. No social login.
- **Frontend**: React + Electron. Single desktop app targeting Mac, Windows, and Linux. Mac-first for development.
- **Audio routing**: LiveKit (self-hosted SFU). Spring Boot handles signaling; LiveKit handles audio packet forwarding. Clients connect to LiveKit directly for audio; Spring Boot manages session metadata. LiveKit runs as a sidecar in Docker Compose.
- **Infrastructure**: Docker Compose bundles Spring Boot + LiveKit. Anyone can self-host. No managed cloud dependency.
- **Group model**: one group = one persistent voice room. The room always exists. It costs no resources when empty. No scheduling, no on-demand room creation.
- **Friends model**: bidirectional — both users must accept before the friendship is active. Added by exact username match.
- **Avatars**: initials + deterministic background color (hashed from username) as default. Optional image upload stored server-side. No external storage service required for MVP.
- **Overlay**: Electron `BrowserWindow` with `alwaysOnTop: true`, `transparent: true`, frameless. Positioned top-right. Appears on room join, dismisses on leave.
- **Mic modes**: PTT (hold hotkey) and toggle (click button). PTT is the default. Hotkey is user-configurable and stored in local app settings.
- **Speaking indicator**: LiveKit emits audio level events — use these to show a green ring on the avatar of whoever is speaking.
- **Presence**: WebSocket connection to Spring Boot broadcasts who is in each room in real time.

## Testing Decisions

Good tests verify observable behavior from the outside — what the API returns, what the UI renders, what audio state changes — not internal implementation details like service method calls or repository queries.

**Backend (Spring Boot)**
- Integration tests against a real database (not mocked) for auth flows: register, login, JWT validation, expired token rejection.
- Integration tests for friend request lifecycle: send, accept, decline, duplicate request handling.
- Integration tests for group operations: create, invite member, join room, leave room.
- WebSocket tests for presence: user joins room → other connected clients receive presence update.

**Frontend (React + Electron)**
- Component tests for the overlay: green hue when mic live, red hue when muted, correct avatar/initials rendering.
- Component tests for the friends list: online/offline state rendering, pending request display.
- E2E test (Playwright or Spectron) for the full join-call flow: click group → overlay appears → mic button state correct.

**Audio / LiveKit**
- LiveKit integration is infrastructure, not business logic. Trust the LiveKit SDK. Test the seam: does the Spring Boot signaling correctly negotiate a LiveKit room token and return it to the client?

**Prior art**: none (greenfield project). Establish the integration test pattern early — the first auth test becomes the template for all others.

## Out of Scope

- Text messaging of any kind
- Mobile apps (iOS, Android)
- Screen sharing
- Video
- Noise suppression / echo cancellation (beyond what the browser/OS provides)
- Social discovery (no public group search, no user search beyond exact username)
- Moderation tools (no bans, no roles, no permissions beyond group membership)
- Notifications (push, email, or otherwise)
- Social login (Google, GitHub, etc.)
- Managed cloud hosting — users self-host
- Multiple voice rooms per group
- Recording
- Bots or integrations

## Further Notes

- The name "Green Button" comes from the visual metaphor: green = you're live, red = you're muted. This should be the dominant visual language throughout the app and especially the overlay.
- The self-hosting model (Docker Compose, one command) is a first-class feature, not an afterthought. The README should make this the primary getting-started path.
- PTT as default is a deliberate product decision — it means users never accidentally broadcast. Toggle mode is available but not the first thing new users see.
- LiveKit is open source (Apache 2.0) and can be self-hosted at no cost. The Docker image is `livekit/livekit-server`.
- For the MVP, a single Hetzner CX22 (~€4/month) comfortably handles dozens of concurrent users. Document this as the recommended self-hosting target.
