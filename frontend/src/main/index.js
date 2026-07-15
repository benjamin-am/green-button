import { app, BrowserWindow, globalShortcut } from 'electron'
import { join } from 'path'

let mainWindow
let overlayWindow

function createMainWindow() {
  mainWindow = new BrowserWindow({
    width: 900,
    height: 600,
    webPreferences: {
      preload: join(__dirname, '../preload/index.js'),
      contextIsolation: true
    }
  })

  if (process.env.NODE_ENV === 'development') {
    mainWindow.loadURL('http://localhost:5173')
  } else {
    mainWindow.loadFile(join(__dirname, '../renderer/index.html'))
  }
}

function createOverlayWindow() {
  overlayWindow = new BrowserWindow({
    width: 80,
    height: 80,
    x: 20,
    y: 20,
    frame: false,
    transparent: true,
    alwaysOnTop: true,
    resizable: false,
    skipTaskbar: true,
    webPreferences: {
      preload: join(__dirname, '../preload/index.js'),
      contextIsolation: true
    }
  })

  if (process.env.NODE_ENV === 'development') {
    overlayWindow.loadURL('http://localhost:5173/overlay.html')
  } else {
    overlayWindow.loadFile(join(__dirname, '../renderer/overlay.html'))
  }

  overlayWindow.hide()
}

app.whenReady().then(() => {
  createMainWindow()
  createOverlayWindow()
})

app.on('window-all-closed', () => {
  if (process.platform !== 'darwin') app.quit()
})
