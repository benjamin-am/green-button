import { contextBridge, ipcRenderer } from 'electron'

contextBridge.exposeInMainWorld('electron', {
  onPttPress: (cb) => ipcRenderer.on('ptt-press', cb),
  onPttRelease: (cb) => ipcRenderer.on('ptt-release', cb),
  showOverlay: () => ipcRenderer.send('show-overlay'),
  hideOverlay: () => ipcRenderer.send('hide-overlay')
})
