export default function App() {
  return (
    <div style={{ display: 'flex', height: '100vh', fontFamily: 'sans-serif' }}>
      <aside style={{ width: 200, background: '#1a1a1a', padding: 16 }}>
        <h3 style={{ color: '#fff', margin: 0 }}>Groups</h3>
      </aside>
      <main style={{ flex: 1, background: '#2a2a2a', padding: 16 }}>
        <h3 style={{ color: '#fff', margin: 0 }}>In the room</h3>
      </main>
      <aside style={{ width: 200, background: '#1a1a1a', padding: 16 }}>
        <h3 style={{ color: '#fff', margin: 0 }}>Friends</h3>
      </aside>
    </div>
  )
}
